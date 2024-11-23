package com.alarm.newsalarm.outputmanager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import com.alarm.newsalarm.R;

import java.io.IOException;

public class SoundPlayer {

    private static final String CLASS_NAME = "SoundPlayer";
    private final Context context;
    private final AudioManager manager;
    private AudioAttributes attr;
    private SoundPool soundPool;
    private MediaPlayer player;
    private int soundId, streamId;

    public SoundPlayer(Context context) {
        this.context = context;
        manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        init();
    }

    private void init() {
        initAudioAttributes();
        initSoundPool();
        initMediaPlayer();
    }

    private void initAudioAttributes() {
        attr = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setLegacyStreamType(AudioManager.STREAM_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
            .build();
    }

    private void initSoundPool() {
        soundPool = new SoundPool.Builder().setAudioAttributes(attr).build();
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            streamId = soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
            if (streamId != 0) {
                Log.i(CLASS_NAME, "playShortSound$sample sound playing successful!");
                return;
            }
            Log.e(CLASS_NAME, "playShortSound$sample sound playing failed!");
        });
    }

    private void initMediaPlayer() {
        player = MediaPlayer.create(context, R.raw.morningkiss, attr, 1);
        player.setVolume(1f, 1f);
        player.setLooping(true);
    }

    public void playShortSound(int volume) {
        manager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
        soundId = soundPool.load(context, R.raw.ding_dong, 1);
    }

    public void playBgm(int volume) {
        manager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
        player.start();
    }

    public void release() {
        if (soundPool != null) {
            soundPool.stop(streamId);
            soundPool.release();
            soundPool = null;
        }
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
}
