package com.alarm.newsalarm.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class AlarmDatabaseUtil {

    private static final String CLASS_NAME = "AlarmDatabaseUtil";
    private static AlarmDatabase db;

    private AlarmDatabaseUtil() {
    }

    private static AlarmDatabase getDB(Context context) {
        if (db != null) {
            return db;
        }
        return db = Room
            .databaseBuilder(context.getApplicationContext(), AlarmDatabase.class, "AlarmDB")
            .build();
    }

    private static boolean isValid(AlarmData data) {
        return isVolumeSizeValid(data) && isVibIntensityValid(data) && isTimeValid(data)
                && (isSpecificDateValid(data) || data.getPeriodicWeekBit() > 0);
    }

    private static boolean isVolumeSizeValid(AlarmData data) {
        float vol = data.getVolumeSize();
        if (Float.compare(vol, 0f) >= 0 && Float.compare(vol, 100f) <= 0) {
            return true;
        }
        Log.e(CLASS_NAME, "isVolumeSizeValid$volume size must be set by 0.0 to 100.0!");
        return false;
    }

    private static boolean isVibIntensityValid(AlarmData data) {
        int intensity = data.getVibIntensity();
        if (intensity >= 0 && intensity < 256) {
            return true;
        }
        Log.e(CLASS_NAME, "isVibIntensityValid$vibration intensity must be set by 0 to 255!");
        return false;
    }

    private static boolean isSpecificDateValid(AlarmData data) {
        long dateInMillis = data.getSpecificDateInMillis();
        if (dateInMillis > System.currentTimeMillis()) {
            return true;
        }
        Log.e(CLASS_NAME, "isSpecificDateValid$alarm date must be set by future date!");
        return false;
    }

    private static boolean isTimeValid(AlarmData data) {
        int timeMin = data.getTimeInMin();
        if (timeMin >= 0 && timeMin < 1440) {
            return true;
        }
        Log.e(CLASS_NAME, "isTimeValid$time must be set by 0(00:00) to 1439(23:59)!");
        return false;
    }

    public static boolean insert(Context context, AlarmData data) {
        if (isValid(data)) {
            new Thread(() -> getDB(context).alarmDataDao().insert(data)).start();
            return true;
        }
        Log.i(CLASS_NAME, "insert$alarm data insertion failed!");
        return false;
    }

    public static boolean update(Context context, AlarmData data) {
        if (isValid(data)) {
            new Thread(() -> getDB(context).alarmDataDao().update(data)).start();
            return true;
        }
        Log.i(CLASS_NAME, "update$alarm data update failed!");
        return false;
    }

    public static void delete(Context context, AlarmData data) {
        new Thread(() -> getDB(context).alarmDataDao().delete(data)).start();
    }

    public static ArrayList<AlarmData> getAll(Context context) {
        AtomicReference<ArrayList<AlarmData>> alarmList = new AtomicReference<>();
        Thread thread = new Thread(() ->
            alarmList.set(new ArrayList<>(getDB(context).alarmDataDao().getAll()))
        );
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
            Log.e(CLASS_NAME, "getAll$" + e.getMessage());
            throw new RuntimeException(e);
        }
        return alarmList.get();
    }

    public static void clear(Context context) {
        new Thread(() -> getDB(context).alarmDataDao().clear()).start();
    }
}
