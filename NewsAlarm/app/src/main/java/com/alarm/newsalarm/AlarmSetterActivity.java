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

import com.alarm.newsalarm.database.AlarmDatabase;
import com.alarm.newsalarm.sampleplayer.SampleSoundPlayer;
import com.alarm.newsalarm.sampleplayer.SampleVibrator;
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
    private SampleSoundPlayer soundPlayer;
    private SampleVibrator vibrator;
    private TimePicker timePicker;
    private DatePickerDialog dialog;
    private ImageButton btnDateSelector;
    private EditText etAlarmName, etNewsTopic;
    private ImageView ivVolumeMute, ivVolumeLow, ivVolumeMedium, ivVolumeHigh;
    private ImageView ivVibNone, ivVibLow, ivVibMedium, ivVibHigh;
    private Slider slVolume, slVib;
    private MaterialButton btnSave, btnCancel;
    private int year, month, day;
    private float volumeSize, vibIntensity;

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
        soundPlayer = new SampleSoundPlayer(this);
        vibrator = new SampleVibrator(this);

        // load saved settings from Room
        displayVolumeImgByVolume(volumeSize);
        displayVibImgByVibIntensity(vibIntensity);
    }

    private void initUI() {
        timePicker = findViewById(R.id.timePicker);
        btnDateSelector = findViewById(R.id.datePicker);
        for (int i = 0; i < 7; i++) {
            cbWeekdays[i] = findViewById(WEEK_IDS[i]);
        }
        etAlarmName = findViewById(R.id.etAlarmName);
        etNewsTopic = findViewById(R.id.etNewsTopic);
        slVolume = findViewById(R.id.slideVolume);
        slVib = findViewById(R.id.slideVib);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        initImageViews();
    }

    private void initImageViews() {
        ivVolumeMute = findViewById(R.id.muteImg);
        ivVolumeLow = findViewById(R.id.volumeLowImg);
        ivVolumeMedium = findViewById(R.id.volumeMediumImg);
        ivVolumeHigh = findViewById(R.id.volumeHighImg);

        ivVibNone = findViewById(R.id.vibNoneImg);
        ivVibLow = findViewById(R.id.vibLowImg);
        ivVibMedium = findViewById(R.id.vibMediumImg);
        ivVibHigh = findViewById(R.id.vibHighImg);
    }

    private void initDatePickerDialog() {
        dialog = new DatePickerDialog(this);
        dialog.setOnDateSetListener((v, year, month, day) -> saveSelectedDate(year, month, day));
        dialog.setOnCancelListener(d -> resetToSelectedDate());
    }

    private void saveSelectedDate(int year, int month, int day) {
        revertWeekdayCheckBox();
        this.year = year;
        this.month = month + 1;
        this.day = day;
        Log.d(CLASS_NAME, "saveSelectedDate$date selecting complete!");
        Toast.makeText(this, this.year + "-" + this.month + "-" + this.day + " 날짜로 알람 설정합니다.",
                Toast.LENGTH_SHORT).show();
    }

    private void revertWeekdayCheckBox() {
        for (CheckBox weekday : cbWeekdays) {
            weekday.setChecked(false);
        }
    }

    private void resetToSelectedDate() {
        dialog.updateDate(year, month - 1, day);
        Log.d(CLASS_NAME, "resetToSelectedDate$date selecting cancelled");
    }

    private void setEventListener() {
        setWeekdayListeners();
        btnDateSelector.setOnClickListener(v -> openDatePicker());
        slVolume.addOnChangeListener((slider, value, fromUser) -> {
            displayVolumeImgByVolume(value);
            playSoundByValue(value);
        });
        slVib.addOnChangeListener((slider, value, fromUser) -> {
            displayVibImgByVibIntensity(value);
            vibrateByValue(value);
        });
        btnSave.setOnClickListener(v -> saveSetting());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setWeekdayListeners() {
        for (int i = 0; i < 7; i++) {
            cbWeekdays[i].setOnClickListener(v -> announceSelectedWeekday());
        }
    }

    private void announceSelectedWeekday() {
        StringBuilder sb = new StringBuilder("매 주 ");
        boolean isSelected = false;
        for (CheckBox weekday : cbWeekdays) {
            if (weekday.isChecked()) {
                isSelected = true;
                sb.append(weekday.getText()).append(" ");
            }
        }
        if (isSelected) {
            sb.append("마다 알람이 울립니다.");
            Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openDatePicker() {
        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(possibleMaxDate);
        dialog.show();
    }

    private void playSoundByValue(float value) {
        volumeSize = value;
        soundPlayer.playSound(R.raw.ding_dong, value);
        Log.i(CLASS_NAME, "playSoundByValue$cur volume : " + volumeSize);
    }

    private void vibrateByValue(float value) {
        vibIntensity = value;
        vibrator.vibrate((int) value * 51);
        Log.i(CLASS_NAME, "vibrateByValue$cur vibration : " + vibIntensity);
    }

    private void saveSetting() {
        // save settings in Room and register Alarm
        finish();
    }

    private void displayVolumeImgByVolume(float volume) {
        if (Float.compare(0f, volume) == 0) {
            setInvisibleAllVolumeImg();
            ivVolumeMute.setVisibility(ImageView.VISIBLE);
        } else if (Float.compare(volume, 0f) > 0 && Float.compare(volume, 33f) <= 0) {
            setInvisibleAllVolumeImg();
            ivVolumeLow.setVisibility(ImageView.VISIBLE);
        } else if (Float.compare(volume, 33f) > 0 && Float.compare(volume, 66f) <= 0) {
            setInvisibleAllVolumeImg();
            ivVolumeMedium.setVisibility(ImageView.VISIBLE);
        } else {
            setInvisibleAllVolumeImg();
            ivVolumeHigh.setVisibility(ImageView.VISIBLE);
        }
    }

    private void setInvisibleAllVolumeImg() {
        ivVolumeMute.setVisibility(ImageView.INVISIBLE);
        ivVolumeLow.setVisibility(ImageView.INVISIBLE);
        ivVolumeMedium.setVisibility(ImageView.INVISIBLE);
        ivVolumeHigh.setVisibility(ImageView.INVISIBLE);
    }

    private void displayVibImgByVibIntensity(float vib) {
        if (Float.compare(vib, 0f) == 0) {
            setInvisibleAllVibImg();
            ivVibNone.setVisibility(ImageView.VISIBLE);
        } else if (Float.compare(vib, 1f) == 0) {
            setInvisibleAllVibImg();
            ivVibLow.setVisibility(ImageView.VISIBLE);
        } else if (Float.compare(vib, 1f) > 0 && Float.compare(vib, 4f) < 0) {
            setInvisibleAllVibImg();
            ivVibMedium.setVisibility(ImageView.VISIBLE);
        } else {
            setInvisibleAllVibImg();
            ivVibHigh.setVisibility(ImageView.VISIBLE);
        }
    }

    private void setInvisibleAllVibImg() {
        ivVibNone.setVisibility(ImageView.INVISIBLE);
        ivVibLow.setVisibility(ImageView.INVISIBLE);
        ivVibMedium.setVisibility(ImageView.INVISIBLE);
        ivVibHigh.setVisibility(ImageView.INVISIBLE);
    }

    @Override
    protected void onDestroy() {
        soundPlayer.release();
        vibrator.cancel();
        super.onDestroy();
    }
}
