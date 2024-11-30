package com.alarm.newsalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alarm.newsalarm.alarmmanager.AlarmSetter;
import com.alarm.newsalarm.alarmmanager.WakeLockUtil;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "AlarmReceiver";
    private final Calendar calendar = Calendar.getInstance();
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
            if (removeInvalidAlarm(data)) {
                continue;
            }
            if (data.isActive()) {
                registerAlarmAgain(data);
            }
        }
    }

    private boolean removeInvalidAlarm(AlarmData data) {
        if (data.getPeriodicWeekBit() > 0) {
            return false;
        }
        if (data.getSpecificDateInMillis() > System.currentTimeMillis()) {
            return false;
        }
        Log.i(CLASS_NAME, "removeInvalidAlarm$remove outdated specific alarm : " + data.getId());
        AlarmDatabaseUtil.delete(context, data);
        return true;
    }

    private void registerAlarmAgain(AlarmData data) {
        if (data.getPeriodicWeekBit() == 0) {
            Log.i(CLASS_NAME, "registerAlarmAgain$register specific date alarm : " + data.getId());
            alarmSetter.registerAlarm(data);
            return;
        }
        registerPeriodicAlarmAgain(data);
    }

    private void registerPeriodicAlarmAgain(AlarmData data) {
        Log.i(CLASS_NAME, "registerPeriodicAlarmAgain$register periodic alarm : " + data.getId());
        setAlarmDateToToday(data);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            registerNextDayAlarm(data);
        } else {
            alarmSetter.registerAlarm(data);
            AlarmDatabaseUtil.update(context, data);
        }
    }

    private void setAlarmDateToToday(AlarmData data) {
        Log.i(CLASS_NAME, "setAlarmDateToToday$only set date, not including time..");
        LocalDate today = LocalDate.now();
        calendar.setTimeInMillis(data.getSpecificDateInMillis());
        calendar.set(Calendar.YEAR, today.getYear());
        calendar.set(Calendar.MONTH, today.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, today.getDayOfMonth());
        data.setSpecificDateInMillis(calendar.getTimeInMillis());
    }

    private void processAlarm(Intent intent) {
        AlarmData data = Objects.requireNonNull(
            intent.getParcelableExtra("alarmData", AlarmData.class));
        Log.i(CLASS_NAME, "processAlarm$alarm " + data.getId() + " arrived!");
        if (data.getPeriodicWeekBit() > 0) {
            processPeriodicAlarm(data);
        } else {
            processSpecificAlarm(data);
        }
    }

    private void processPeriodicAlarm(AlarmData data) {
        if ((data.getPeriodicWeekBit() & (1 << getCurWeekDay(data))) > 0) {
            Log.i(CLASS_NAME, "processPeriodicAlarm$start periodic alarm : " + data.getId());
            startNotifierActivity(data);
        }
        registerNextDayAlarm(data);
    }

    private int getCurWeekDay(AlarmData data) {
        calendar.setTimeInMillis(data.getSpecificDateInMillis());
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    private void registerNextDayAlarm(AlarmData data) {
        Log.i(CLASS_NAME, "registerNextDayAlarm$for periodic alarm : " + data.getId());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        data.setSpecificDateInMillis(calendar.getTimeInMillis());
        alarmSetter.registerAlarm(data);
        AlarmDatabaseUtil.update(context, data);
    }

    private void processSpecificAlarm(AlarmData data) {
        Log.i(CLASS_NAME, "processSpecificAlarm$start specific alarm : " + data.getId());
        startNotifierActivity(data);
    }

    private void startNotifierActivity(AlarmData data) {
        if (WakeLockUtil.acquireWakeLock(context)) {
            Log.i(CLASS_NAME, "startNotifierActivity$start notifier");
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
