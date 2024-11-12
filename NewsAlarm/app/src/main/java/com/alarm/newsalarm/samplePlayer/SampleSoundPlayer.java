package com.alarm.newsalarm.samplePlayer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class SampleSoundPlayer {

    private static final String CLASS_NAME = "SampleSoundPlayer";
    private final Context context;
    private SoundPool soundPool;
    private int soundId, streamId;

    public SampleSoundPlayer(Context context) {
        this.context = context;
        initSound();
    }

    private void initSound() {
        AudioAttributes attributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setLegacyStreamType(AudioManager.STREAM_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
            .build();
        soundPool = new SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build();
    }

    public void playSound(int rawId, float volume) {
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            streamId = soundPool.play(soundId, volume / 100f, volume / 100f, 1, 0, 1f);
            if (streamId != 0) {
                Log.i(CLASS_NAME, "playSound$sample sound playing successful!!");
                return;
            }
            Log.e(CLASS_NAME, "playSound$sample sound playing failed!!");
        });
        soundId = soundPool.load(context, rawId, 1);
    }

    public void release() {
        soundPool.stop(streamId);
        soundPool.release();
        soundPool = null;
    }
}
