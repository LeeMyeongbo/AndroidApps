package com.alarm.newsalarm;

import android.graphics.Insets;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alarm.newsalarm.utils.LogUtil;

public class BaseActivity extends AppCompatActivity {

    protected final String CLASS_NAME;

    public BaseActivity(String className) {
        CLASS_NAME = className;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.logI(CLASS_NAME, "onCreate", "");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        LogUtil.logI(CLASS_NAME, "onStart", "");
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            Insets systemBarInsets = insets
                .getInsets(WindowInsetsCompat.Type.systemBars())
                .toPlatformInsets();
            v.setPadding(systemBarInsets.left, systemBarInsets.top, systemBarInsets.right,
                systemBarInsets.bottom);
            return insets;
        });
        super.onStart();
    }

    @Override
    protected void onResume() {
        LogUtil.logI(CLASS_NAME, "onResume", "");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.logI(CLASS_NAME, "onPause", "");
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.logI(CLASS_NAME, "onStop", "");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LogUtil.logI(CLASS_NAME, "onDestroy", "");
        super.onDestroy();
    }
}
