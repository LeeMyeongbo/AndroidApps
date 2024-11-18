package com.alarm.newsalarm.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDataDao {

    @Insert
    void insert(AlarmData... data);

    @Update
    void update(AlarmData data);

    @Delete
    void delete(AlarmData data);

    @Query("SELECT * FROM AlarmDataTable")
    List<AlarmData> getAll();

    @Query("DELETE FROM AlarmDataTable")
    void clear();
}
