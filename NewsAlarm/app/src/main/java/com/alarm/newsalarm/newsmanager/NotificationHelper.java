package com.alarm.newsalarm.newsmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;

import com.alarm.newsalarm.R;

public class NotificationHelper extends ContextWrapper {

    public static final String channel1ID = "channel1ID";
    public static final String channel1Name = "channel 1 ";
    public static final String channel2ID = "channel2ID";
    public static final String channel2Name = "channel 2 ";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);

        createChannels();
    }

    public void createChannels() {
        NotificationChannel channel1 =new NotificationChannel(
            channel1ID,channel1Name, NotificationManager.IMPORTANCE_DEFAULT
        );
        channel1.enableLights(true);
        channel1.enableVibration(true);
        channel1.setLightColor(R.color.purple_200);
        channel1.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel1);

        NotificationChannel channel2 = new NotificationChannel(
            channel2ID,channel2Name, NotificationManager.IMPORTANCE_DEFAULT
        );
        channel2.enableLights(true);
        channel2.enableVibration(true);
        channel2.setLightColor(R.color.purple_200);
        channel2.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel2);
    }

    public NotificationManager getManager() {
        if (mManager == null)
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        return mManager;
    }
}
