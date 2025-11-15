package com.alarm.newsalarm.outputmanager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;

import com.alarm.newsalarm.R;
import com.alarm.newsalarm.utils.LogUtil;

public class SoundPlayer {

    private static final String CLASS_NAME = "SoundPlayer";
    private final Context context;
    private AudioAttributes attr;
    private MediaPlayer player;

    public SoundPlayer(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        initAudioAttributes();
        initMediaPlayer();
    }

    private void initAudioAttributes() {
        attr = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();
    }

    private void initMediaPlayer() {
        player = MediaPlayer.create(context, R.raw.morningkiss, attr, 1);
        player.setVolume(0.15f, 0.15f);
        player.setLooping(true);
    }

    public void playBgm() {
        player.start();
        LogUtil.logD(CLASS_NAME, "playBgm", "sound player started to play!");
    }

    public void release() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        LogUtil.logD(CLASS_NAME, "release", "sound player released completely!");
    }
}
