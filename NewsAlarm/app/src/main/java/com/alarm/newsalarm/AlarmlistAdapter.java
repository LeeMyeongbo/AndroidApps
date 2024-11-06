package com.alarm.newsalarm;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

public class AlarmlistAdapter extends RecyclerView.Adapter<AlarmlistAdapter.ViewHolder> {

    private final ArrayList<Pair<String, Boolean>> alarmList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView text;
        private final SwitchMaterial sm;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.timeText);
            sm = itemView.findViewById(R.id.alarm_switch);
        }
    }

    public AlarmlistAdapter(ArrayList<Pair<String, Boolean>> dataset) {
        alarmList = dataset;
    }

    @NonNull
    @Override
    public AlarmlistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.alarm_listviewitem, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmlistAdapter.ViewHolder holder, int position) {
        String text = alarmList.get(position).first;
        boolean checked = alarmList.get(position).second;

        holder.text.setText(text);
        holder.sm.setChecked(checked);
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }
}
