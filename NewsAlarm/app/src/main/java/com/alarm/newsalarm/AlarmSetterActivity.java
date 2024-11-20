package com.alarm.newsalarm;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.alarm.newsalarm.alarmmanager.AlarmSetter;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;
import com.alarm.newsalarm.sampleplayer.SampleSoundPlayer;
import com.alarm.newsalarm.sampleplayer.SampleVibrator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    private AlarmData alarmData;
    private AlarmSetter setter;
    private SharedPreferences sharedPref;
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
        setViewsFromAlarmData();
        setEventListener();
        soundPlayer = new SampleSoundPlayer(this);
        vibrator = new SampleVibrator(this);
        sharedPref = getSharedPreferences("id_pref", Context.MODE_PRIVATE);

        displayVolumeImgByVolume(slVolume.getValue());
        displayVibImgByVibIntensity(slVib.getValue());

        setter = new AlarmSetter(this);
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
        this.month = month;
        this.day = day;
        Log.d(CLASS_NAME, "saveSelectedDate$date selecting complete!");
        Toast.makeText(
            this,
            this.year + "-" + this.month + "-" + this.day + " 날짜로 알람 설정합니다.",
            Toast.LENGTH_SHORT
        ).show();
    }

    private void revertWeekdayCheckBox() {
        for (CheckBox weekday : cbWeekdays) {
            weekday.setChecked(false);
        }
    }

    private void resetToSelectedDate() {
        dialog.updateDate(year, month, day);
        Log.d(CLASS_NAME, "resetToSelectedDate$date selecting cancelled");
    }

    private void setViewsFromAlarmData() {
        alarmData = getIntent().getParcelableExtra("alarmData", AlarmData.class);
        if (alarmData == null) {
            return;
        }
        timePicker.setHour(alarmData.getTimeInMin() / 60);
        timePicker.setMinute(alarmData.getTimeInMin() % 60);

        byte weekBit = alarmData.getPeriodicWeekBit();
        if (weekBit > 0) {
            setWeekCheckbox(weekBit);
        } else {
            setDatePickerDialog();
        }
        etAlarmName.setText(alarmData.getAlarmName());
        etNewsTopic.setText(alarmData.getAlarmTopic());

        slVolume.setValue(alarmData.getVolumeSize());
        slVib.setValue(alarmData.getVibIntensity());
    }

    private void setWeekCheckbox(byte weekBit) {
        for (int i = 0; i < 7; i++) {
            cbWeekdays[i].setChecked((weekBit & (1 << i)) == (1 << i));
        }
    }

    private void setDatePickerDialog() {
        calendar.setTimeInMillis(alarmData.getSpecificDateInMillis());
        dialog.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        calendar.setTimeInMillis(System.currentTimeMillis());
    }

    private void setEventListener() {
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

    private void openDatePicker() {
        dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(possibleMaxDate);
        dialog.show();
    }

    private void playSoundByValue(float value) {
        soundPlayer.playSound(R.raw.ding_dong, value);
        Log.i(CLASS_NAME, "playSoundByValue$cur volume : " + value);
    }

    private void vibrateByValue(float value) {
        vibrator.vibrate((int) value * 51);
        Log.i(CLASS_NAME, "vibrateByValue$cur vibration : " + (int) value);
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

    private void saveSetting() {
        if (alarmData == null) {
            if (addNewAlarmData()) {
                registerAlarm();
                Log.i(CLASS_NAME, "saveSetting$adding new alarm data completed!");
            } else {
                Toast.makeText(this, "알람을 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
                Log.i(CLASS_NAME, "saveSetting$couldn't add new alarm data..");
                return;
            }
        } else {
            if (updateAlarmData()) {
                modifyAlarm();
                Log.i(CLASS_NAME, "saveSetting$updating existing alarm data completed!");
            } else {
                Toast.makeText(this, "알람을 수정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                Log.i(CLASS_NAME, "saveSetting$couldn't update alarm data..");
                return;
            }
        }
        finish();
    }

    private boolean addNewAlarmData() {
        alarmData = new AlarmData(
            getNewId(),
            etAlarmName.getText().toString(),
            etNewsTopic.getText().toString(),
            slVolume.getValue(),
            (int) slVib.getValue()
        );
        setAlarmTime();
        if (!AlarmDatabaseUtil.insert(this, alarmData)) {
            return false;
        }
        sendResultToMainActivity("addNewAlarmData");
        return true;
    }

    private long getNewId() {
        long curId = sharedPref.getLong("alarm_id", 0L);
        sharedPref.edit().putLong("alarm_id", ++curId).apply();
        return curId;
    }

    private boolean updateAlarmData() {
        alarmData.setAlarmName(etAlarmName.getText().toString());
        alarmData.setAlarmTopic(etNewsTopic.getText().toString());
        alarmData.setVolumeSize(slVolume.getValue());
        alarmData.setVibIntensity((int) slVib.getValue());
        setAlarmTime();
        if (!AlarmDatabaseUtil.update(this, alarmData)) {
            return false;
        }
        sendResultToMainActivity("updateAlarmData");
        return true;
    }

    private void setAlarmTime() {
        StringBuilder toastMessage = new StringBuilder("매주");
        byte weekBit = getWeekBit(toastMessage);
        if (weekBit > 0) {
            toastMessage.append(" 마다 알람이 울립니다.");
            Toast.makeText(this, toastMessage.toString(), Toast.LENGTH_LONG).show();
            alarmData.setPeriodicWeekBit(weekBit);
        } else {
            alarmData.setSpecificDateInMillis(getDateInMillis());
        }
        alarmData.setTimeInMin(timePicker.getHour() * 60 + timePicker.getMinute());
    }

    private byte getWeekBit(StringBuilder toastMessage) {
        byte weekBit = 0;
        for (byte i = 0; i < 7; i++) {
            if (cbWeekdays[i].isChecked()) {
                toastMessage.append(" ").append(cbWeekdays[i].getText());
                weekBit = (byte) (weekBit | (1 << i));
            }
        }
        return weekBit;
    }

    private long getDateInMillis() {
        setAlarmDateInCalendar();
        long alarmDate = calendar.getTimeInMillis();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", new Locale("ko", "KR"));
        String timeFormat = format.format(calendar.getTime());
        Toast.makeText(this, timeFormat + "에 알람이 울립니다.", Toast.LENGTH_LONG).show();

        calendar.setTime(new Date(System.currentTimeMillis()));
        return alarmDate;
    }

    private void setAlarmDateInCalendar() {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        calendar.set(Calendar.MINUTE, timePicker.getMinute());
        calendar.set(Calendar.SECOND, 0);

        if (System.currentTimeMillis() >= calendar.getTimeInMillis()) {
            calendar.add(Calendar.DATE, 1);
        }
    }

    private void sendResultToMainActivity(String extraName) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(extraName, alarmData);
        setResult(RESULT_OK, resultIntent);
    }

    private void registerAlarm() {
        if (alarmData.getPeriodicWeekBit() == 0) {
            setter.setSpecificAlarm(alarmData.getId(), alarmData.getSpecificDateInMillis());
        } else {
            /* To Do : register periodic alarm */
        }
    }

    private void modifyAlarm() {
        setter.cancelAlarm(alarmData.getId());
        registerAlarm();
    }

    @Override
    protected void onDestroy() {
        soundPlayer.release();
        vibrator.cancel();
        super.onDestroy();
    }
}
