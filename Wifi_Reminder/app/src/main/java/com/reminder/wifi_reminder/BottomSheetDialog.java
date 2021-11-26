package com.reminder.wifi_reminder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class BottomSheetDialog extends BottomSheetDialogFragment {
    private final boolean is_night;
    private final List<Routine_Table> routine_list;
    private final CheckableListViewAdapter adapter;

    public BottomSheetDialog(boolean is_night, List<Routine_Table> routine_list, CheckableListViewAdapter adapter) {
        this.is_night = is_night;
        this.routine_list = routine_list;
        this.adapter = adapter;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences sp = requireContext().getSharedPreferences("test", Context.MODE_PRIVATE);
        View view;
        if (is_night)
            view = inflater.inflate(R.layout.bottomsheet_night, container, false);
        else
            view = inflater.inflate(R.layout.bottomsheet, container, false);

        ConstraintLayout c = view.findViewById(R.id.const_layout);
        c.setClipToOutline(true);

        RadioGroup criterion = view.findViewById(R.id.criteria);
        int std = sp.getInt("criteria", 1);         // 기본값은 생성 날짜 기준
        RadioButton button1;
        if (std == 0)
            button1 = view.findViewById(R.id.rt_title);
        else if (std == 1)
            button1 = view.findViewById(R.id.rt_generated);
        else
            button1 = view.findViewById(R.id.rt_modified);
        button1.setChecked(true);

        RadioGroup ways = view.findViewById(R.id.ways);
        RadioButton button2;
        if (sp.getBoolean("form", false))           // 기본값은 내림차순
            button2 = view.findViewById(R.id.ascending);
        else
            button2 = view.findViewById(R.id.descending);
        button2.setChecked(true);

        Button cancel = view.findViewById(R.id.sort_cancel);
        cancel.setOnClickListener(v -> dismiss());

        Button confirm = view.findViewById(R.id.sort_confirm);
        confirm.setOnClickListener(v -> {
            int id = criterion.getCheckedRadioButtonId();
            if (id == R.id.rt_title)
                sp.edit().putInt("criteria", 0).apply();        // 제목 기준이면 0으로 저장
            else if (id == R.id.rt_generated)
                sp.edit().putInt("criteria", 1).apply();        // 생성된 날짜 기준이면 1로 저장
            else
                sp.edit().putInt("criteria", 2).apply();        // 수정된 날짜 기준이면 2로 저장

            id = ways.getCheckedRadioButtonId();
            if (id == R.id.ascending)
                sp.edit().putBoolean("form", true).apply();     // 오름차순이면 true
            else
                sp.edit().putBoolean("form", false).apply();    // 내림차순 false

            int checked = sp.getInt("criteria", 1);
            boolean asc = sp.getBoolean("form", false);

            if (checked == 0) {
                if (asc)
                    routine_list.sort((o1, o2) -> o1.name.compareTo(o2.name));
                else
                    routine_list.sort((o1, o2) -> o2.name.compareTo(o1.name));
            } else if (checked == 1) {
                if (asc)
                    routine_list.sort((o1, o2) -> o1.g_date.compareTo(o2.g_date));
                else
                    routine_list.sort((o1, o2) -> o2.g_date.compareTo(o1.g_date));
            } else {
                if (asc)
                    routine_list.sort((o1, o2) -> o1.m_date.compareTo(o2.m_date));
                else
                    routine_list.sort((o1, o2) -> o2.m_date.compareTo(o1.m_date));
            }

            int count = adapter.getCount();
            for (int i = count - 1; i >= 0; i--)
                adapter.removeItem(i);
            for (Routine_Table rt : routine_list)
                adapter.addItem(rt.name, rt.g_date, rt.m_date);
            adapter.notifyDataSetChanged();
            dismiss();
        });
        return view;
    }
}