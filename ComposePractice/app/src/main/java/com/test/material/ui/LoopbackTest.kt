package com.test.material.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.test.material.R

class LoopbackTest : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {

    companion object {
        private const val CLASS_NAME = "LoopbackTest"
    }

    internal var loopbackTaskThread: Thread? = null
        private set
    internal var isRecording = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loopback_layout)

        requestPermission()
        initUI()
    }

    private fun requestPermission() {
        if (isRecordingPermissionGranted()) return

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
    }

    private fun isRecordingPermissionGranted() =
        (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED)

    private fun initUI() {
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(radioGroup: RadioGroup, i: Int) {
        if (radioGroup.checkedRadioButtonId == R.id.bt_start) {
            if (!isRecording) {
                Log.d(CLASS_NAME, "create Loopback thread..")
                startLoopback()
            }
        } else {
            if (isRecording) {
                Log.d(CLASS_NAME, "destroy Loopback thread..")
                stopLoopback()
            }
        }
    }

    private fun startLoopback() {
        isRecording = true
        if (loopbackTaskThread == null) loopbackTaskThread = Thread(LoopbackTask())
        loopbackTaskThread?.start()
    }

    private fun stopLoopback() {
        isRecording = false
        loopbackTaskThread?.interrupt()
        loopbackTaskThread = null
    }

    class LoopbackTask : Runnable {

        companion object {
            private const val CLASS_NAME = "LoopbackTask"
        }

        private val sampleRate = 44100
        private val channelMask = AudioFormat.CHANNEL_IN_STEREO
        private val encoding = AudioFormat.ENCODING_PCM_16BIT
        private val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelMask, encoding) * 3
        private val readData = ShortArray(bufferSize)
        private var audioRecorder: AudioRecord? = null
        private var audioTracker: AudioTrack? = null

        override fun run() {
            Log.d(CLASS_NAME, "Loopback Thread started..")
            prepareLoopbackTask()

            do {
                playAudio(recordAudio())
            } while (!Thread.interrupted())

            Log.d(CLASS_NAME, "Loopback Thread stopped..")
            closeAudioRecorder()
            closeAudioTracker()
            Log.d(CLASS_NAME, "Loopback Thread finished..")
        }

        private fun prepareLoopbackTask() {
            val audioFormat = initAudioFormat()
            audioRecorder = initAudioRecord(audioFormat)
            audioTracker = initAudioTrack(audioFormat)
        }

        private fun initAudioFormat(): AudioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(encoding)
            .setChannelMask(channelMask)
            .build()

        @SuppressLint("MissingPermission")
        private fun initAudioRecord(audioFormat: AudioFormat): AudioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setBufferSizeInBytes(bufferSize)
            .setAudioFormat(audioFormat)
            .build()

        private fun initAudioTrack(audioFormat: AudioFormat) = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder().setLegacyStreamType(AudioManager.STREAM_MUSIC).build()
            )
            .setAudioFormat(audioFormat)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setSessionId(1)
            .build()

        private fun closeAudioTracker() {
            audioTracker?.flush()
            audioTracker?.stop()
            audioTracker?.release()
            audioTracker = null
        }

        private fun closeAudioRecorder() {
            audioRecorder?.stop()
            audioRecorder?.release()
            audioRecorder = null
        }

        private fun recordAudio(): Int {
            audioRecorder?.startRecording() ?: return -1

            val input = audioRecorder?.read(readData, 0, bufferSize) ?: -1
            Log.d(CLASS_NAME, "read bytes = $input")
            return input
        }

        private fun playAudio(input: Int) {
            if (input == -1) return

            if (audioTracker?.playState != AudioTrack.PLAYSTATE_PLAYING) {
                audioTracker?.play()
            }

            val output = audioTracker?.write(readData, 0, input)
            Log.d(CLASS_NAME, "write bytes = $output")
        }
    }
}

