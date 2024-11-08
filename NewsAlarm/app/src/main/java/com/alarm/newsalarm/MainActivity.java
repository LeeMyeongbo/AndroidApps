package com.alarm.newsalarm;

import android.app.AlarmManager;
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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView lvAlarmList;
    private AlarmManager alarmManager;
    private ItemTouchHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        initUI();
        requestExactAlarmPermission();
        initAlarmListView(getStoredAlarmList());
    }

    private void initUI() {
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> { /* TO DO : Google Drawer */ });
        MaterialButton btnAdd = findViewById(R.id.btnAdd);
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
        /* TO DO : load alarm list data from Room DB and show them into alarmListView
           Below is temporary alarm list ^^ */
        ArrayList<Pair<String, Boolean>> dataset = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dataset.add(new Pair<>("0" + i + ":00", i % 2 == 0));
        }
        return dataset;
    }

    private void initAlarmListView(ArrayList<Pair<String, Boolean>> dataset) {
        AlarmlistAdapter adapter = new AlarmlistAdapter(
            dataset,
            (view, position) -> executeAlarmSetterActivityWithData(position),
            viewHolder -> helper.startDrag(viewHolder)
        );
        lvAlarmList.setLayoutManager(new LinearLayoutManager(this));
        lvAlarmList.setAdapter(adapter);

        helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        helper.attachToRecyclerView(lvAlarmList);
    }

    private void executeAlarmSetterActivityWithData(int position) {
        /* TO DO : start SetterActivity with alarm data located in the 'position' */
        Toast.makeText(this, "clicked : " + (position + 1) + "번 째", Toast.LENGTH_SHORT).show();
        executeAlarmSetterActivity();
    }
}
