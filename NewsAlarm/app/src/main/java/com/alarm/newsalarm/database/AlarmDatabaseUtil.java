package com.alarm.newsalarm.database;

import android.content.Context;

import androidx.room.Room;

import com.alarm.newsalarm.utils.LogUtil;

import java.util.ArrayList;
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
        Context storageContext = context.createDeviceProtectedStorageContext();
        return db = Room
            .databaseBuilder(storageContext, AlarmDatabase.class, "AlarmDB")
            .fallbackToDestructiveMigrationOnDowngrade()
            .build();
    }

    public static void insert(Context context, AlarmData data) {
        LogUtil.logD(CLASS_NAME, "insert", "alarm data insertion started!");
        new Thread(() -> getDB(context).alarmDataDao().insert(data)).start();
    }

    public static void update(Context context, AlarmData data) {
        LogUtil.logD(CLASS_NAME, "update", "alarm data update started!");
        new Thread(() -> getDB(context).alarmDataDao().update(data)).start();
    }

    public static void delete(Context context, AlarmData data) {
        LogUtil.logD(CLASS_NAME, "delete", "alarm data removal started!");
        new Thread(() -> getDB(context).alarmDataDao().delete(data)).start();
    }

    public static List<AlarmData> getAll(Context context) {
        AtomicReference<List<AlarmData>> alarmList = new AtomicReference<>();
        Thread thread = new Thread(() -> alarmList.set(getDB(context).alarmDataDao().getAll()));
        try {
            LogUtil.logD(CLASS_NAME, "getAll", "load all alarm data started!");
            thread.start();
            thread.join(3000);
            return alarmList.get();
        } catch (InterruptedException e) {
            LogUtil.logE(CLASS_NAME, "getAll",  e);
        }

        return new ArrayList<>();
    }

    public static void clear(Context context) {
        LogUtil.logD(CLASS_NAME, "clear", "clear all alarm data started!");
        new Thread(() -> getDB(context).alarmDataDao().clear()).start();
    }
}
