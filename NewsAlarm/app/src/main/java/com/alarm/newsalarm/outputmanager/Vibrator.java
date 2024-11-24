package com.alarm.newsalarm.outputmanager;

import android.content.Context;
import android.os.CombinedVibration;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.VibratorManager;
import android.util.Log;

import androidx.annotation.NonNull;

public class Vibrator {

    private static final String CLASS_NAME = "Vibrator";

    private final VibratorManager vibrator;
    private final int MSG_VIBRATE = 0x01;
    private final Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            VibrationEffect effect = VibrationEffect.createOneShot(1000, msg.arg1);
            vibrator.vibrate(CombinedVibration.createParallel(effect));
            sendMessageAgain(msg.arg1);
        }

        private void sendMessageAgain(int amplitude) {
            Message message = obtainMessage();
            message.what = MSG_VIBRATE;
            message.arg1 = amplitude;
            sendMessageDelayed(message, 2000);
        }
    };

    public Vibrator(Context context) {
        vibrator = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
    }

    public void vibrateOnce(int amplitude) {
        if (amplitude == 0) {
            Log.i(CLASS_NAME, "vibrateOnce$amplitude : 0");
            return;
        }
        Log.i(CLASS_NAME, "vibrateOnce$amplitude : " + amplitude);
        VibrationEffect effect = VibrationEffect.createOneShot(500, amplitude);
        vibrator.vibrate(CombinedVibration.createParallel(effect));
    }

    public void vibrateRepeatedly(int amplitude) {
        if (amplitude == 0) {
            Log.i(CLASS_NAME, "vibrateRepeatedly$amplitude : 0");
            return;
        }
        Message msg = handler.obtainMessage();
        msg.what = MSG_VIBRATE;
        msg.arg1 = amplitude;
        handler.sendMessageDelayed(msg, 1000);
        Log.i(CLASS_NAME, "vibrateRepeatedly$amplitude : " + amplitude);
    }

    public void release() {
        handler.removeMessages(MSG_VIBRATE);
        vibrator.cancel();
        Log.i(CLASS_NAME, "release$Vibrator released completely!");
    }
}
