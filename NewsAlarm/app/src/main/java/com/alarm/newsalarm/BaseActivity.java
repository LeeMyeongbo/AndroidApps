package com.alarm.newsalarm;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected final String CLASS_NAME;

    public BaseActivity(String className) {
        CLASS_NAME = className;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(CLASS_NAME, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        Log.i(CLASS_NAME, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(CLASS_NAME, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(CLASS_NAME, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(CLASS_NAME, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(CLASS_NAME, "onDestroy");
        super.onDestroy();
    }
}
