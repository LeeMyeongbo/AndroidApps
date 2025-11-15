package com.alarm.newsalarm.utils;

import android.util.Log;

import androidx.annotation.NonNull;

public class LogUtil {

    private enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    private static final String TAG = "NewsAlarm";

    private LogUtil() {
    }

    private static void log(@NonNull String className, @NonNull String methodName,
            @NonNull String message, @NonNull LogLevel logLevel) {

        String logMsg = "[" + className + "$" + methodName + "] " + message;

        switch (logLevel) {
            case DEBUG -> Log.d(TAG, logMsg);
            case INFO -> Log.i(TAG, logMsg);
            case WARN -> Log.w(TAG, logMsg);
            case ERROR -> Log.e(TAG, logMsg);
        }
    }

    public static void logD(String className, String methodName, String message) {
        log(className, methodName, message, LogLevel.DEBUG);
    }

    public static void logI(String className, String methodName, String message) {
        log(className, methodName, message, LogLevel.INFO);
    }

    public static void logW(String className, String methodName, String message) {
        log(className, methodName, message, LogLevel.WARN);
    }

    public static void logE(String className, String methodName, String message) {
        log(className, methodName, message, LogLevel.ERROR);
    }

    public static void logE(String className, String methodName, Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            log(className, methodName, element.toString(), LogLevel.ERROR);
        }
    }
}
