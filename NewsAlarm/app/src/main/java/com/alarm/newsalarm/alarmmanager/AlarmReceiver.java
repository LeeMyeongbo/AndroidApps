package com.alarm.newsalarm.alarmmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alarm.newsalarm.AlarmNotifierActivity;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLockUtil.acquireWakeLock(context);
        Log.i(CLASS_NAME, "onReceive$alarm intent received!");

        long curId = intent.getLongExtra("periodicId", 0L);
        if (curId > 0L) {
            /* To Do : load alarm data with id from room db and register alarm again */
        }
        Intent notifierIntent = new Intent(context, AlarmNotifierActivity.class);
        notifierIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(notifierIntent);
    }
}
