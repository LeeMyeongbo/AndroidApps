package com.alarm.newsalarm;

import static androidx.core.view.WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_STOP;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;

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
import java.util.List;
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
    private ScrollView backgroundScrollview;
    private InputMethodManager inputManager;
    private AlarmData alarmData;
    private AlarmSetter setter;
    private SharedPreferences sharedPref;
    private Vibrator vibrator;
    private TtsManager ttsManager;
    private TimePicker timePicker;
    private DatePickerDialog dialog;
    private TextView tvInfo, tvTempo, tvPitch;
    private ImageButton btnDateSelector;
    private TextInputLayout topicSelector, voiceSelector;
    private AutoCompleteTextView tvTopicList, tvVoiceList;
    private ImageView ivVolumeMute, ivVolumeLow, ivVolumeMedium, ivVolumeHigh;
    private ImageView ivVibNone, ivVibLow, ivVibMedium, ivVibHigh;
    private Slider slTempo, slVolume, slVib, slPitch;
    private MaterialButton btnSave, btnCancel;
    private String selectedTopic = "";
    private int selectedVoiceIdx = -1, curWeekBit;

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
        initNewsTopicDropdownSelector();
        setViewsFromAlarmData();
        setTvInfo();
        setEventListener();

        displayTvTempoByTempo(slTempo.getValue());
        displayVolumeImgByVolume((int) slVolume.getValue());
        displayVibImgByVibIntensity((int) slVib.getValue());
        displayTvPitchByPitch(slPitch.getValue());
    }

    private void init() {
        alarmData = getIntent().getParcelableExtra("alarmData", AlarmData.class);
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        vibrator = new Vibrator(this);
        ttsManager = new TtsManager(this, alarmData, this::initVoiceDropdownSelector);
        setter = new AlarmSetter(this);
        sharedPref = getSharedPreferences("id_pref", Context.MODE_PRIVATE);
    }

    private void initUI() {
        backgroundScrollview = findViewById(R.id.backgroundScrollview);
        dialog = new DatePickerDialog(this);
        timePicker = findViewById(R.id.timePicker);
        btnDateSelector = findViewById(R.id.datePicker);
        for (int i = 0; i < 7; i++) {
            cbWeekdays[i] = findViewById(WEEK_IDS[i]);
        }
        tvInfo = findViewById(R.id.tvInfo);
        tvTempo = findViewById(R.id.tvTempo);
        tvPitch = findViewById(R.id.tvPitch);
        topicSelector = findViewById(R.id.newsTopicSelector);
        tvTopicList = findViewById(R.id.tvTopicList);
        voiceSelector = findViewById(R.id.voiceSelector);
        tvVoiceList = findViewById(R.id.tvVoiceList);
        slTempo = findViewById(R.id.slideTempo);
        slVolume = findViewById(R.id.slideVolume);
        slVib = findViewById(R.id.slideVib);
        slPitch = findViewById(R.id.slidePitch);
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

    @SuppressLint("SetTextI18n")
    private void initVoiceDropdownSelector() {
        String[] voices = new String[ttsManager.getAvailableVoiceNum()];
        for (int i = 1; i <= voices.length; i++) {
            voices[i - 1] = "음성 " + i;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, DROPDOWN_LAYOUT, voices) {

            @NonNull
            @Override
            public Filter getFilter() {
                return getDropdownFilter(voices);
            }
        };
        tvVoiceList.setThreshold(Integer.MAX_VALUE);
        tvVoiceList.setAdapter(adapter);
        if (alarmData != null) {
            selectedVoiceIdx = alarmData.getVoiceIdx();
            tvVoiceList.setText("음성 " + (selectedVoiceIdx + 1), false);
        }
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
        tvTopicList.setText(selectedTopic, false);
    }

    private void setSliders() {
        slTempo.setValue(alarmData.getTempo());
        slVolume.setValue(alarmData.getVolumeSize());
        slVib.setValue(alarmData.getVibIntensity() / 51f);
        slPitch.setValue(alarmData.getPitch());
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
            ? format.format(dateMillis + 1000L * 60 * 60 * 24) : format.format(dateMillis);
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
        ViewCompat.setWindowInsetsAnimationCallback(backgroundScrollview,
            new WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

                @NonNull
                @Override
                public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets,
                        @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
                    return insets;
                }

                @Override
                public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
                    super.onEnd(animation);
                    boolean isKeyboardVisible = Objects
                        .requireNonNull(ViewCompat.getRootWindowInsets(backgroundScrollview))
                        .isVisible(WindowInsetsCompat.Type.ime());

                    if (isKeyboardVisible) {
                        int finalKeyboardHeight = Objects
                            .requireNonNull(ViewCompat.getRootWindowInsets(backgroundScrollview))
                            .getInsets(WindowInsetsCompat.Type.ime()).bottom;

                        backgroundScrollview.post(() -> {
                            int[] location = new int[2];
                            tvVoiceList.getLocationOnScreen(location);

                            int screenHeight = backgroundScrollview.getRootView().getHeight();
                            int scroll = location[1] - (screenHeight - finalKeyboardHeight);
                            backgroundScrollview.smoothScrollBy(0, scroll);
                        });
                    } else {
                        clearFocusOnTopicSelector();
                    }
                }
            }
        );
        tvTopicList.setOnItemClickListener((parent, view, pos, id) -> {
            if ("직접 입력".equals(selectedTopic)) {
                selectedTopic = "";
                tvTopicList.setText("", false);
                tvTopicList.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                tvTopicList.requestFocus();
                inputManager.showSoftInput(tvTopicList, 0);
            } else {
                selectedTopic = (String) tvTopicList.getAdapter().getItem(pos);
                inputManager.hideSoftInputFromWindow(
                    Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                topicSelector.setHelperText("설정한 키워드 : " + selectedTopic);
                clearFocusOnTopicSelector();
            }
        });
        tvTopicList.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BACK) {
                inputManager.hideSoftInputFromWindow(
                    Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
            }
            return false;
        });
        tvTopicList.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedTopic = s.toString();
                topicSelector.setHelperText(selectedTopic.isBlank()
                    ? "" : "설정한 키워드 : " + selectedTopic);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        tvVoiceList.setOnItemClickListener((parent, view, pos, id) -> {
            selectedVoiceIdx = pos;
            tvVoiceList.clearFocus();
            voiceSelector.setHelperText("설정한 목소리 : 음성" + (selectedVoiceIdx + 1));
            ttsManager.setSpecificVoice(selectedVoiceIdx);
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
        slPitch.addOnChangeListener((slider, value, fromUser) -> {
            displayTvPitchByPitch(value);
            playSampleTestByPitch(value);
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

    private void clearFocusOnTopicSelector() {
        tvTopicList.clearFocus();
        tvTopicList.setInputType(EditorInfo.TYPE_NULL);
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

    private void playSampleTestByPitch(float value) {
        ttsManager.setVoicePitch(value);
        ttsManager.speak("안녕하세요?", 0, TextToSpeech.QUEUE_FLUSH);
    }

    private void vibrateByValue(float value) {
        vibrator.vibrateOnce((int) value * 51);
    }

    @SuppressLint("SetTextI18n")
    private void displayTvTempoByTempo(float tempo) {
        tvTempo.setText("x " + tempo);
    }

    @SuppressLint("SetTextI18n")
    private void displayTvPitchByPitch(float pitch) {
        tvPitch.setText("" + pitch);
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
        if (selectedTopic.isBlank()) {
            topicSelector.setError("키워드를 입력해주세요.");
            backgroundScrollview.smoothScrollTo(0, topicSelector.getTop());
            return;
        }
        if (selectedVoiceIdx == -1) {
            voiceSelector.setError("음성을 선택해주세요.");
            backgroundScrollview.smoothScrollTo(0, voiceSelector.getTop());
            return;
        }
        if (alarmData == null) {
            addNewAlarmData();
            registerAlarm();
            Log.i(CLASS_NAME, "saveSetting$adding new alarm data completed!");
        } else {
            updateAlarmData();
            modifyAlarm();
            Log.i(CLASS_NAME, "saveSetting$updating existing alarm data completed!");
        }
        finish();
    }

    private void addNewAlarmData() {
        alarmData = new AlarmData(getNewId());
        setAlarmData();
        AlarmDatabaseUtil.insert(this, alarmData);
        sendResultToMainActivity("addNewAlarmData");
    }

    private int getNewId() {
        int curId = sharedPref.getInt("alarm_id", 0);
        sharedPref.edit().putInt("alarm_id", ++curId).apply();
        return curId;
    }

    private void updateAlarmData() {
        setAlarmData();
        AlarmDatabaseUtil.update(this, alarmData);
        sendResultToMainActivity("updateAlarmData");
    }

    private void setAlarmData() {
        alarmData.setAlarmTopic(selectedTopic);
        alarmData.setVoiceIdx(selectedVoiceIdx);
        alarmData.setPitch(slPitch.getValue());
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
