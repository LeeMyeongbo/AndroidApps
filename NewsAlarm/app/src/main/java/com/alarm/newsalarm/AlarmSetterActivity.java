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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.alarm.newsalarm.alarmmanager.AlarmSetter;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;
import com.alarm.newsalarm.outputmanager.SoundPlayer;
import com.alarm.newsalarm.outputmanager.Vibrator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Locale;

public class AlarmSetterActivity extends BaseActivity {

    private static final String[] WEEK = {"일", "월", "화", "수", "목", "금", "토"};
    private static final int[] WEEK_IDS = {
        R.id.cbSunday, R.id.cbMonday, R.id.cbTuesday, R.id.cbWednesday,
        R.id.cbThursday, R.id.cbFriday, R.id.cbSaturday
    };

    private final Calendar calendar = Calendar.getInstance();
    private final Locale locale = new Locale("ko", "KR");
    private final CheckBox[] cbWeekdays = new CheckBox[7];
    private AlarmData alarmData;
    private AlarmSetter setter;
    private SharedPreferences sharedPref;
    private SoundPlayer soundPlayer;
    private Vibrator vibrator;
    private TimePicker timePicker;
    private DatePickerDialog dialog;
    private TextView tvInfo;
    private ImageButton btnDateSelector;
    private EditText etAlarmName, etNewsTopic;
    private ImageView ivVolumeMute, ivVolumeLow, ivVolumeMedium, ivVolumeHigh;
    private ImageView ivVibNone, ivVibLow, ivVibMedium, ivVibHigh;
    private Slider slVolume, slVib;
    private MaterialButton btnSave, btnCancel;
    private int curWeekBit;

    public AlarmSetterActivity() {
        super("AlarmSetterActivity");

        calendar.setTimeInMillis(System.currentTimeMillis());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setter);

        initUI();
        setViewsFromAlarmData();
        setTvInfo();
        setEventListener();
        soundPlayer = new SoundPlayer(this);
        vibrator = new Vibrator(this);
        setter = new AlarmSetter(this);
        sharedPref = getSharedPreferences("id_pref", Context.MODE_PRIVATE);

