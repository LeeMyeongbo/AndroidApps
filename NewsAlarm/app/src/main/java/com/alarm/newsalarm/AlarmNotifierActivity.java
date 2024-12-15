package com.alarm.newsalarm;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.alarm.newsalarm.alarmmanager.WakeLockUtil;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.newsmanager.NewsNotifier;
import com.google.android.material.button.MaterialButton;

public class AlarmNotifierActivity extends BaseActivity {

    private static final int FINISH_DIST = 100;

    private MaterialButton btnReleaseAlarm;
    private NewsNotifier notifier;
    private float finishDistInPx;
    private float radius, centerX, centerY, dx, dy;

    public AlarmNotifierActivity() {
        super("AlarmNotifierActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_notifier);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startNotifier();
        init();
    }

    private void startNotifier() {
        AlarmData data = getIntent().getParcelableExtra("alarmData", AlarmData.class);
        notifier = new NewsNotifier(this, data);
        notifier.start();
    }

    private void init() {
        initBtnRelease();

        setShowWhenLocked(true);
        setTurnScreenOn(true);
        KeyguardManager manager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        manager.requestDismissKeyguard(this, null);
    }

    private void initBtnRelease() {
        btnReleaseAlarm = findViewById(R.id.btnReleaseAlarm);
        btnReleaseAlarm.post(() -> {
            radius = btnReleaseAlarm.getWidth() / 2f;
            centerX = btnReleaseAlarm.getX() + radius;
            centerY = btnReleaseAlarm.getY() + radius;
            finishDistInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, FINISH_DIST,
                getResources().getDisplayMetrics());
            Log.i(CLASS_NAME, "initBtnRelease$finishDistInPx : " + finishDistInPx);
            setBtnReleaseListener();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setBtnReleaseListener() {
        btnReleaseAlarm.setOnTouchListener((view, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    dx = view.getX() - event.getRawX();
                    dy = view.getY() - event.getRawY();
                }
                case MotionEvent.ACTION_MOVE -> {
                    float x = event.getRawX() + dx;
                    float y = event.getRawY() + dy;
                    view.animate().x(x).y(y).setDuration(0).start();
                    double distance = Math.pow((x + radius - centerX) * (x + radius - centerX)
                        + (y + radius - centerY) * (y + radius - centerY), 0.5);
                    if (distance >= finishDistInPx) {
                        finish();
                    }
                }
                case MotionEvent.ACTION_UP -> {
                    view.animate().x(centerX - radius).y(centerY - radius).setDuration(0).start();
                }
            }
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        notifier.finish();
        WakeLockUtil.releaseWakeLock();
        super.onDestroy();
    }
}
