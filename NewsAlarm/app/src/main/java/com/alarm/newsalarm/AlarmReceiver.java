package com.alarm.newsalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alarm.newsalarm.utils.AlarmHelperUtil;
import com.alarm.newsalarm.alarmmanager.AlarmSetter;
import com.alarm.newsalarm.utils.LogUtil;
import com.alarm.newsalarm.utils.WakeLockUtil;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "AlarmReceiver";

    private Context context;
    private AlarmSetter alarmSetter;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        alarmSetter = new AlarmSetter(context);

        String action = intent.getAction();
        if (action == null) {
            LogUtil.logE(CLASS_NAME, "onReceive", "action is null");
            return;
        }

        LogUtil.logI(CLASS_NAME, "onReceive", "action : " + action);
        switch (action) {
            case Intent.ACTION_LOCKED_BOOT_COMPLETED -> manageAlarms();
            case "com.alarm.newsalarm.ACTION_ALARM_GOES_OFF" -> processAlarm(intent);
        }
    }

    private void manageAlarms() {
        List<AlarmData> alarmDataList = AlarmDatabaseUtil.getAll(context);
        for (AlarmData data : alarmDataList) {
            if (!isAlarmReregisterable(data)) {
                continue;
            }
            if (data.getPeriodicWeekBit() == 0) {
                registerOneTimeAlarmAgain(data);
            } else {
                registerPeriodicAlarmAgain(data);
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
        LogUtil.logI(CLASS_NAME, "deactivateInvalidAlarm", "deactivate alarm " + data.getId());
        data.setActive(false);
        AlarmDatabaseUtil.update(context, data);
    }

    private void registerOneTimeAlarmAgain(AlarmData data) {
        LogUtil.logI(CLASS_NAME, "registerOneTimeAlarmAgain", "register alarm " + data.getId());
        alarmSetter.registerAlarm(data);
    }

    private void registerPeriodicAlarmAgain(AlarmData data) {
        LogUtil.logI(CLASS_NAME, "registerPeriodicAlarmAgain", "register alarm " + data.getId());
        long properDateTime = AlarmHelperUtil.getProperAlarmDate(data.getSpecificDateInMillis());
        data.setSpecificDateInMillis(properDateTime);
        alarmSetter.registerAlarm(data);
        AlarmDatabaseUtil.update(context, data);
    }

    private void processAlarm(Intent intent) {
        AlarmData data = intent.getParcelableExtra("alarmData", AlarmData.class);
        if (data == null) {
            LogUtil.logE(CLASS_NAME, "processAlarm", "alarm data is null");
            return;
        }
        if (data.getPeriodicWeekBit() == 0) {
            processOneTimeAlarm(data);
        } else {
            processPeriodicAlarm(data);
        }
    }

    private void processOneTimeAlarm(AlarmData data) {
        LogUtil.logI(CLASS_NAME, "processOneTimeAlarm", "start one-time alarm " + data.getId());
        startNotifierActivity(data);
        data.setActive(false);
        AlarmDatabaseUtil.update(context, data);
    }

    private void processPeriodicAlarm(AlarmData data) {
        if (AlarmHelperUtil.isProperWeekdayInPeriodic(data)) {
            LogUtil.logI(CLASS_NAME, "processPeriodicAlarm",
                "start periodic alarm " + data.getId());
            startNotifierActivity(data);
        } else {
            LogUtil.logW(CLASS_NAME, "processPeriodicAlarm",
                "alarm " + data.getId() + " is not today");
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
        LogUtil.logI(CLASS_NAME, "startNotifierActivity", "another alarm already arrived "
            + "and started notifier -> alarm " + data.getId() + " dismissed");
    }
}
