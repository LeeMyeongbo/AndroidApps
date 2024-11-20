package com.alarm.newsalarm.alarmmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.AlarmManagerCompat;

import com.alarm.newsalarm.MainActivity;
import com.alarm.newsalarm.database.AlarmData;

public class AlarmSetter {

    private final Context context;
    private final AlarmManager alarmManager;

    public AlarmSetter(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setSpecificAlarm(AlarmData data) {
        PendingIntent pShowIntent = getPendingReserveIntent(data.getId());
        PendingIntent pNotifyIntent = getPendingNotifyIntent(data.getId());

        AlarmManagerCompat.setAlarmClock(
            alarmManager, data.getSpecificDateInMillis(), pShowIntent, pNotifyIntent
        );
    }

    private PendingIntent getPendingReserveIntent(int id) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent getPendingNotifyIntent(int id) {
        Intent intent = new Intent(context, AlarmReceiver.class);

        return PendingIntent.getBroadcast(context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public void cancelAlarm(AlarmData data) {
        alarmManager.cancel(getPendingNotifyIntent(data.getId()));
    }

    public void setPeriodicAlarm() {
        /* To Do */
    }
}
