package com.alarm.newsalarm.alarmmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.AlarmManagerCompat;

import com.alarm.newsalarm.MainActivity;

public class AlarmSetter {

    private final Context context;
    private final AlarmManager alarmManager;

    public AlarmSetter(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setSpecificAlarm(long id, long triggerTimeInMillis) {
        PendingIntent pShowIntent = getPendingReserveIntent((int) id);
        PendingIntent pNotifyIntent = getPendingNotifyIntent((int) id);

        AlarmManagerCompat.setAlarmClock(
            alarmManager, triggerTimeInMillis, pShowIntent, pNotifyIntent
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

    public void cancelAlarm(long id) {
        alarmManager.cancel(getPendingNotifyIntent((int) id));
    }

    public void setPeriodicAlarm() {
        /* To Do */
    }
}
