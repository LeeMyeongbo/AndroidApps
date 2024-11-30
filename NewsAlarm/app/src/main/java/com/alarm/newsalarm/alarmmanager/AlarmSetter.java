package com.alarm.newsalarm.alarmmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.AlarmManagerCompat;

import com.alarm.newsalarm.MainActivity;
import com.alarm.newsalarm.AlarmReceiver;
import com.alarm.newsalarm.database.AlarmData;

public class AlarmSetter {

    private final Context context;
    private final AlarmManager alarmManager;

    public AlarmSetter(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void registerAlarm(AlarmData data) {
        PendingIntent pShowIntent = getPendingReserveIntent(data.getId());
        PendingIntent pNotifyIntent = getPendingNotifyIntent(data);

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

    private PendingIntent getPendingNotifyIntent(AlarmData data) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarmData", data);
        intent.setAction("com.alarm.newsalarm.ACTION_ALARM_GOES_OFF");

        return PendingIntent.getBroadcast(context, data.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public void cancelAlarm(AlarmData data) {
        alarmManager.cancel(getPendingNotifyIntent(data));
    }
}
