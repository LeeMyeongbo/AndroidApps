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

import java.util.Comparator;
import java.util.List;

public class BottomSheetDialog extends BottomSheetDialogFragment {

    private final boolean is_night;
    private final List<Routine_Table> routine_list;
    private final CheckableListViewAdapter adapter;

    public BottomSheetDialog(
            boolean is_night,
            List<Routine_Table> routine_list,
            CheckableListViewAdapter adapter
    ) {
        this.is_night = is_night;
        this.routine_list = routine_list;
        this.adapter = adapter;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        SharedPreferences sp = requireContext().getSharedPreferences("test", Context.MODE_PRIVATE);
        View view = is_night ? inflater.inflate(R.layout.bottomsheet_night, container, false)
                : inflater.inflate(R.layout.bottomsheet, container, false);

        ConstraintLayout c = view.findViewById(R.id.const_layout);
        c.setClipToOutline(true);

        RadioGroup criterion = view.findViewById(R.id.criteria);
        int std = sp.getInt("criteria", 1);
        RadioButton button1 = std == 0 ? view.findViewById(R.id.rt_title)
                : std == 1 ? view.findViewById(R.id.rt_generated)
                : view.findViewById(R.id.rt_modified);

        button1.setChecked(true);

        RadioGroup ways = view.findViewById(R.id.ways);
        RadioButton button2 = sp.getBoolean("form", false) ? view.findViewById(R.id.ascending)
                : view.findViewById(R.id.descending);
        button2.setChecked(true);

        Button cancel = view.findViewById(R.id.sort_cancel);
        cancel.setOnClickListener(v -> dismiss());

        Button confirm = view.findViewById(R.id.sort_confirm);
        confirm.setOnClickListener(v -> {
            int id = criterion.getCheckedRadioButtonId();
            if (id == R.id.rt_title)
                sp.edit().putInt("criteria", 0).apply();
            else if (id == R.id.rt_generated)
                sp.edit().putInt("criteria", 1).apply();
            else
                sp.edit().putInt("criteria", 2).apply();

            id = ways.getCheckedRadioButtonId();
            if (id == R.id.ascending)
                sp.edit().putBoolean("form", true).apply();
            else
                sp.edit().putBoolean("form", false).apply();

            int checked = sp.getInt("criteria", 1);
            boolean asc = sp.getBoolean("form", false);

            if (checked == 0) {
                if (asc)
                    routine_list.sort(Comparator.comparing(o -> o.name));
                else
                    routine_list.sort((o1, o2) -> o2.name.compareTo(o1.name));
            } else if (checked == 1) {
                if (asc)
                    routine_list.sort(Comparator.comparing(o -> o.g_date));
                else
                    routine_list.sort((o1, o2) -> o2.g_date.compareTo(o1.g_date));
            } else {
                if (asc)
                    routine_list.sort(Comparator.comparing(o -> o.m_date));
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