package com.alarm.newsalarm;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alarm.newsalarm.alarmlist.AlarmlistAdapter;
import com.alarm.newsalarm.alarmlist.ItemTouchHelperCallback;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;
import com.google.android.material.button.MaterialButton;

import java.util.LinkedList;
import java.util.Objects;

public class MainActivity extends BaseActivity {

    private RecyclerView lvAlarmList;
    private AlarmManager alarmManager;
    private AlarmlistAdapter adapter;
    private ItemTouchHelper helper;
    private LinkedList<AlarmData> alarmDataList;
    private long backKeyReleasedTime = -1;
    private long backKeyPressedTime = -1;
    private int clickedPos;

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() != RESULT_OK) {
                return;
            }
            appendNewAlarmData(result);
            updateExistingAlarmData(result);
        }
    );

    private void appendNewAlarmData(ActivityResult result) {
        AlarmData data = Objects.requireNonNull(result.getData())
            .getParcelableExtra("addNewAlarmData", AlarmData.class);
        if (data == null) {
            return;
        }
        adapter.addItem(data);
        lvAlarmList.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void updateExistingAlarmData(ActivityResult result) {
        AlarmData data = Objects.requireNonNull(result.getData())
            .getParcelableExtra("updateAlarmData", AlarmData.class);
        if (data == null) {
            return;
        }
        adapter.updateItem(clickedPos, data);
        lvAlarmList.scrollToPosition(clickedPos);
    }

    public MainActivity() {
        super("MainActivity");
    }

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
        alarmDataList = AlarmDatabaseUtil.getAll(this);
        initAlarmListView();
    }

    private void initUI() {
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> { /* TO DO : Google Drawer */ });
        MaterialButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> executeAlarmSetterActivity(new Intent()));
        lvAlarmList = findViewById(R.id.alarmList);
    }

    private void executeAlarmSetterActivity(Intent intent) {
        intent.setClass(this, AlarmSetterActivity.class);
        launcher.launch(intent);
    }

    private void requestExactAlarmPermission() {
        if (!alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }

    private void initAlarmListView() {
        adapter = new AlarmlistAdapter(
            alarmDataList,
            (view, position) -> executeAlarmSetterActivityWithData(position),
            viewHolder -> helper.startDrag(viewHolder)
        );
        lvAlarmList.setLayoutManager(new LinearLayoutManager(this));
        lvAlarmList.setAdapter(adapter);

        helper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        helper.attachToRecyclerView(lvAlarmList);
    }

    private void executeAlarmSetterActivityWithData(int pos) {
        clickedPos = pos;
        Intent intent = new Intent();
        intent.putExtra("alarmData", alarmDataList.get(pos));
        executeAlarmSetterActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        backKeyPressedTime = System.currentTimeMillis();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long curBackKeyReleased = System.currentTimeMillis();
            if (isBackKeyPressedOverTwoSeconds(curBackKeyReleased)) {
                return true;
            }
            if (isBackKeyReleasedTwoTimesWithinTwoSeconds(curBackKeyReleased)) {
                finish();
                return true;
            }
            Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르시면 종료합니다.", Toast.LENGTH_SHORT).show();
            backKeyReleasedTime = curBackKeyReleased;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isBackKeyPressedOverTwoSeconds(long curBackKeyReleased) {
        return curBackKeyReleased - backKeyPressedTime > 2000;
    }

    private boolean isBackKeyReleasedTwoTimesWithinTwoSeconds(long curBackKeyReleased) {
        return curBackKeyReleased - backKeyReleasedTime < 2000;
    }
}
