package com.alarm.newsalarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
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

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

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
        TextView mTextView =  findViewById(R.id.tvNoAlarm);
        MaterialButton button = findViewById(R.id.btnAdd);
        button.setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        ArrayList<Pair<String, Boolean>> dataset = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            dataset.add(new Pair<>("0" + i + ":00", i % 2 == 0));

        AlarmlistAdapter adapter = new AlarmlistAdapter(dataset, listenerImpl);
        RecyclerView alarm_listview = findViewById(R.id.alarmList);

        alarm_listview.setLayoutManager(new LinearLayoutManager(this));
        alarm_listview.setAdapter(adapter);

        helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        helper.attachToRecyclerView(alarm_listview);

        if (dataset.isEmpty()) {
            mTextView.setVisibility(View.VISIBLE);
            alarm_listview.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        startAlarm(c);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
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
