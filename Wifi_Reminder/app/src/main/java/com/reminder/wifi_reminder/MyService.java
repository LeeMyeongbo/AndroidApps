package com.reminder.wifi_reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.ExecutionException;

public class MyService extends Service {

    private NotificationCompat.BigTextStyle bigText;
    private NotificationCompat.Builder notification;
    private NotificationManager mNotificationManager;
    private Check_which_Wifi_Thread wifi_thread;
    private Routine_Database rd;
    private Routine_Table rt;

    public MyService() { }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        rd = Routine_Database.getRoutineDatabase(getApplicationContext());
        rt = new Routine_Table("", "", "", 0, 0, 0, "", "", "");
        Intent intent1 = new Intent(getApplicationContext(), Preview.class);
        intent1.setAction(Intent.ACTION_MAIN);
        intent1.addCategory(Intent.CATEGORY_LAUNCHER);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent1, PendingIntent.FLAG_IMMUTABLE
        );

        mNotificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        NotificationChannel channel = new NotificationChannel("ch1", "Wifi Reminder", NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager.createNotificationChannel(channel);
        bigText = new NotificationCompat.BigTextStyle();
        notification = new NotificationCompat.Builder(getApplicationContext(), "ch1")
                .setNotificationSilent()
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("루틴 정보")
                .setContentIntent(pendingIntent);
        mNotificationManager.notify(1, notification.build());
        startForeground(1, notification.build());

        wifi_thread = new Check_which_Wifi_Thread();
        wifi_thread.start();

        return START_STICKY;
    }

    private class Check_which_Wifi_Thread extends Thread {

        @Override
        public void run() {
            AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
            WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            SharedPreferences sp = getSharedPreferences("test", MODE_PRIVATE);

            boolean cur = false, pre = false, changed = false;
            int cur_sound_mode = 0, cur_volume = 0, cur_gamma = 0;
            int pre_sound_mode = audioManager.getRingerMode();
            int pre_volume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            int pre_gamma = getGamma(Settings.System.SCREEN_BRIGHTNESS);
            int before_sound_mode = 0, before_volume = 0, before_gamma = 0;

            while (true) {
                String info = "현재 연결된 wifi에 설정된 루틴이 없습니다.";
                String name = "저장 정보 없음";
                String todo = "";
                String mode;
                try {
                    if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                        rt.BSSID = wifiManager.getConnectionInfo().getBSSID();
                        Routine_Table Rt = new DaoAsyncTask(rd.routine_dao(), "SELECT")
                                .execute(rt)
                                .get()
                                .get(0);
                        if (Rt != null) {
                            if (Rt.sound_mode == 0)
                                mode = "무음";
                            else if (Rt.sound_mode == 1)
                                mode = "진동";
                            else
                                mode = "소리";
                            info = "소리 모드 : " + mode + "\n볼륨 : " + Rt.volume + "\n밝기 : " + Rt.gamma + "\nwifi 명 : " + Rt.SSID
                                 + "\n할 일 : " + Rt.Todo_list;
                            name = "활성화된 루틴 : " + Rt.name;
                            cur = true;
                            cur_sound_mode = Rt.sound_mode;
                            cur_volume = Rt.volume;
                            cur_gamma = Rt.gamma;
                            todo = Rt.Todo_list;

                            changed = cur_sound_mode != before_sound_mode || cur_volume != before_volume || cur_gamma != before_gamma;
                        } else
                            cur = false;
                    } else {
                        info = "wifi를 켜야 정상적으로 이용이 가능합니다.";
                        name = "wifi 꺼짐";
                        cur = false;
                    }
                    if (cur && !pre || changed) {
                        if (getGamma(Settings.System.SCREEN_BRIGHTNESS_MODE) == 0)
                            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, cur_gamma);
                        if (cur_sound_mode != AudioManager.RINGER_MODE_SILENT)
                            audioManager.setRingerMode(cur_sound_mode);
                        else
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, cur_volume, 0);
                    } else if (!cur && pre) {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, pre_gamma);
                        audioManager.setRingerMode(pre_sound_mode);
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, pre_volume, 0);
                    } else if (!cur) {
                        pre_sound_mode = audioManager.getRingerMode();
                        pre_volume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                        pre_gamma = getGamma(Settings.System.SCREEN_BRIGHTNESS);
                        sp.edit().putBoolean("not_show_again", false).apply();
                    }

                    PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                    boolean msg_show = sp.getBoolean("msg_show", false);
                    boolean show_again = sp.getBoolean("not_show_again", false);
                    if (cur && msg_show && !pm.isInteractive() && !show_again) {
                        Intent intent = new Intent(getApplicationContext(), Notify_ToDo.class)
                                .putExtra("todo_list", todo)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                bigText = bigText.setBigContentTitle(name).bigText(info);
                notification.setStyle(bigText);
                notification.setContentText(name);
                mNotificationManager.notify(1, notification.build());
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, pre_gamma);
                    audioManager.setRingerMode(pre_sound_mode);
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, pre_volume, 0);
                    break;
                }
                if (cur_sound_mode == AudioManager.RINGER_MODE_SILENT && (cur && !pre || changed))
                    audioManager.setRingerMode(cur_sound_mode);

                pre = cur;
                before_gamma = cur_gamma;
                before_sound_mode = cur_sound_mode;
                before_volume = cur_volume;
            }
        }
    }

    private int getGamma(final String want) {
        int val = 0;
        try {
            val = Settings.System.getInt(getContentResolver(), want);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return val;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wifi_thread.interrupt();
        stopForeground(true);
    }
}
