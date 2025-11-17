package com.alarm.newsalarmkt.utils

import android.util.Log

object LogUtil {
    private const val TAG = "NewsAlarm"

    private fun log(
        className: String, methodName: String,
        message: String, logLevel: LogLevel
    ) {
        val logMsg = "[$className$$methodName] $message"

        when (logLevel) {
            LogLevel.DEBUG -> Log.d(TAG, logMsg)
            LogLevel.INFO -> Log.i(TAG, logMsg)
            LogLevel.WARN -> Log.w(TAG, logMsg)
            LogLevel.ERROR -> Log.e(TAG, logMsg)
        }
    }

    @JvmStatic
    fun logD(className: String, methodName: String, message: String) {
        log(className, methodName, message, LogLevel.DEBUG)
    }

    @JvmStatic
    fun logI(className: String, methodName: String, message: String) {
        log(className, methodName, message, LogLevel.INFO)
    }

    @JvmStatic
    fun logW(className: String, methodName: String, message: String) {
        log(className, methodName, message, LogLevel.WARN)
    }

    @JvmStatic
    fun logE(className: String, methodName: String, message: String) {
        log(className, methodName, message, LogLevel.ERROR)
    }

    @JvmStatic
    fun logE(className: String, methodName: String, e: Exception) {
        val stackTrace = e.stackTrace
        for (element in stackTrace) {
            log(className, methodName, element.toString(), LogLevel.ERROR)
        }
    }

    private enum class LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}
