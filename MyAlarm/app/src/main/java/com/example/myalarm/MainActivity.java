package com.example.myalarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView mTextView =  findViewById(R.id.textView);
        ImageView button = findViewById(R.id.button_add);
        button.setOnClickListener(v -> {
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "time picker");
        });

        Button buttonCancelAlarm = findViewById(R.id.button_delete);
        buttonCancelAlarm.setOnClickListener(v -> cancelAlarm());

        ArrayList<Pair<String, Boolean>> dataset = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            dataset.add(new Pair<>("0" + i + ":00", i % 2 == 0));        // 테스트용!!

        RecyclerView alarm_listview = findViewById(R.id.alarm_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        alarm_listview.setLayoutManager(linearLayoutManager);

        AlarmlistAdapter adapter = new AlarmlistAdapter(dataset);
        alarm_listview.setAdapter(adapter);

        if (dataset.size() == 0) {
            mTextView.setVisibility(View.VISIBLE);
            alarm_listview.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        startAlarm(c);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("key", "alarm");
        intent.setAction("timer");
        PendingIntent pendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            pendingIntent = PendingIntent.getBroadcast(this, (int)System.currentTimeMillis(), intent,
                PendingIntent.FLAG_IMMUTABLE);
        else
            pendingIntent = PendingIntent.getBroadcast(this, (int)System.currentTimeMillis(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (c.before((Calendar.getInstance())))
            c.add(Calendar.DATE, 1);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        // alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 1*60*1000 ,  pendingIntent);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);
        NewsNotification.getInstance().destroyTTS();
    }
}
