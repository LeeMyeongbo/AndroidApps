package com.reminder.wifireminder;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class HowToUse extends AppCompatActivity {

    private int clicked_num;
    private TextView add_explain, setting_explain, search_explain, list_explain, service_explain;
    private Button prev, next, service_button;
    private ImageButton add_button, setting_button, search_button;
    private View routine_listview;
    private SharedPreferences sp;
    private float alpha;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_how_to_use);

        ConstraintLayout layout = findViewById(R.id.clayout);
        alpha = layout.getAlpha();
        sp = getSharedPreferences("test", MODE_PRIVATE);
        add_explain = findViewById(R.id.explain_add);
        setting_explain = findViewById(R.id.explain_setting);
        search_explain = findViewById(R.id.explain_search);
        list_explain = findViewById(R.id.explain_routine);
        service_explain = findViewById(R.id.explain_service);

        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        add_button = findViewById(R.id.add_Button);
        setting_button = findViewById(R.id.setting_Button);
        search_button = findViewById(R.id.search_Button);
        routine_listview = findViewById(R.id.routine_space);
        service_button = findViewById(R.id.service_button);
        int height = getIntent().getIntExtra("button_height", 100);
        service_button.setHeight(height);

        prev.setOnClickListener(v -> {
            clicked_num--;
            explain();
        });
        next.setOnClickListener(v -> {
            clicked_num++;
            explain();
        });
    }

    @SuppressLint("SetTextI18n")
    public void explain() {
        switch(clicked_num) {
            case 0:
                prev.setVisibility(View.GONE);
                next.setText("How to use");
                add_button.setVisibility(View.INVISIBLE);
                add_explain.setVisibility(View.GONE);
                break;
            case 1:
                prev.setVisibility(View.VISIBLE);
                next.setText("다음");
                add_button.setVisibility(View.VISIBLE);
                add_explain.setVisibility(View.VISIBLE);
                setting_button.setVisibility(View.INVISIBLE);
                setting_explain.setVisibility(View.GONE);
                break;
            case 2:
                add_button.setVisibility(View.INVISIBLE);
                add_explain.setVisibility(View.GONE);
                setting_button.setVisibility(View.VISIBLE);
                setting_explain.setVisibility(View.VISIBLE);
                search_button.setVisibility(View.INVISIBLE);
                search_explain.setVisibility(View.GONE);
                break;
            case 3:
                setting_button.setVisibility(View.INVISIBLE);
                setting_explain.setVisibility(View.GONE);
                search_button.setVisibility(View.VISIBLE);
                search_explain.setVisibility(View.VISIBLE);
                routine_listview.setVisibility(View.GONE);
                list_explain.setVisibility(View.GONE);
                break;
            case 4:
                search_button.setVisibility(View.INVISIBLE);
                search_explain.setVisibility(View.GONE);
                routine_listview.setVisibility(View.VISIBLE);
                list_explain.setVisibility(View.VISIBLE);
                service_button.setAlpha(alpha);
                service_explain.setVisibility(View.GONE);
                next.setText("다음");
                break;
            case 5:
                routine_listview.setVisibility(View.GONE);
                list_explain.setVisibility(View.GONE);
                service_button.setAlpha(0f);
                service_explain.setVisibility(View.VISIBLE);
                next.setText("시작하기");
                break;
            case 6:
                sp.edit().putBoolean("new_here", false).apply();
                setResult(RESULT_OK);
                finish();
        }
    }
}
