package com.alarm.newsalarmkt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alarm.newsalarmkt.alarmmanager.AlarmSetter
import com.alarm.newsalarmkt.database.AlarmData
import com.alarm.newsalarmkt.database.AlarmDatabaseUtil
import com.alarm.newsalarmkt.utils.AlarmHelperUtil.getProperAlarmDate
import com.alarm.newsalarmkt.utils.AlarmHelperUtil.isProperWeekdayInPeriodic
import com.alarm.newsalarmkt.utils.LogUtil.logE
import com.alarm.newsalarmkt.utils.LogUtil.logI
import com.alarm.newsalarmkt.utils.LogUtil.logW
import com.alarm.newsalarmkt.utils.WakeLockUtil

class AlarmReceiver : BroadcastReceiver() {

    private lateinit var context: Context
    private lateinit var alarmSetter: AlarmSetter

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        this.alarmSetter = AlarmSetter(context)

        val action = intent.action
        if (action == null) {
            logE(CLASS_NAME, "onReceive", "action is null")
            return
        }

        logI(CLASS_NAME, "onReceive", "action : $action")
        when (action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> manageAlarms()
            "com.alarm.newsalarm.ACTION_ALARM_GOES_OFF" -> processAlarm(intent)
        }
    }

    private fun manageAlarms() {
        val alarmDataList = AlarmDatabaseUtil.getAll(context)
        for (data in alarmDataList) {
            if (!isAlarmReregisterable(data)) {
                continue
            }
            if (data.periodicWeekBit == 0) {
                registerOneTimeAlarmAgain(data)
            } else {
                registerPeriodicAlarmAgain(data)
            }
        }
    }

    private fun isAlarmReregisterable(data: AlarmData): Boolean {
        if (!data.isActive) {
            return false
        }
        if (data.specificDateInMillis <= System.currentTimeMillis() && data.periodicWeekBit == 0) {
            deactivateInvalidAlarm(data)
            return false
        }
        return true
    }

    private fun deactivateInvalidAlarm(data: AlarmData) {
        logI(CLASS_NAME, "deactivateInvalidAlarm", "deactivate alarm ${data.id}")
        data.isActive = false
        AlarmDatabaseUtil.update(context, data)
    }

    private fun registerOneTimeAlarmAgain(data: AlarmData) {
        logI(CLASS_NAME, "registerOneTimeAlarmAgain", "register alarm ${data.id}")
        alarmSetter.registerAlarm(data)
    }

    private fun registerPeriodicAlarmAgain(data: AlarmData) {
        logI(CLASS_NAME, "registerPeriodicAlarmAgain", "register alarm ${data.id}")
        val properDateTime = getProperAlarmDate(data.specificDateInMillis)
        data.specificDateInMillis = properDateTime
        alarmSetter.registerAlarm(data)
        AlarmDatabaseUtil.update(context, data)
    }

    private fun processAlarm(intent: Intent) {
        val data = intent.getParcelableExtra("alarmData", AlarmData::class.java)
        if (data == null) {
            logE(CLASS_NAME, "processAlarm", "alarm data is null")
            return
        }
        if (data.periodicWeekBit == 0) {
            processOneTimeAlarm(data)
        } else {
            processPeriodicAlarm(data)
        }
    }

    private fun processOneTimeAlarm(data: AlarmData) {
        logI(CLASS_NAME, "processOneTimeAlarm", "start one-time alarm ${data.id}")
        startNotifierActivity(data)
        data.isActive = false
        AlarmDatabaseUtil.update(context, data)
    }

    private fun processPeriodicAlarm(data: AlarmData) {
        if (isProperWeekdayInPeriodic(data)) {
            logI(CLASS_NAME, "processPeriodicAlarm", "start periodic alarm ${data.id}")
            startNotifierActivity(data)
        } else {
            logW(CLASS_NAME, "processPeriodicAlarm", "alarm ${data.id} is not today")
        }
        registerPeriodicAlarmAgain(data)
    }

    private fun startNotifierActivity(data: AlarmData) {
        if (WakeLockUtil.acquireWakeLock(context)) {
//            val intent: Intent = Intent(context, AlarmNotifierActivity::class.java)
//            intent.putExtra("alarmData", data)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context.startActivity(intent)
            return
        }
        logI(
            CLASS_NAME,
            "startNotifierActivity",
            "another alarm already arrived and started notifier -> alarm ${data.id} dismissed"
        )
    }

    companion object {
        private const val CLASS_NAME = "AlarmReceiver"
    }
}
