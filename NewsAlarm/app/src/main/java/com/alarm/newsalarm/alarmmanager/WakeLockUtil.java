package com.alarm.newsalarm.alarmmanager;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class WakeLockUtil {

    private static final String CLASS_NAME = "WakeLockUtil";
    private static PowerManager.WakeLock wakeLock;

    private WakeLockUtil() {
    }

    public synchronized static boolean acquireWakeLock(Context context) {
        if (wakeLock != null) {
            Log.i(CLASS_NAME, "acquireWakeLock$already acquired -> starting notifier dismissed");
            return false;
        }
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "newAlarm:tag");
        wakeLock.acquire(1000L * 60 * 30);
        Log.i(CLASS_NAME, "acquireWakeLock$wake lock acquired(max time 30m)!");
        return true;
    }

    public static void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        Log.i(CLASS_NAME, "releaseWakeLock$wake lock released completely!");
    }
}
