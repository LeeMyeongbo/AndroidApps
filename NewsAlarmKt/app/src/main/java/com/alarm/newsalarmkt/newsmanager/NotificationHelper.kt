package com.alarm.newsalarmkt.newsmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import com.alarm.newsalarmkt.R

class NotificationHelper(base: Context?) : ContextWrapper(base) {

    private val manager: NotificationManager
        get() {
            return getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }

    init {
        createChannels()
    }

    fun createChannels() {
        val channel1 = NotificationChannel(
            CH1_ID, CH1_NAME, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel1.enableLights(true)
        channel1.enableVibration(true)
        channel1.lightColor = R.color.purple_200
        channel1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        this.manager.createNotificationChannel(channel1)

        val channel2 = NotificationChannel(
            CH2_ID, CH2_NAME, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel2.enableLights(true)
        channel2.enableVibration(true)
        channel2.lightColor = R.color.purple_200
        channel2.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        this.manager.createNotificationChannel(channel2)
    }



    companion object {
        const val CH1_ID: String = "channel1ID"
        const val CH1_NAME: String = "channel 1 "
        const val CH2_ID: String = "channel2ID"
        const val CH2_NAME: String = "channel 2 "
    }
}
