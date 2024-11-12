package com.alarm.newsalarm;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.Calendar;

public class AlarmSetterActivity extends BaseActivity {

    private static final int[] WEEK_IDS = {
        R.id.cbSunday, R.id.cbMonday, R.id.cbTuesday, R.id.cbWednesday,
        R.id.cbThursday, R.id.cbFriday, R.id.cbSaturday
    };
    private static final Calendar calendar = Calendar.getInstance();
    private static final long possibleMaxDate;
    static {
        calendar.add(Calendar.MONTH, 1);
        possibleMaxDate = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, -1);
    }

    private final CheckBox[] cbWeekdays = new CheckBox[7];
    private TimePicker timePicker;
    private DatePickerDialog dialog;
    private ImageButton btnDateSelector;
    private EditText etAlarmName, etNewsTopic;
    private ImageView ivVolume, ivVib;
    private Slider slVolume, slVib;
    private MaterialButton btnSave, btnCancel;
    private boolean isDateSelected = true;
    private boolean isPeriodicByWeek;
    private int year, month, day;

    public AlarmSetterActivity() {
        super("AlarmSetterActivity");

        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH);
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setter);

        initUI();
        initDatePickerDialog();
        setEventListener();
    }

    private void initUI() {
        timePicker = findViewById(R.id.timePicker);
        btnDateSelector = findViewById(R.id.datePicker);
        for (int i = 0; i < 7; i++) {
            cbWeekdays[i] = findViewById(WEEK_IDS[i]);
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

    private void initDatePickerDialog() {
        dialog = new DatePickerDialog(this);
        dialog.setOnDateSetListener((v, year, month, day) -> saveSelectedDate(year, month, day));
        dialog.setOnCancelListener(d -> resetToSelectedDate());
    }

    private void saveSelectedDate(int year, int month, int day) {
        this.year = year;
        this.month = month + 1;
        this.day = day;
        Log.d(CLASS_NAME, "saveSelectedDate$date selecting complete!");
        Toast.makeText(this, this.year + "-" + this.month + "-" + this.day + " 날짜로 알람 설정합니다.",
                Toast.LENGTH_SHORT).show();
    }

    private void resetToSelectedDate() {
        dialog.updateDate(year, month - 1, day);
        Log.d(CLASS_NAME, "resetToSelectedDate$date selecting cancelled");
    }

    private void setEventListener() {
        btnDateSelector.setOnClickListener(v -> openDatePicker());
        slVolume.addOnChangeListener((slider, value, fromUser) -> playSoundByValue(value));
        slVib.addOnChangeListener((slider, value, fromUser) -> vibrateByValue(value));
        btnSave.setOnClickListener(v -> saveSetting());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void openDatePicker() {
        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(possibleMaxDate);
        dialog.show();
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
