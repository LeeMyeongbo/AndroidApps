package com.alarm.newsalarmkt.outputmanager

import android.content.Context
import android.os.CombinedVibration
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.VibrationEffect
import android.os.VibratorManager
import com.alarm.newsalarmkt.utils.LogUtil

class Vibrator(context: Context) {

    private val vibrator: VibratorManager = context
        .getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val effect = VibrationEffect.createOneShot(1000, msg.arg1)
            vibrator.vibrate(CombinedVibration.createParallel(effect))
            sendMessageAgain(msg.arg1)
        }

        private fun sendMessageAgain(amplitude: Int) {
            val message = obtainMessage()
            message.what = msgVibrate
            message.arg1 = amplitude
            sendMessageDelayed(message, 2000)
        }
    }
    private val msgVibrate = 0x01

    init {
        LogUtil.logI(CLASS_NAME, "constructor", "vibrator initialized completely!")
    }

    fun vibrateOnce(amplitude: Int) {
        if (amplitude == 0) {
            LogUtil.logD(CLASS_NAME, "vibrateOnce", "amplitude : 0")
            return
        }

        LogUtil.logD(CLASS_NAME, "vibrateOnce", "amplitude : $amplitude")
        val effect = VibrationEffect.createOneShot(500, amplitude)
        vibrator.vibrate(CombinedVibration.createParallel(effect))
    }

    fun vibrateRepeatedly(amplitude: Int) {
        if (amplitude == 0) {
            LogUtil.logD(CLASS_NAME, "vibrateRepeatedly", "amplitude : 0")
            return
        }

        val msg = handler.obtainMessage()
        msg.what = msgVibrate
        msg.arg1 = amplitude
        handler.sendMessageDelayed(msg, 1000)
        LogUtil.logD(CLASS_NAME, "vibrateRepeatedly", "amplitude : $amplitude")
    }

    fun release() {
        handler.removeMessages(msgVibrate)
        vibrator.cancel()
        LogUtil.logI(CLASS_NAME, "release", "vibrator released completely!")
    }

    companion object {
        private const val CLASS_NAME = "Vibrator"
    }
}
