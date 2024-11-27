package com.alarm.newsalarm.alarmmanager;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockUtil {

    private static PowerManager.WakeLock wakeLock;

    private WakeLockUtil() {
    }

    public synchronized static boolean acquireWakeLock(Context context) {
        if (wakeLock != null) {
            return false;
        }
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "newAlarm:tag");
        wakeLock.acquire(1000L * 60 * 30);
        return true;
    }

    public static void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}
