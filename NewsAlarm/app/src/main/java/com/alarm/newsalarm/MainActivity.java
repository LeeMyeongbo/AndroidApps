package com.alarm.newsalarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Pair;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alarm.newsalarm.alarmlist.AlarmlistAdapter;
import com.alarm.newsalarm.alarmlist.ItemTouchHelperCallback;
import com.alarm.newsalarm.newsmanager.NewsNotification;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnMenu;
    private MaterialButton btnAdd;
    private RecyclerView lvAlarmList;
    private AlarmManager alarmManager;
    private ItemTouchHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        initUI();
        requestExactAlarmPermission();
        initAlarmListView(getStoredAlarmList());
    }

    private void initUI() {
        btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> { /* TO DO */ });
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> executeAlarmSetterActivity());
        lvAlarmList = findViewById(R.id.alarmList);
    }

    private void executeAlarmSetterActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AlarmSetterActivity.class);
        startActivity(intent);
    }

    private void requestExactAlarmPermission() {
        if (!alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }

    @NonNull
    private ArrayList<Pair<String, Boolean>> getStoredAlarmList() {
        ArrayList<Pair<String, Boolean>> dataset = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dataset.add(new Pair<>("0" + i + ":00", i % 2 == 0));
        }
        return dataset;
    }

    private void initAlarmListView(ArrayList<Pair<String, Boolean>> dataset) {
        AlarmlistAdapter adapter = new AlarmlistAdapter(
            dataset,
            (view, position) -> {
                executeAlarmSetterActivity();
                Toast.makeText(this, "clicked : " + (position + 1) + "번 째", Toast.LENGTH_SHORT)
                    .show();
            },
            viewHolder -> helper.startDrag(viewHolder)
        );
        lvAlarmList.setLayoutManager(new LinearLayoutManager(this));
        lvAlarmList.setAdapter(adapter);

        helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        helper.attachToRecyclerView(lvAlarmList);
    }

    @SuppressLint("MissingPermission")
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