        displayVolumeImgByVolume((int) slVolume.getValue());
        displayVibImgByVibIntensity((int) slVib.getValue());
    }

    private void initUI() {
        dialog = new DatePickerDialog(this);
        timePicker = findViewById(R.id.timePicker);
        btnDateSelector = findViewById(R.id.datePicker);
        for (int i = 0; i < 7; i++) {
            cbWeekdays[i] = findViewById(WEEK_IDS[i]);
        }
        tvInfo = findViewById(R.id.tvInfo);
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

    private void setViewsFromAlarmData() {
        alarmData = getIntent().getParcelableExtra("alarmData", AlarmData.class);
        if (alarmData == null) {
            return;
        }
        curWeekBit = alarmData.getPeriodicWeekBit();
        calendar.setTimeInMillis(alarmData.getSpecificDateInMillis());
        setWeekCheckbox();
        setTimePicker();
        setEditTexts();
        setSliders();
    }

    private void setWeekCheckbox() {
        for (int i = 0; i < 7; i++) {
            cbWeekdays[i].setChecked((curWeekBit & (1 << i)) == (1 << i));
        }
    }

    private void setTimePicker() {
        timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(calendar.get(Calendar.MINUTE));
    }

    private void setEditTexts() {
        etAlarmName.setText(alarmData.getAlarmName());
        etNewsTopic.setText(alarmData.getAlarmTopic());
    }

    private void setSliders() {
        slVolume.setValue(alarmData.getVolumeSize());
        slVib.setValue(alarmData.getVibIntensity() / 51f);
    }

    private void setTvInfo() {
        if (curWeekBit == 0) {
            setTvInfoSpecific();
            return;
        }
        setTvInfoToPeriodic();
    }

    private void setTvInfoSpecific() {
        StringBuilder builder = new StringBuilder();
        long dateMillis = calendar.getTimeInMillis();
        SimpleDateFormat format = new SimpleDateFormat("MM월 dd일 HH:mm", locale);
        String timeFormat = dateMillis <= System.currentTimeMillis()
            ? format.format(dateMillis + 1000L * 60 * 60 * 24)
            : format.format(dateMillis);
        tvInfo.setText(builder.append(timeFormat).append("에 알람이 울립니다.").toString());
    }

    private void setTvInfoToPeriodic() {
        StringBuilder builder = new StringBuilder("매주");
        for (int i = 0; i < 7; i++) {
            if ((curWeekBit & (1 << i)) == (1 << i)) {
                builder.append(" ").append(WEEK[i]);
            }
        }
        tvInfo.setText(builder.append("마다 알람이 울립니다.").toString());
    }

    private void setEventListener() {
        dialog.setOnDateSetListener((v, year, month, day) -> saveSelectedDate(year, month, day));
        btnDateSelector.setOnClickListener(v -> openDatePicker());
        slVolume.addOnChangeListener((slider, value, fromUser) -> {
            displayVolumeImgByVolume((int) value);
            playSoundByValue((int) value);
        });
        slVib.addOnChangeListener((slider, value, fromUser) -> {
            displayVibImgByVibIntensity((int) value);
            vibrateByValue(value);
        });
        btnSave.setOnClickListener(v -> saveSetting());
        btnCancel.setOnClickListener(v -> finish());
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            setTvInfo();
        });
        for (int i = 0; i < 7; i++) {
            final int FI = i;
            cbWeekdays[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (curWeekBit == 0) {
                    LocalDate today = LocalDate.now();
                    calendar.set(Calendar.YEAR, today.getYear());
                    calendar.set(Calendar.MONTH, today.getMonthValue() - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, today.getDayOfMonth());
                }
                curWeekBit = isChecked ? curWeekBit + (1 << FI) : curWeekBit - (1 << FI);
                setTvInfo();
            });
        }
    }

    private void saveSelectedDate(int year, int month, int day) {
        revertWeekdayCheckBox();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        setTvInfo();
    }

    private void revertWeekdayCheckBox() {
        for (CheckBox weekday : cbWeekdays) {
            weekday.setChecked(false);
        }
        curWeekBit = 0;
    }

    private void openDatePicker() {
        long curDate = System.currentTimeMillis();
        dialog.getDatePicker().setMinDate(curDate);
        dialog.getDatePicker().setMaxDate(curDate + 1000L * 60 * 60 * 24 * 28);
        dialog.show();
    }

    private void playSoundByValue(int value) {
        soundPlayer.playShortSound(value);
        Log.i(CLASS_NAME, "playSoundByValue$cur volume : " + value);
    }

    private void vibrateByValue(float value) {
        vibrator.vibrateOnce((int) value * 51);
        Log.i(CLASS_NAME, "vibrateByValue$cur vibration : " + (int) value);
    }

    private void displayVolumeImgByVolume(int volume) {
        if (volume == 0) {
            setInvisibleAllVolumeImg();
            ivVolumeMute.setVisibility(ImageView.VISIBLE);
        } else if (volume <= 5) {
            setInvisibleAllVolumeImg();
            ivVolumeLow.setVisibility(ImageView.VISIBLE);
        } else if (volume <= 10) {
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

    private void displayVibImgByVibIntensity(int vib) {
        if (vib == 0) {
            setInvisibleAllVibImg();
            ivVibNone.setVisibility(ImageView.VISIBLE);
        } else if (vib <= 1) {
            setInvisibleAllVibImg();
            ivVibLow.setVisibility(ImageView.VISIBLE);
        } else if (vib <= 3) {
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
            (int) slVolume.getValue(),
            (int) slVib.getValue() * 51
        );
        setAlarmTime();
        if (!AlarmDatabaseUtil.insert(this, alarmData)) {
            return false;
        }
        sendResultToMainActivity("addNewAlarmData");
        return true;
    }

    private int getNewId() {
        int curId = sharedPref.getInt("alarm_id", 0);
        sharedPref.edit().putInt("alarm_id", ++curId).apply();
        return curId;
    }

    private boolean updateAlarmData() {
        alarmData.setAlarmName(etAlarmName.getText().toString());
        alarmData.setAlarmTopic(etNewsTopic.getText().toString());
        alarmData.setVolumeSize((int) slVolume.getValue());
        alarmData.setVibIntensity((int) slVib.getValue() * 51);
        setAlarmTime();
        if (!AlarmDatabaseUtil.update(this, alarmData)) {
            return false;
        }
        sendResultToMainActivity("updateAlarmData");
        return true;
    }

    private void setAlarmTime() {
        alarmData.setPeriodicWeekBit(curWeekBit);
        alarmData.setSpecificDateInMillis(getDateInMillis());
    }

    private long getDateInMillis() {
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return calendar.getTimeInMillis();
    }

    private void sendResultToMainActivity(String extraName) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(extraName, alarmData);
        setResult(RESULT_OK, resultIntent);
    }

    private void registerAlarm() {
        setter.registerAlarm(alarmData);
    }

    private void modifyAlarm() {
        setter.cancelAlarm(alarmData);
        registerAlarm();
    }

    @Override
    protected void onDestroy() {
        soundPlayer.release();
        vibrator.release();
        super.onDestroy();
    }
}
