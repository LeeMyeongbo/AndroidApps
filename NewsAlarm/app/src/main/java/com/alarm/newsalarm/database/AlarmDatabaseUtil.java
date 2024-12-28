package com.alarm.newsalarm.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AlarmDatabaseUtil {

    private static final String CLASS_NAME = "AlarmDatabaseUtil";
    private static AlarmDatabase db;

    private AlarmDatabaseUtil() {
    }

    private synchronized static AlarmDatabase getDB(Context context) {
        if (db != null) {
            return db;
        }
        return db = Room
            .databaseBuilder(context.getApplicationContext(), AlarmDatabase.class, "AlarmDB")
            .build();
    }

    public static void insert(Context context, AlarmData data) {
        Log.i(CLASS_NAME, "insert$alarm data insertion started!");
        new Thread(() -> getDB(context).alarmDataDao().insert(data)).start();
    }

    public static void update(Context context, AlarmData data) {
        Log.i(CLASS_NAME, "update$alarm data update started!");
        new Thread(() -> getDB(context).alarmDataDao().update(data)).start();
    }

    public static void delete(Context context, AlarmData data) {
        Log.i(CLASS_NAME, "delete$alarm data removal started!");
        new Thread(() -> getDB(context).alarmDataDao().delete(data)).start();
    }

    public static List<AlarmData> getAll(Context context) {
        AtomicReference<List<AlarmData>> alarmList = new AtomicReference<>();
        Thread thread = new Thread(() ->
            alarmList.set(getDB(context).alarmDataDao().getAll())
        );
        try {
            Log.i(CLASS_NAME, "getAll$load all alarm data");
            thread.start();
            thread.join(3000);
        } catch (InterruptedException e) {
            Log.e(CLASS_NAME, "getAll$" + e);
            throw new RuntimeException(e);
        }
        return alarmList.get();
    }

    public static void clear(Context context) {
        Log.i(CLASS_NAME, "clear$clear all alarm data");
        new Thread(() -> getDB(context).alarmDataDao().clear()).start();
    }
}
