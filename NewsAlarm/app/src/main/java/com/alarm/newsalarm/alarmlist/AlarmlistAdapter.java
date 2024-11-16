package com.alarm.newsalarm.alarmlist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alarm.newsalarm.R;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Locale;

public class AlarmlistAdapter extends RecyclerView.Adapter<AlarmlistAdapter.ViewHolder>
        implements ItemActionListener {

    private final ArrayList<AlarmData> alarmList;
    private final OnItemDragListener dragListener;
    private final OnItemClickListener clickListener;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView text;
        private final SwitchMaterial sm;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(
            @NonNull View view, OnItemClickListener clickListener, OnItemDragListener dragListener
        ) {
            super(view);
            text = view.findViewById(R.id.tvTime);
            sm = view.findViewById(R.id.switchAlarm);

            view.setOnClickListener(v -> {
                int curPos = getAdapterPosition();
                if (curPos != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(view, curPos);
                }
            });

            view.setOnLongClickListener(v -> {
                dragListener.onStartDrag(this);
                return false;
            });
        }
    }

    public AlarmlistAdapter(
        ArrayList<AlarmData> dataset,
        OnItemClickListener clickListener,
        OnItemDragListener dragListener
    ) {
        this.alarmList = dataset;
        this.clickListener = clickListener;
        this.dragListener = dragListener;
    }

    @NonNull
    @Override
    public AlarmlistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater
            .from(context)
            .inflate(R.layout.alarm_listviewitem, parent, false);

        return new ViewHolder(view, clickListener, dragListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmlistAdapter.ViewHolder holder, int position) {
        AlarmData data = alarmList.get(position);
        Locale locale = new Locale("ko", "KR");
        int hour = data.getTimeInMin() / 60, minute = data.getTimeInMin() % 60;
        String timeText = String.format(locale, "%02d:%02d", hour, minute);

        holder.text.setText(timeText);
        holder.sm.setChecked(data.isActive());
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    @Override
    public void onItemMoved(int from, int to) {
        if (from == to) {
            return;
        }
        AlarmData fromItem = alarmList.remove(from);
        alarmList.add(to, fromItem);
        /* To Do : update order in room also */
        notifyItemMoved(from, to);
    }

    @Override
    public void onItemSwiped(int position) {
        AlarmDatabaseUtil.delete(context, alarmList.remove(position));
        notifyItemRemoved(position);
    }

    public void addItem(AlarmData data) {
        alarmList.add(data);
        notifyItemInserted(alarmList.size() - 1);
    }
}
