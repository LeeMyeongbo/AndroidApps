package com.test.loopbacktest;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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
    private Thread audioTaskThread;
    private boolean isRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        initUI();
    }

    void requestPermission() {
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
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioButtonRecordOn) {
            if (!isRecording) {
                Log.d(CLASS_NAME, "start Loopback thread..");
                isRecording = true;
                if (audioTaskThread == null) {
                    audioTaskThread = new Thread(new AudioTask());
                    audioTaskThread.start();
                }
            }
        } else {
            if (isRecording) {
                isRecording = false;
                if (audioTaskThread != null) {
                    audioTaskThread.interrupt();
                    audioTaskThread = null;
                }
            }
        }
    }

    static class AudioTask implements Runnable {

        private static final String CLASS_NAME = "AudioTask";
        private final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
        private final int SAMPLE_RATE = 44100;
        private final int CHANNEL_TYPE = AudioFormat.CHANNEL_IN_STEREO;
        private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        private final int BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_TYPE, AUDIO_FORMAT) * 3;
        private short[] readData;
        private boolean playStarted;
        private AudioRecord audioRecorder;
        private AudioTrack audioTracker;

        @Override
        public void run() {
            Log.d(CLASS_NAME, "Loopback Thread started..");
            initLoopback();

            do {
                int read = recordAudio();
                playAudio(read);
            } while (!Thread.interrupted());

            Log.d(CLASS_NAME, "Loopback Thread stopped..");
            closeAudioRecorder();
            closeAudioTracker();
            Log.d(CLASS_NAME, "Loopback Thread finished..");
        }

        @SuppressLint("MissingPermission")
        private void initLoopback() {
            readData = new short[BUFFER_SIZE];
            audioRecorder = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_TYPE, AUDIO_FORMAT, BUFFER_SIZE);
            audioTracker = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, CHANNEL_TYPE, AUDIO_FORMAT,
                    BUFFER_SIZE, AudioTrack.MODE_STREAM);
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
