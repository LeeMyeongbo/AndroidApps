package com.alarm.newsalarmkt.utils

import android.content.Context
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import com.alarm.newsalarmkt.utils.LogUtil.logI

object WakeLockUtil {

    private const val CLASS_NAME = "WakeLockUtil"
    private lateinit var wakeLock: WakeLock

    @Synchronized
    fun acquireWakeLock(context: Context): Boolean {
        if (::wakeLock.isInitialized) {
            return false
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "newAlarm:tag")
        wakeLock.acquire(1000L * 60 * 30)
        logI(CLASS_NAME, "acquireWakeLock", "wake lock acquired! (max 30m)")
        return true
    }

    fun releaseWakeLock() {
        if (!::wakeLock.isInitialized || !wakeLock.isHeld) {
            return
        }
        wakeLock.release()
        logI(CLASS_NAME, "releaseWakeLock", "wake lock released!")
    }
}
