package com.reminder.wifireminder;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface Routine_Dao {

    @Query("SELECT * FROM routine_Table WHERE BSSID=:bssid")
    Routine_Table select(String bssid);

    @Query("SELECT * FROM routine_table")
    List<Routine_Table> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Routine_Table wifi);

    @Delete
    void delete(Routine_Table... routine_tables);

    @Query("DELETE FROM routine_table")
    void clear();
}
