package com.reminder.wifi_reminder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Routine extends AppCompatActivity {

    private Routine_Table routine_table;
    private TextInputLayout name;
    private TextInputEditText name_tag;
    private RadioGroup sound_mode;
    private EditText list;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getSharedPreferences("test", MODE_PRIVATE);
        boolean is_night = pref.getBoolean("is_night", false);
        if (is_night) {
            setTheme(R.style.NightTheme);
            setContentView(R.layout.routine_night);
        }
        else {
            setTheme(R.style.DayTheme);
            setContentView(R.layout.routine);
        }

        name = findViewById(R.id.name);
        name.requestFocus();
        name_tag = findViewById(R.id.input);
        name_tag.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(name_tag.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        int origin_color = name.getBoxStrokeColor();
        name_tag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text.length() == 20) {
                    name.setBoxStrokeColor(Color.RED);
                    name.setCounterTextColor(ColorStateList.valueOf(Color.RED));
                    name.setEndIconTintList(ColorStateList.valueOf(Color.RED));
                } else {
                    name.setBoxStrokeColor(origin_color);
                    name.setEndIconTintList(ColorStateList.valueOf(origin_color));
                    name.setCounterTextColor(ColorStateList.valueOf(origin_color));
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        TextInputEditText wifi_info = findViewById(R.id.wifi);
        list = findViewById(R.id.todo_list);
        SeekBar volume = findViewById(R.id.volume_Seekbar);
        SeekBar gamma = findViewById(R.id.gamma_Seekbar);
        sound_mode = findViewById(R.id.choice);
        RadioButton silent_button = findViewById(R.id.mute);

        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        silent_button.setEnabled(manager.isNotificationPolicyAccessGranted());

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int sound_max = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

        int cur_brightness = 0;
        try {
            cur_brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        boolean selected = getIntent().getBooleanExtra("selected", false);
        Date currentTime = Calendar.getInstance().getTime();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(currentTime);

        if (selected) {
            routine_table = getIntent().getParcelableExtra("routine");
            name_tag.setText(routine_table.name);
            name_tag.setSelection(routine_table.name.length());

            if (routine_table.sound_mode == AudioManager.RINGER_MODE_NORMAL) {
                sound_mode.check(R.id.sound);
                volume.setEnabled(true);
            }
            else if (routine_table.sound_mode == AudioManager.RINGER_MODE_VIBRATE) {
                sound_mode.check(R.id.vibrate);
                volume.setEnabled(false);
            }
            else {
                sound_mode.check(R.id.mute);
                volume.setEnabled(false);
            }

            list.setText(routine_table.Todo_list);
            routine_table.m_date = date;
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            routine_table = new Routine_Table("", "", null, 0, 0, 0, "", "", "");
            WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                routine_table.SSID = wifiManager.getConnectionInfo().getSSID();
                String tmp = wifiManager.getConnectionInfo().getBSSID();
                if (tmp != null)
                    routine_table.BSSID = tmp;
            }
            if (routine_table.BSSID.isEmpty())
                showDialog();

            routine_table.volume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            routine_table.gamma = cur_brightness;
            routine_table.m_date = routine_table.g_date = date;
        }
        wifi_info.setText(routine_table.SSID);

        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        sound_mode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.sound) {
                volume.setEnabled(true);
                volume.setProgress(routine_table.volume);
            } else {
                if (checkedId == R.id.vibrate) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, 50));
                }
                volume.setEnabled(false);
                volume.setProgress(0);
                routine_table.volume = 0;
            }
        });

        AudioAttributes aud = new AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_RING)
                .build();
        SoundPool sp = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(aud)
                .build();
        int sound_effect = sp.load(this, R.raw.soundeffect, 1);

        volume.setMax(sound_max);
        volume.setProgress(routine_table.volume);
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                routine_table.volume = progress;
                sp.play(sound_effect, (float)progress / volume.getMax(),
                        (float)progress / volume.getMax(), 0, 0, 1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        TextView warning = findViewById(R.id.warning);
        try {
            if (Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == 1)
                warning.setVisibility(View.VISIBLE);
            else
                warning.setVisibility(View.GONE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        WindowManager.LayoutParams params = getWindow().getAttributes();
        gamma.setMax(255);
        gamma.setProgress(routine_table.gamma);
        gamma.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                params.screenBrightness = progress / 255f;
                routine_table.gamma = progress;
                getWindow().setAttributes(params);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    public void showDialog() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("와이파이가 연결되었을 때 추가할 수 있습니다!")
                .setPositiveButton("확인", (dialog, which) -> onBackPressed());

        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.setCanceledOnTouchOutside(false);
        msgDlg.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK)
                onBackPressed();

            return true;
        });
        msgDlg.setOnShowListener(
                dialog -> msgDlg.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
        );
        msgDlg.show();
    }

    public void just_exit(View v) {
        onBackPressed();
    }

    public void save_and_exit(View v) {
        Routine_Database rd = Routine_Database.getRoutineDatabase(getApplicationContext());
        if (!Objects.requireNonNull(name_tag.getText()).toString().isEmpty()) {
            routine_table.name = name_tag.getText().toString();

            if (sound_mode.getCheckedRadioButtonId() == R.id.sound)
                routine_table.sound_mode = AudioManager.RINGER_MODE_NORMAL;
            else if (sound_mode.getCheckedRadioButtonId() == R.id.vibrate)
                routine_table.sound_mode = AudioManager.RINGER_MODE_VIBRATE;
            else
                routine_table.sound_mode = AudioManager.RINGER_MODE_SILENT;

            routine_table.Todo_list = list.getText().toString();
            new DaoAsyncTask(rd.routine_dao(), "INSERT").execute(routine_table);

            onBackPressed();
        } else
            name_tag.setError("루틴 이름을 입력해야 합니다.");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent it = new Intent(this, MainActivity.class);
        startActivity(it);
        finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
}