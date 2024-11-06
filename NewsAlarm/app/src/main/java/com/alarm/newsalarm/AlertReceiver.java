package com.alarm.newsalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NewsNotification.getInstance().notifyNews(context);
    }
}
