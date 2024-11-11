package com.alarm.newsalarm;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

public class AlarmSetterActivity extends AppCompatActivity {

    private static final int[] WEEK_IDS = {
        R.id.tvSunday, R.id.tvMonday, R.id.tvTuesday, R.id.tvWednesday,
        R.id.tvThursday, R.id.tvFriday, R.id.tvSaturday
    };
    private final TextView[] tvWeek = new TextView[7];
    private TimePicker timePicker;
    private MaterialButton btnDateSelector;
    private EditText etAlarmName, etNewsTopic;
    private ImageView ivVolume, ivVib;
    private Slider slVolume, slVib;
    private MaterialButton btnSave, btnCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setter);

        initUI();
        setEvent();
    }

    private void initUI() {
        timePicker = findViewById(R.id.timePicker);
        btnDateSelector = findViewById(R.id.datePicker);
        for (int i = 0; i < 7; i++) {
            tvWeek[i] = findViewById(WEEK_IDS[i]);
        }
        etAlarmName = findViewById(R.id.etAlarmName);
        etNewsTopic = findViewById(R.id.etNewsTopic);
        ivVolume = findViewById(R.id.volumeImg);
        ivVib = findViewById(R.id.vibImg);
        slVolume = findViewById(R.id.slideVolume);
        slVib = findViewById(R.id.slideVib);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setEvent() {
        btnDateSelector.setOnClickListener(v -> openDatePicker());
        slVolume.addOnChangeListener((slider, value, fromUser) -> playSoundByValue(value));
        slVib.addOnChangeListener((slider, value, fromUser) -> vibrateByValue(value));
        btnSave.setOnClickListener(v -> saveSetting());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void openDatePicker() {
        // open DatePicker Dialog (Calendar)
    }

    private void playSoundByValue(float value) {
        // play sound by value set by slVolume
    }

    private void vibrateByValue(float value) {
        // start vibration by value set by slVib
    }

    private void saveSetting() {
        // save settings in Room and register Alarm
        finish();
    }
}
