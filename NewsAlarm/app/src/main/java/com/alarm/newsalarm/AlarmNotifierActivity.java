package com.alarm.newsalarm;

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
    private float finishRadius;
    private float btnRadius, centerX, centerY, dx, dy;

    public AlarmNotifierActivity() {
        super("AlarmNotifierActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_notifier);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startNotifier();
        initBtnRelease();
    }

    private void startNotifier() {
        AlarmData data = getIntent().getParcelableExtra("alarmData", AlarmData.class);
        notifier = new NewsNotifier(this, data);
        notifier.start();
    }

    private void initBtnRelease() {
        btnReleaseAlarm = findViewById(R.id.btnReleaseAlarm);
        btnReleaseAlarm.post(() -> {
            btnRadius = btnReleaseAlarm.getWidth() / 2f;
            centerX = btnReleaseAlarm.getX() + btnRadius;
            centerY = btnReleaseAlarm.getY() + btnRadius;
            finishRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, FINISH_DIST,
                getResources().getDisplayMetrics());
            Log.i(CLASS_NAME, "initBtnRelease$finishRadius length : " + finishRadius);
            setBtnReleaseListener();
        });
    }

    private void setBtnReleaseListener() {
        btnReleaseAlarm.setOnTouchListener((view, event) -> {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN -> calculateDistBetweenBtnAndTouch(event);
                case MotionEvent.ACTION_MOVE -> moveBtnWithTouch(event);
                case MotionEvent.ACTION_UP -> resetBtnWithDetach();
            }
            view.performClick();
            return true;
        });
    }

    private void calculateDistBetweenBtnAndTouch(MotionEvent event) {
        dx = btnReleaseAlarm.getX() - event.getRawX();
        dy = btnReleaseAlarm.getY() - event.getRawY();
    }

    private void moveBtnWithTouch(MotionEvent event) {
        float x = event.getRawX() + dx;
        float y = event.getRawY() + dy;
        btnReleaseAlarm.animate().x(x).y(y).setDuration(0).start();
        finishWhenBtnOutOfBoundary(x, y);
    }

    private void finishWhenBtnOutOfBoundary(float x, float y) {
        double distance = Math.pow((x + btnRadius - centerX) * (x + btnRadius - centerX)
            + (y + btnRadius - centerY) * (y + btnRadius - centerY), 0.5);
        if (distance >= finishRadius) {
            finish();
        }
    }

    private void resetBtnWithDetach() {
        btnReleaseAlarm.animate().x(centerX - btnRadius).y(centerY - btnRadius).start();
    }

    @Override
    protected void onDestroy() {
        notifier.finish();
        WakeLockUtil.releaseWakeLock();
        super.onDestroy();
    }
}
