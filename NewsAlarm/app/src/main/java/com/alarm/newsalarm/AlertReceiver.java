package com.alarm.newsalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alarm.newsalarm.newsmanager.NewsNotification;

public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /* TO DO : Start Alarm Service to run alarm */
        NewsNotification.getInstance().notifyNews(context);
    }
}
