package com.reminder.wifi_reminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Notify_ToDo extends AppCompatActivity {

    private SharedPreferences sp;
    PowerManager.WakeLock wake;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_todo);

        sp = getSharedPreferences("test", MODE_PRIVATE);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "notify:todo");
        ImageView close = findViewById(R.id.close_this);

        TextView todo = findViewById(R.id.todo_lists);
        String content = getIntent().getStringExtra("todo_list");
        todo.setText(content);

        wake.acquire(4000);
        close.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sp.edit().putBoolean("not_show_again", true).apply();
        wake.release();
        finish();
    }
}
