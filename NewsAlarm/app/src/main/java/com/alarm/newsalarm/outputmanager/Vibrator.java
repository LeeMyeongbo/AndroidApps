package com.alarm.newsalarm.outputmanager;

import android.content.Context;
import android.os.CombinedVibration;
import android.os.VibrationEffect;
import android.os.VibratorManager;

public class Vibrator {

    private final VibratorManager vibrator;

    public Vibrator(Context context) {
        vibrator = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
    }

    public void vibrateOnce(int amplitude) {
        if (amplitude == 0) {
            return;
        }
        VibrationEffect effect = VibrationEffect.createOneShot(500L, amplitude);
        vibrator.vibrate(CombinedVibration.createParallel(effect));
    }

    public void cancel() {
        vibrator.cancel();
    }
}
