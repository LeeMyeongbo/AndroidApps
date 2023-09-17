package com.test.loopbacktest;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private static final String CLASS_NAME = "MainActivity";
    private Thread loopbackTaskThread;
    private boolean isRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        initUI();
    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    private void initUI() {
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonRecordOn) {
            if (!isRecording) {
                Log.d(CLASS_NAME, "create Loopback thread..");
                startLoopback();
            }
        } else {
            if (isRecording) {
                Log.d(CLASS_NAME, "destroy Loopback thread..");
                stopLoopback();
            }
        }
    }

    private void startLoopback() {
        isRecording = true;
        if (loopbackTaskThread == null) {
            loopbackTaskThread = new Thread(new LoopbackTask());
            loopbackTaskThread.start();
        }
    }

    private void stopLoopback() {
        isRecording = false;
        if (loopbackTaskThread != null) {
            loopbackTaskThread.interrupt();
            loopbackTaskThread = null;
        }
    }

    private static class LoopbackTask implements Runnable {

        private static final String CLASS_NAME = "LoopbackTask";
        private final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
        private final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
        private final int SAMPLE_RATE = 44100;
        private final int CHANNEL_TYPE = AudioFormat.CHANNEL_IN_STEREO;
        private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        private final int BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_TYPE, AUDIO_FORMAT) * 3;
        private final int MODE_STREAM = AudioTrack.MODE_STREAM;
        private short[] readData;
        private boolean playStarted;
        private AudioRecord audioRecorder;
        private AudioTrack audioTracker;

        @Override
        public void run() {
            Log.d(CLASS_NAME, "Loopback Thread started..");
            prepareLoopbackTask();

            do {
                playAudio(recordAudio());
            } while (!Thread.interrupted());

            Log.d(CLASS_NAME, "Loopback Thread stopped..");
            closeAudioRecorder();
            closeAudioTracker();
            Log.d(CLASS_NAME, "Loopback Thread finished..");
        }

        @SuppressLint("MissingPermission")
        private void prepareLoopbackTask() {
            readData = new short[BUFFER_SIZE];
            audioRecorder = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_TYPE, AUDIO_FORMAT, BUFFER_SIZE);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setLegacyStreamType(STREAM_TYPE)
                .build();

            AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AUDIO_FORMAT)
                .setChannelMask(CHANNEL_TYPE)
                .build();

            audioTracker = new AudioTrack(audioAttributes, audioFormat, BUFFER_SIZE, MODE_STREAM, 0);
        }

        private void closeAudioTracker() {
            audioTracker.flush();
            audioTracker.stop();
            audioTracker.release();
            audioTracker = null;
        }

        private void closeAudioRecorder() {
            audioRecorder.stop();
            audioRecorder.release();
            audioRecorder = null;
        }

        private int recordAudio() {
            audioRecorder.startRecording();

            int input = audioRecorder.read(readData, 0, BUFFER_SIZE);
            Log.d(CLASS_NAME, "read bytes = " + input);
            return input;
        }

        private void playAudio(int input) {
            if (!playStarted) {
                audioTracker.play();
                playStarted = true;
            }

            int output = audioTracker.write(readData, 0, input);
            Log.d(CLASS_NAME, "write bytes = " + output);
        }
    }
}
