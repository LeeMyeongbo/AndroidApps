package com.reminder.wifireminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Preview extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("test", MODE_PRIVATE);
        boolean is_night = sp.getBoolean("is_night", false);
        if (is_night) {
            setTheme(R.style.NightTheme);
            setContentView(R.layout.preview_night);
        }
        else {
            setTheme(R.style.DayTheme);
            setContentView(R.layout.preview);
        }
        
        Intent it = new Intent(this, MainActivity.class);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            it.putExtra("saved", 0);
            startActivity(it);
            finish();
        }, 500);
    }
}
