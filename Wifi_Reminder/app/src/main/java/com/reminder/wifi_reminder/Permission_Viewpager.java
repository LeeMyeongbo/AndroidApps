package com.reminder.wifi_reminder;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class Permission_Viewpager extends AppCompatActivity {

    private LinearLayout layoutIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.DayTheme);
        setContentView(R.layout.permission_viewpager);

        ViewPager2 viewpager = findViewById(R.id.sliderViewPager);
        layoutIndicator = findViewById(R.id.layoutIndicators);

        ArrayList<Integer> img_ids = getIntent().getIntegerArrayListExtra("img_ids");
        ArrayList<String> titles = getIntent().getStringArrayListExtra("titles");
        ArrayList<String> contents = getIntent().getStringArrayListExtra("contents");
        ViewPageAdapter adapter = new ViewPageAdapter(this, img_ids, titles, contents);

        viewpager.setOffscreenPageLimit(1);
        viewpager.setAdapter(adapter);
        viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });
        setupIndicators(img_ids.size());
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    4);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (requestCode == 1 && Settings.System.canWrite(this)) {
            Toast.makeText(this, "쓰기 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
            Intent it = new Intent(this, MainActivity.class);
            startActivity(it);
            finish();
        } else if (requestCode == 2 && manager.isNotificationPolicyAccessGranted()) {
            Toast.makeText(this, "방해 금지 설정 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
            Intent it = new Intent(this, MainActivity.class);
            startActivity(it);
            finish();
        }
    }

    private void setupIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bg_indicator_inactive));
            indicators[i].setLayoutParams(params);
            layoutIndicator.addView(indicators[i]);
        }

        setCurrentIndicator(0);
    }

    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView)layoutIndicator.getChildAt(i);
            if (i == position)
                imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bg_indicator_active));
            else
                imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bg_indicator_inactive));
        }
    }
}