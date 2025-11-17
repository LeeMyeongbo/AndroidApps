package com.alarm.newsalarmkt.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlarmDataDao {
    @Insert
    fun insert(vararg data: AlarmData)

    @Update
    fun update(data: AlarmData)

    @Delete
    fun delete(data: AlarmData)

    @get:Query("SELECT * FROM AlarmDataTable")
    val all: MutableList<AlarmData>

    @Query("DELETE FROM AlarmDataTable")
    fun clear()
}
