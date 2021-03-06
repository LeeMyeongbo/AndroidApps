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
                    Toast.makeText(this, "?????? ?????? ??? ?????? ?????? ????????? ????????? ?????????", Toast.LENGTH_SHORT).show();
                    startActivityResult.launch(intent);
                }
                else {
                    Toast.makeText(this, "?????? ????????? ????????? ??? ??? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    sp.edit().putBoolean("msg_show", true).apply();
                }
            } else {
                Toast.makeText(this, "??? ??? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                sp.edit().putBoolean("msg_show", false).apply();
            }
        });

        ListView listView = findViewById(R.id.listview1);
        ListViewAdapter adapter = new ListViewAdapter(is_night);
        adapter.addItem("?????????", "?????? ??? ????????? ???????????? ??????????????????");
        adapter.addItem("?????????????????? ??????", "??????, ????????? ????????? ?????? ??????????????? ????????? ??? ????????????.");
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                AlertDialog.Builder msgBuilder = new AlertDialog.Builder(this)
                        .setTitle("?????????")
                        .setMessage("?????? ???????????? ???????????????. ?????????????????????????")
                        .setNegativeButton("??????", null)
                        .setPositiveButton("?????????", (dialog, which) -> {
                            new DaoAsyncTask(rd.routine_dao(), "CLEAR").execute();

                            Intent intent = new Intent(getApplicationContext(), MyService.class);
                            Toast.makeText(this, "Remind ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                            stopService(intent);
                            Toast.makeText(this, "????????? ??????", Toast.LENGTH_SHORT).show();

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
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
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
        adapter2.addItem("?????????", "version : 1.0.0");
        listView2.setAdapter(adapter2);
        listView2.setOnItemClickListener((parent, view, position, id) -> Toast.makeText(this, "?????? ??????...", Toast.LENGTH_SHORT).show());
    }

    private final ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "?????? ????????? ????????? ??? ??? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
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