package com.alarm.newsalarmkt.database

import android.content.Context
import androidx.room.Room.databaseBuilder
import com.alarm.newsalarmkt.utils.LogUtil
import java.util.concurrent.atomic.AtomicReference

object AlarmDatabaseUtil {

    private const val CLASS_NAME = "AlarmDatabaseUtil"
    private lateinit var db: AlarmDatabase

    @Synchronized
    private fun getDB(context: Context): AlarmDatabase {
        if (::db.isInitialized) {
            return db
        }

        val storageContext = context.createDeviceProtectedStorageContext()
        return databaseBuilder(storageContext, AlarmDatabase::class.java, "AlarmDB")
            .build()
            .also { db = it }
    }

    fun insert(context: Context, data: AlarmData) {
        LogUtil.logD(CLASS_NAME, "insert", "alarm data insertion started!")
        Thread { getDB(context).alarmDataDao().insert(data) }.start()
    }

    fun update(context: Context, data: AlarmData) {
        LogUtil.logD(CLASS_NAME, "update", "alarm data update started!")
        Thread { getDB(context).alarmDataDao().update(data) }.start()
    }

    fun delete(context: Context, data: AlarmData) {
        LogUtil.logD(CLASS_NAME, "delete", "alarm data removal started!")
        Thread { getDB(context).alarmDataDao().delete(data) }.start()
    }

    fun getAll(context: Context): MutableList<AlarmData> {
        val alarmList = AtomicReference<MutableList<AlarmData>>()
        val thread = Thread { alarmList.set(getDB(context).alarmDataDao().all) }
        try {
            LogUtil.logD(CLASS_NAME, "getAll", "load all alarm data started!")
            thread.start()
            thread.join(3000)
            return alarmList.get()
        } catch (e: InterruptedException) {
            LogUtil.logE(CLASS_NAME, "getAll", e)
        }

        return ArrayList()
    }

    fun clear(context: Context) {
        LogUtil.logD(CLASS_NAME, "clear", "clear all alarm data started!")
        Thread { getDB(context).alarmDataDao().clear() }.start()
    }
}
