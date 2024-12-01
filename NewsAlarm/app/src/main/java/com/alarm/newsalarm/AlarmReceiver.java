package com.alarm.newsalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alarm.newsalarm.alarmmanager.AlarmHelperUtil;
import com.alarm.newsalarm.alarmmanager.AlarmSetter;
import com.alarm.newsalarm.alarmmanager.WakeLockUtil;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;

import java.util.List;
import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "AlarmReceiver";

    private Context context;
    private AlarmSetter alarmSetter;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        alarmSetter = new AlarmSetter(context);

        String action = Objects.requireNonNull(intent.getAction());
        Log.i(CLASS_NAME, "onReceive$action : " + action);

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED -> manageAlarms();
            case "com.alarm.newsalarm.ACTION_ALARM_GOES_OFF" -> processAlarm(intent);
        }
    }

    private void manageAlarms() {
        List<AlarmData> alarmDataList = AlarmDatabaseUtil.getAll(context);
        for (AlarmData data : alarmDataList) {
            if (isAlarmReregisterable(data)) {
                registerAlarmAgain(data);
            }
        }
    }

    private boolean isAlarmReregisterable(AlarmData data) {
        if (!data.isActive()) {
            return false;
        }
        if (data.getSpecificDateInMillis() <= System.currentTimeMillis()
                && data.getPeriodicWeekBit() == 0) {
            deactivateInvalidAlarm(data);
            return false;
        }
        return true;
    }

    private void deactivateInvalidAlarm(AlarmData data) {
        Log.i(CLASS_NAME, "deactivateInvalidAlarm$specific alarm " + data.getId());
        data.setActive(false);
        AlarmDatabaseUtil.update(context, data);
    }

    private void registerAlarmAgain(AlarmData data) {
        if (data.getPeriodicWeekBit() == 0) {
            Log.i(CLASS_NAME, "registerAlarmAgain$register specific date alarm " + data.getId());
            alarmSetter.registerAlarm(data);
            return;
        }
        registerPeriodicAlarmAgain(data);
    }

    private void registerPeriodicAlarmAgain(AlarmData data) {
        Log.i(CLASS_NAME, "registerPeriodicAlarmAgain$register periodic alarm " + data.getId());
        long properDateTime = AlarmHelperUtil.getProperAlarmDate(data.getSpecificDateInMillis());
        data.setSpecificDateInMillis(properDateTime);
        alarmSetter.registerAlarm(data);
        AlarmDatabaseUtil.update(context, data);
    }

    private void processAlarm(Intent intent) {
        AlarmData data = Objects.requireNonNull(
            intent.getParcelableExtra("alarmData", AlarmData.class));
        Log.i(CLASS_NAME, "processAlarm$alarm " + data.getId() + " arrived!");
        if (data.getPeriodicWeekBit() == 0) {
            processSpecificAlarm(data);
        } else {
            processPeriodicAlarm(data);
        }
    }

    private void processSpecificAlarm(AlarmData data) {
        Log.i(CLASS_NAME, "processSpecificAlarm$start specific alarm " + data.getId());
        startNotifierActivity(data);
        data.setActive(false);
        AlarmDatabaseUtil.update(context, data);
    }

    private void processPeriodicAlarm(AlarmData data) {
        if (AlarmHelperUtil.isProperWeekdayInPeriodic(data)) {
            Log.i(CLASS_NAME, "processPeriodicAlarm$start periodic alarm " + data.getId());
            startNotifierActivity(data);
        } else {
            Log.i(CLASS_NAME, "processPeriodicAlarm$alarm " + data.getId() + " is not today");
        }
        registerPeriodicAlarmAgain(data);
    }

    private void startNotifierActivity(AlarmData data) {
        if (WakeLockUtil.acquireWakeLock(context)) {
            Intent intent = new Intent(context, AlarmNotifierActivity.class);
            intent.putExtra("alarmData", data);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }
        Log.i(CLASS_NAME, "startNotifierActivity$another alarm already acquired wakelock "
            + "and started notifier.. so alarm " + data.getId() + " dismissed");
    }
}
