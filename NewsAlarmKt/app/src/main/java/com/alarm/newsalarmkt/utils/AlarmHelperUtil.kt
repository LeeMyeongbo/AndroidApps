package com.alarm.newsalarmkt.utils

import com.alarm.newsalarmkt.database.AlarmData
import java.time.LocalDate
import java.util.Calendar

object AlarmHelperUtil {

    private const val CLASS_NAME = "AlarmHelperUtil"
    private val calendar: Calendar = Calendar.getInstance()

    @JvmStatic
    fun isProperWeekdayInPeriodic(data: AlarmData): Boolean {
        calendar.setTimeInMillis(System.currentTimeMillis())
        val curWeekday = calendar.get(Calendar.DAY_OF_WEEK) - 1

        return (data.periodicWeekBit and (1 shl curWeekday)) > 0
    }

    @JvmStatic
    fun getProperAlarmDate(dateTimeMillis: Long): Long {
        calendar.setTimeInMillis(dateTimeMillis)
        setCalendarDateToToday()
        addOneDayIfTimePassed()
        return calendar.getTimeInMillis()
    }

    private fun setCalendarDateToToday() {
        val localDate = LocalDate.now()
        calendar.set(Calendar.YEAR, localDate.year)
        calendar.set(Calendar.MONTH, localDate.monthValue - 1)
        calendar.set(Calendar.DAY_OF_MONTH, localDate.dayOfMonth)
    }

    private fun addOneDayIfTimePassed() {
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            LogUtil.logD(CLASS_NAME, "addOneDayIfTimePassed", "date expired -> add 1 day")
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}
