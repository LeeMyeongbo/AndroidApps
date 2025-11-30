package com.reminder.wifireminder;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Routine_Table.class}, version = 1)
public abstract class Routine_Database extends RoomDatabase {

    private static Routine_Database INSTANCE;

    public abstract Routine_Dao routine_dao();

    public static Routine_Database getRoutineDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, Routine_Database.class, "routine-db").build();
        }
        return INSTANCE;
    }
}
