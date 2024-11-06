package com.reminder.wifi_reminder;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isChecked() {
        CheckBox cb = findViewById(R.id.checkbox);
        return cb.isChecked();
    }

    @Override
    public void setChecked(boolean checked) {
        CheckBox cb = findViewById(R.id.checkbox);
        if (cb.isChecked() != checked)
            cb.setChecked(checked);
    }

    @Override
    public void toggle() {
        CheckBox cb = findViewById(R.id.checkbox);
        setChecked(!cb.isChecked());
    }
}