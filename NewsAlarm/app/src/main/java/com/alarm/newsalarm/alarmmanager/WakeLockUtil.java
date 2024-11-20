package com.alarm.newsalarm.alarmmanager;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockUtil {

    private static PowerManager.WakeLock wakeLock;

    private WakeLockUtil() {
    }

    public static void acquireWakeLock(Context context) {
        if (wakeLock != null) {
            return;
        }
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "newAlarm:tag");
        wakeLock.acquire(10 * 60 * 1000L);
    }

    public static void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
