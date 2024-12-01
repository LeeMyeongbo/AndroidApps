package com.alarm.newsalarm.alarmmanager;

import android.util.Log;

import com.alarm.newsalarm.database.AlarmData;

import java.time.LocalDate;
import java.util.Calendar;

public class AlarmHelperUtil {

    private static final String CLASS_NAME = "AlarmHelperUtil";
    private static final Calendar calendar = Calendar.getInstance();

    private AlarmHelperUtil() {
    }

    public static boolean isProperWeekdayInPeriodic(AlarmData data) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        int curWeekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        return (data.getPeriodicWeekBit() & (1 << curWeekday)) > 0;
    }

    public static long getProperAlarmDate(long dateTimeMillis) {
        calendar.setTimeInMillis(dateTimeMillis);
        setCalendarDateToToday();
        addOneDayIfTimePassed();
        return calendar.getTimeInMillis();
    }

    private static void setCalendarDateToToday() {
        Log.i(CLASS_NAME, "setCalendarDateToToday$load today's date");
        LocalDate localDate = LocalDate.now();
        calendar.set(Calendar.YEAR, localDate.getYear());
        calendar.set(Calendar.MONTH, localDate.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());
    }

    private static void addOneDayIfTimePassed() {
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Log.i(CLASS_NAME, "addOneDayIfTimePassed$former than current time -> add 1 day");
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            Log.i(CLASS_NAME, "addOneDayIfTimePassed$later than current time -> stay as is");
        }
    }
}
