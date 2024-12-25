package com.alarm.newsalarm;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alarm.newsalarm.alarmmanager.AlarmSetter;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;
import com.alarm.newsalarm.outputmanager.TtsManager;
import com.alarm.newsalarm.outputmanager.Vibrator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class AlarmSetterActivity extends BaseActivity {

    private static final String[] WEEK = {"일", "월", "화", "수", "목", "금", "토"};
    private static final int[] WEEK_IDS = {
        R.id.cbSunday, R.id.cbMonday, R.id.cbTuesday, R.id.cbWednesday,
        R.id.cbThursday, R.id.cbFriday, R.id.cbSaturday
    };
    private static final int DROPDOWN_LAYOUT =
        com.google.android.material.R.layout.support_simple_spinner_dropdown_item;

    private final Calendar calendar = Calendar.getInstance();
    private final Locale locale = new Locale("ko", "KR");
    private final CheckBox[] cbWeekdays = new CheckBox[7];
    private InputMethodManager inputManager;
    private AlarmData alarmData;
    private AlarmSetter setter;
    private SharedPreferences sharedPref;
    private Vibrator vibrator;
    private TtsManager ttsManager;
    private TimePicker timePicker;
    private DatePickerDialog dialog;
    private TextView tvInfo, tvTempo;
    private ImageButton btnDateSelector;
    private TextInputLayout topicSelector, genderSelector;
    private AutoCompleteTextView tvTopicList, tvGenderList;
    private ImageView ivVolumeMute, ivVolumeLow, ivVolumeMedium, ivVolumeHigh;
    private ImageView ivVibNone, ivVibLow, ivVibMedium, ivVibHigh;
    private Slider slTempo, slVolume, slVib;
    private MaterialButton btnSave, btnCancel;
    private String selectedTopic = "";
    private String selectedGender = "남성";
    private int curWeekBit;

    public AlarmSetterActivity() {
        super("AlarmSetterActivity");

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setter);

        init();
        initUI();
        initDropdownSelectors();
        setViewsFromAlarmData();
        setTvInfo();
        setEventListener();

        displayTvTempoByTempo(slTempo.getValue());
        displayVolumeImgByVolume((int) slVolume.getValue());
        displayVibImgByVibIntensity((int) slVib.getValue());
    }

    private void init() {
        alarmData = getIntent().getParcelableExtra("alarmData", AlarmData.class);
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        vibrator = new Vibrator(this);
        ttsManager = new TtsManager(this, alarmData);
        setter = new AlarmSetter(this);
        sharedPref = getSharedPreferences("id_pref", Context.MODE_PRIVATE);
    }

    private void initUI() {
        dialog = new DatePickerDialog(this);
        timePicker = findViewById(R.id.timePicker);
        btnDateSelector = findViewById(R.id.datePicker);
        for (int i = 0; i < 7; i++) {
            cbWeekdays[i] = findViewById(WEEK_IDS[i]);
        }
        tvInfo = findViewById(R.id.tvInfo);
        tvTempo = findViewById(R.id.tvTempo);
        topicSelector = findViewById(R.id.newsTopicSelector);
        tvTopicList = findViewById(R.id.tvTopicList);
        genderSelector = findViewById(R.id.genderSelector);
        tvGenderList = findViewById(R.id.tvGenderList);
        slTempo = findViewById(R.id.slideTempo);
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

    private void initDropdownSelectors() {
        initNewsTopicDropdownSelector();
        initGenderDropdownSelector();
    }

    private void initNewsTopicDropdownSelector() {
        String[] topics = getResources().getStringArray(R.array.topics);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, DROPDOWN_LAYOUT, topics) {

            @NonNull
            @Override
            public Filter getFilter() {
                return getDropdownFilter(topics);
            }
        };
        tvTopicList.setThreshold(Integer.MAX_VALUE);
        tvTopicList.setAdapter(adapter);
    }

    private void initGenderDropdownSelector() {
        String[] genders = getResources().getStringArray(R.array.gender);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, DROPDOWN_LAYOUT, genders) {

            @NonNull
            @Override
            public Filter getFilter() {
                return getDropdownFilter(genders);
            }
        };
        tvGenderList.setThreshold(Integer.MAX_VALUE);
        tvGenderList.setAdapter(adapter);
    }

    private Filter getDropdownFilter(String[] values) {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.count = values.length;
                results.values = values;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
            }
        };
    }

    private void setViewsFromAlarmData() {
        if (alarmData == null) {
            return;
        }
        curWeekBit = alarmData.getPeriodicWeekBit();
        calendar.setTimeInMillis(alarmData.getSpecificDateInMillis());
        setWeekCheckbox();
        setTimePicker();
        setSelectors();
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

    private void setSelectors() {
        selectedTopic = alarmData.getAlarmTopic();
        selectedGender = alarmData.getGender();
        tvTopicList.setText(selectedTopic, false);
        tvGenderList.setText(selectedGender, false);
    }

    private void setSliders() {
        slTempo.setValue(alarmData.getTempo());
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
        tvTopicList.setOnItemClickListener((parent, view, pos, id) -> {
            selectedTopic = (String) tvTopicList.getAdapter().getItem(pos);
            if ("직접 입력".equals(selectedTopic)) {
                selectedTopic = "";
                tvTopicList.setText("", false);
                tvTopicList.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                tvTopicList.requestFocus();
                inputManager.showSoftInput(tvTopicList, 0);
            } else {
                tvTopicList.setInputType(EditorInfo.TYPE_NULL);
                inputManager.hideSoftInputFromWindow(
                    Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                tvTopicList.clearFocus();
                topicSelector.setHelperText("설정한 키워드 : " + selectedTopic);
            }
        });
        tvTopicList.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                selectedTopic = tvTopicList.getText().toString();
                inputManager.hideSoftInputFromWindow(
                    Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                tvTopicList.clearFocus();
                topicSelector.setHelperText("설정한 키워드 : " + selectedTopic);
                return true;
            }
            return false;
        });
        tvGenderList.setOnItemClickListener((parent, view, pos, id) -> {
            selectedGender = (String) tvGenderList.getAdapter().getItem(pos);
            tvGenderList.clearFocus();
            genderSelector.setHelperText("설정한 목소리 : " + selectedGender);
            ttsManager.setVoiceGender("남성".equals(selectedGender) ? "male" : "female");
        });
        slTempo.addOnChangeListener((slider, value, fromUser) -> {
            displayTvTempoByTempo(value);
            playSampleTextByTempo(value);
        });
        slVolume.addOnChangeListener((slider, value, fromUser) -> {
            displayVolumeImgByVolume((int) value);
            playSampleTextByVolumeSize((int) value);
        });
        slVib.addOnChangeListener((slider, value, fromUser) -> {
            displayVibImgByVibIntensity((int) value);
            vibrateByValue(value);
        });
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            setTvInfo();
        });
        for (int i = 0; i < 7; i++) {
            final int idx = i;
            cbWeekdays[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                curWeekBit = isChecked ? curWeekBit + (1 << idx) : curWeekBit - (1 << idx);
                setDateAsTodayWhenDeselectAll();
                setTvInfo();
            });
        }
        btnSave.setOnClickListener(v -> saveSetting());
        btnCancel.setOnClickListener(v -> finish());
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

    private void playSampleTextByVolumeSize(int value) {
        ttsManager.setVolumeSize(value);
        ttsManager.speak("안녕하세요?", 0, TextToSpeech.QUEUE_FLUSH);
    }

    private void playSampleTextByTempo(float value) {
        ttsManager.setVoiceTempo(value);
        ttsManager.speak("안녕하세요?", 0, TextToSpeech.QUEUE_FLUSH);
    }
    private void vibrateByValue(float value) {
        vibrator.vibrateOnce((int) value * 51);
    }

    @SuppressLint("SetTextI18n")
    private void displayTvTempoByTempo(float tempo) {
        tvTempo.setText("x " + tempo);
    }

    private void displayVolumeImgByVolume(int volume) {
        setInvisibleAllVolumeImg();
        if (volume == 0) {
            ivVolumeMute.setVisibility(ImageView.VISIBLE);
        } else if (volume <= 5) {
            ivVolumeLow.setVisibility(ImageView.VISIBLE);
        } else if (volume <= 10) {
            ivVolumeMedium.setVisibility(ImageView.VISIBLE);
        } else {
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
        setInvisibleAllVibImg();
        if (vib == 0) {
            ivVibNone.setVisibility(ImageView.VISIBLE);
        } else if (vib <= 1) {
            ivVibLow.setVisibility(ImageView.VISIBLE);
        } else if (vib <= 3) {
            ivVibMedium.setVisibility(ImageView.VISIBLE);
        } else {
            ivVibHigh.setVisibility(ImageView.VISIBLE);
        }
    }

    private void setInvisibleAllVibImg() {
        ivVibNone.setVisibility(ImageView.INVISIBLE);
        ivVibLow.setVisibility(ImageView.INVISIBLE);
        ivVibMedium.setVisibility(ImageView.INVISIBLE);
        ivVibHigh.setVisibility(ImageView.INVISIBLE);
    }

    private void setDateAsTodayWhenDeselectAll() {
        if (curWeekBit == 0) {
            setDateAsToday();
        }
    }

    private void setDateAsToday() {
        LocalDate today = LocalDate.now();
        calendar.set(Calendar.YEAR, today.getYear());
        calendar.set(Calendar.MONTH, today.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, today.getDayOfMonth());
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
        alarmData = new AlarmData(getNewId());
        setAlarmData();
        AlarmDatabaseUtil.insert(this, alarmData);
        sendResultToMainActivity("addNewAlarmData");
        return true;
    }

    private int getNewId() {
        int curId = sharedPref.getInt("alarm_id", 0);
        sharedPref.edit().putInt("alarm_id", ++curId).apply();
        return curId;
    }

    private boolean updateAlarmData() {
        setAlarmData();
        AlarmDatabaseUtil.update(this, alarmData);
        sendResultToMainActivity("updateAlarmData");
        return true;
    }

    private void setAlarmData() {
        alarmData.setAlarmTopic(selectedTopic);
        alarmData.setGender(selectedGender);
        alarmData.setTempo(slTempo.getValue());
        alarmData.setVolumeSize((int) slVolume.getValue());
        alarmData.setVibIntensity((int) slVib.getValue() * 51);
        alarmData.setPeriodicWeekBit(curWeekBit);
        alarmData.setSpecificDateInMillis(getDateInMillis());
        alarmData.setActive(true);
    }

    private long getDateInMillis() {
        if (curWeekBit > 0) {
            setDateAsToday();
        }
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
        ttsManager.release();
        vibrator.release();
        super.onDestroy();
    }
}
