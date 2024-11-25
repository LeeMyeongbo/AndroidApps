package com.alarm.newsalarm.alarmmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alarm.newsalarm.AlarmNotifierActivity;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;

import java.util.Calendar;
import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "AlarmReceiver";
    private final Calendar calendar = Calendar.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(CLASS_NAME, "onReceive");

        AlarmData data = Objects.requireNonNull(
            intent.getParcelableExtra("alarmData", AlarmData.class)
        );
        if (data.getPeriodicWeekBit() > 0) {
            managePeriodicAlarm(context, data);
        } else {
            manageSpecificAlarm(context, data);
        }
    }

    private void managePeriodicAlarm(Context context, AlarmData data) {
        if ((data.getPeriodicWeekBit() | (1 << getCurWeekDay(data))) > 0) {
            Log.i(CLASS_NAME, "managePeriodicAlarm$periodic alarm started");
            startNotifierActivity(context, data);
        }
        registerNextDayAlarm(context, data);
    }

    private int getCurWeekDay(AlarmData data) {
        calendar.setTimeInMillis(data.getSpecificDateInMillis());
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    private void registerNextDayAlarm(Context context, AlarmData data) {
        AlarmSetter setter = new AlarmSetter(context);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        data.setSpecificDateInMillis(calendar.getTimeInMillis());
        setter.registerAlarm(data);
        AlarmDatabaseUtil.update(context, data);
    }

    private void manageSpecificAlarm(Context context, AlarmData data) {
        Log.i(CLASS_NAME, "manageSpecificAlarm$specific alarm started");
        startNotifierActivity(context, data);
    }

    private void startNotifierActivity(Context context, AlarmData data) {
        WakeLockUtil.acquireWakeLock(context);
        Intent intent = new Intent(context, AlarmNotifierActivity.class);
        intent.putExtra("alarmData", data);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
