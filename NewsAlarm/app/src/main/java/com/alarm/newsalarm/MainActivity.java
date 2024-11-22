package com.alarm.newsalarm;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends BaseActivity {

    private SharedPreferences sharedPref;
    private RecyclerView lvAlarmList;
    private AlarmManager alarmManager;
    private AlarmlistAdapter adapter;
    private ItemTouchHelper helper;
    private final LinkedList<AlarmData> alarmDataList = new LinkedList<>();
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
        lvAlarmList.scrollToPosition(0);
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
        sharedPref = getSharedPreferences("id_pref", Context.MODE_PRIVATE);

        initUI();
        requestExactAlarmPermission();
        requestOverlayPermission();
        prepareAlarmData();
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

    private void requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
            );
            launcher.launch(intent);
        }
    }

    private void prepareAlarmData() {
        String[] ids = sharedPref.getString("id_order", "").split(",");
        ArrayList<AlarmData> loadedData = new ArrayList<>(AlarmDatabaseUtil.getAll(this));
        for (int i = 0; i < loadedData.size(); i++) {
            alarmDataList.add(loadedData.get(lowerBound(loadedData, Long.parseLong(ids[i]))));
        }
    }

    private int lowerBound(List<AlarmData> data, long id) {
        int max = data.size(), min = 0;
        while (min < max) {
            int mid = (min + max) / 2;
            if (id > data.get(mid).getId()) {
                min = mid + 1;
            } else {
                max = mid;
            }
        }
        return min;
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
    protected void onResume() {
        super.onResume();
        removeInvalidAlarms();
    }

    private void removeInvalidAlarms() {
        for (int i = alarmDataList.size() - 1; i >= 0; i--) {
            AlarmData curData = alarmDataList.get(i);
            if (curData.getPeriodicWeekBit() == 0
                    && curData.getSpecificDateInMillis() < System.currentTimeMillis()) {
                alarmDataList.remove(i);
                AlarmDatabaseUtil.delete(this, curData);
            }
        }
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

    @Override
    protected void onDestroy() {
        storeIdOrder();
        super.onDestroy();
    }

    private void storeIdOrder() {
        StringBuilder idOrder = new StringBuilder();
        for (AlarmData data : alarmDataList) {
            idOrder.append(data.getId()).append(",");
        }
        if (idOrder.length() > 0) {
            idOrder.deleteCharAt(idOrder.lastIndexOf(","));
        }
        sharedPref.edit().putString("id_order", idOrder.toString()).apply();
    }
}
