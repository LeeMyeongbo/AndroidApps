package com.alarm.newsalarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alarm.newsalarm.alarmlist.AlarmlistAdapter;
import com.alarm.newsalarm.alarmlist.ItemDragListener;
import com.alarm.newsalarm.alarmlist.ItemTouchHelperCallback;
import com.alarm.newsalarm.newsmanager.NewsNotification;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private AlarmManager alarmManager;
    private ItemTouchHelper helper;
    private final ItemDragListener listenerImpl = viewHolder -> helper.startDrag(viewHolder);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (!alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
        MaterialButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, AlarmSetterActivity.class);
            startActivity(intent);
        });

        ArrayList<Pair<String, Boolean>> dataset = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dataset.add(new Pair<>("0" + i + ":00", i % 2 == 0));
        }

        AlarmlistAdapter adapter = new AlarmlistAdapter(dataset, listenerImpl);
        RecyclerView lvAlarm = findViewById(R.id.alarmList);

        lvAlarm.setLayoutManager(new LinearLayoutManager(this));
        lvAlarm.setAdapter(adapter);

        helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        helper.attachToRecyclerView(lvAlarm);
    }

    private void startAlarm(Calendar c) {
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("key", "alarm");
        intent.setAction("timer");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE
        );

        if (c.before((Calendar.getInstance())))
            c.add(Calendar.DATE, 1);

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent
        );
    }

    @SuppressLint("SetTextI18n")
    private void cancelAlarm() {
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 1, intent, PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        NewsNotification.getInstance().destroyTTS();
    }
}
