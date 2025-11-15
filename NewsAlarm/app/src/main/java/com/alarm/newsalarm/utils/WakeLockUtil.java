package com.alarm.newsalarm.utils;

import android.content.Context;
import android.os.PowerManager;

public class WakeLockUtil {

    private static final String CLASS_NAME = "WakeLockUtil";
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
        LogUtil.logI(CLASS_NAME, "acquireWakeLock", "wake lock acquired! (max 30m)");
        return true;
    }

    public static void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        LogUtil.logI(CLASS_NAME, "releaseWakeLock", "wake lock released!");
    }
}
