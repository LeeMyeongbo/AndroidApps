package com.alarm.newsalarm;

import android.app.KeyguardManager;
import android.os.Bundle;
import android.view.WindowManager;

import com.alarm.newsalarm.alarmmanager.WakeLockUtil;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.newsmanager.NewsNotifier;
import com.google.android.material.button.MaterialButton;

public class AlarmNotifierActivity extends BaseActivity {

    public AlarmNotifierActivity() {
        super("AlarmNotifierActivity");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_notifier);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        NewsNotifier.getInstance().notifyNews(
            this, getIntent().getParcelableExtra("alarmData", AlarmData.class)
        );
        init();
    }

    private void init() {
        MaterialButton btnReleaseAlarm = findViewById(R.id.btnReleaseAlarm);
        btnReleaseAlarm.setOnClickListener(v -> finish());

        setShowWhenLocked(true);
        setTurnScreenOn(true);
        KeyguardManager manager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        manager.requestDismissKeyguard(this, null);
    }

    @Override
    protected void onDestroy() {
        NewsNotifier.getInstance().destroyTTS();
        WakeLockUtil.releaseWakeLock();
        super.onDestroy();
    }
}
