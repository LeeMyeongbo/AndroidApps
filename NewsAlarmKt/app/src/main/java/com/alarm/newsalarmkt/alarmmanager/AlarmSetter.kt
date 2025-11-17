package com.alarm.newsalarmkt.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.alarm.newsalarmkt.AlarmReceiver
import com.alarm.newsalarmkt.MainActivity
import com.alarm.newsalarmkt.database.AlarmData

class AlarmSetter(private val context: Context) {

    private val alarmManager: AlarmManager = context
        .getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun registerAlarm(data: AlarmData) {
        val pShowIntent = getPendingReserveIntent(data.id)
        val pNotifyIntent = getPendingNotifyIntent(data)

        AlarmManagerCompat.setAlarmClock(
            alarmManager, data.specificDateInMillis, pShowIntent, pNotifyIntent
        )
    }

    private fun getPendingReserveIntent(id: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getPendingNotifyIntent(data: AlarmData): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("alarmData", data)
        intent.setAction("com.alarm.newsalarm.ACTION_ALARM_GOES_OFF")

        return PendingIntent.getBroadcast(
            context, data.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancelAlarm(data: AlarmData) {
        alarmManager.cancel(getPendingNotifyIntent(data))
    }
}
