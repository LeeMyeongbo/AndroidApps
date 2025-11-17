package com.alarm.newsalarmkt.outputmanager

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.alarm.newsalarmkt.R
import com.alarm.newsalarmkt.utils.LogUtil

class SoundPlayer(private val context: Context) {

    private lateinit var attr: AudioAttributes
    private var player: MediaPlayer? = null

    init {
        initAudioAttributes()
        initMediaPlayer()
    }

    private fun initAudioAttributes() {
        attr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }

    private fun initMediaPlayer() {
        player = MediaPlayer.create(context, R.raw.morningkiss, attr, 1)
        player?.setVolume(0.15f, 0.15f)
        player?.isLooping = true
    }

    fun playBgm() {
        player?.start()
        LogUtil.logD(CLASS_NAME, "playBgm", "sound player started to play!")
    }

    fun release() {
        player?.stop()
        player?.release()
        player = null
        LogUtil.logD(CLASS_NAME, "release", "sound player released completely!")
    }

    companion object {
        private const val CLASS_NAME = "SoundPlayer"
    }
}
