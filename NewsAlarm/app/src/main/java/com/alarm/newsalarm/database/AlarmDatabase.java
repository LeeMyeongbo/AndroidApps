package com.alarm.newsalarm.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {AlarmData.class}, version = 1)
public abstract class AlarmDatabase extends RoomDatabase {

    public abstract AlarmDataDao alarmDataDao();
}
