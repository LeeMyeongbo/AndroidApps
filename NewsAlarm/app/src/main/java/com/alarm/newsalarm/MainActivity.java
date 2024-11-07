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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    private AlarmManager alarmManager;

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
        ImageView button = findViewById(R.id.btnAdd);
        button.setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        Button buttonCancelAlarm = findViewById(R.id.btnDelete);
        buttonCancelAlarm.setOnClickListener(v -> cancelAlarm());

        ArrayList<Pair<String, Boolean>> dataset = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            dataset.add(new Pair<>("0" + i + ":00", i % 2 == 0));

        RecyclerView alarm_listview = findViewById(R.id.AlarmList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        alarm_listview.setLayoutManager(linearLayoutManager);

        AlarmlistAdapter adapter = new AlarmlistAdapter(dataset);
        alarm_listview.setAdapter(adapter);

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
