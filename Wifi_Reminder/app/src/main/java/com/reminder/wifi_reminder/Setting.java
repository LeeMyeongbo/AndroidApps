package com.reminder.wifi_reminder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class Setting extends AppCompatActivity {

    private SwitchMaterial notify_switch;
    private SharedPreferences sp;

    @SuppressLint("BatteryLife")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Routine_Database rd = Routine_Database.getRoutineDatabase(getApplicationContext());
        sp = getSharedPreferences("test", MODE_PRIVATE);
        boolean is_night = sp.getBoolean("is_night", false);
        if (is_night) {
            setTheme(R.style.NightTheme);
            setContentView(R.layout.setting_night);
        }
        else {
            setTheme(R.style.DayTheme);
            setContentView(R.layout.setting);
        }
        if (!Settings.canDrawOverlays(getApplicationContext()))
            sp.edit().putBoolean("msg_show", false).apply();

        SwitchMaterial dark_switch = findViewById(R.id.dark_switch);
        dark_switch.setChecked(sp.getBoolean("is_night", false));
        dark_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp.edit().putBoolean("is_night", isChecked).apply();

            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.appear_anim, R.anim.disappear_anim);
        });

        notify_switch = findViewById(R.id.msg_show_switch);
        notify_switch.setChecked(sp.getBoolean("msg_show", false));
        notify_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!Settings.canDrawOverlays(getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    Toast.makeText(this, "먼저 다른 앱 위에 표시 권한을 설정해 주세요", Toast.LENGTH_SHORT).show();
                    startActivityResult.launch(intent);
                }
                else {
                    Toast.makeText(this, "이제 화면이 꺼지면 할 일 목록이 표시됩니다.", Toast.LENGTH_SHORT).show();
                    sp.edit().putBoolean("msg_show", true).apply();
                }
            } else {
                Toast.makeText(this, "할 일 목록이 표시되지 않습니다.", Toast.LENGTH_SHORT).show();
                sp.edit().putBoolean("msg_show", false).apply();
            }
        });

        ListView listView = findViewById(R.id.listview1);
        ListViewAdapter adapter = new ListViewAdapter(is_night);
        adapter.addItem("초기화", "설정 및 저장된 루틴들을 초기화합니다");
        adapter.addItem("애플리케이션 정보", "권한, 배터리 최적화 등을 설정하거나 해제할 수 있습니다.");
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                AlertDialog.Builder msgBuilder = new AlertDialog.Builder(this)
                        .setTitle("초기화")
                        .setMessage("모든 데이터가 지워집니다. 계속하시겠습니까?")
                        .setNegativeButton("취소", null)
                        .setPositiveButton("초기화", (dialog, which) -> {
                            new DaoAsyncTask(rd.routine_dao(), "CLEAR").execute();

                            Intent intent = new Intent(getApplicationContext(), MyService.class);
                            Toast.makeText(this, "Remind 기능이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                            stopService(intent);
                            Toast.makeText(this, "초기화 완료", Toast.LENGTH_SHORT).show();

                            onBackPressed();
                        });
                AlertDialog msgDlg = msgBuilder.create();
                msgDlg.setCanceledOnTouchOutside(false);
                msgDlg.setOnShowListener(dialog -> {
                    msgDlg.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                    msgDlg.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                });
                msgDlg.show();
            } else if (position == 1) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent);
                }
            }
        });
        ListView listView2 = findViewById(R.id.listview2);
        ListViewAdapter adapter2 = new ListViewAdapter(is_night);
        adapter2.addItem("도움말", "version : 1.0.0");
        listView2.setAdapter(adapter2);
        listView2.setOnItemClickListener(
                (parent, view, position, id) -> Toast.makeText(
                        this, "추가 예정...", Toast.LENGTH_SHORT
                ).show());
    }

    private final ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "이제 화면이 꺼지면 할 일 목록이 표시됩니다.", Toast.LENGTH_SHORT).show();
                    sp.edit().putBoolean("msg_show", true).apply();
                } else {
                    notify_switch.setChecked(false);
                    sp.edit().putBoolean("msg_show", false).apply();
                }
            }
    );

    @Override
    public void onBackPressed() {
        Intent it = new Intent(this, MainActivity.class);
        startActivity(it);
        finish();
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}