package com.alarm.newsalarm.alarmlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.alarm.newsalarm.R;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;
import com.alarm.newsalarm.alarmlist.AlarmlistAdapter.AlarmListViewHolder;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AlarmlistAdapter extends Adapter<AlarmListViewHolder> implements ItemActionListener {

    private static final String[] WEEK = {"일", "월", "화", "수", "목", "금", "토"};

    private final Locale locale = new Locale("ko", "KR");
    private final ArrayList<AlarmData> alarmList;
    private final OnItemDragListener dragListener;
    private final OnItemClickListener clickListener;
    private Context context;

    public static class AlarmListViewHolder extends ViewHolder {

        private static final int[] TV_UNSELECTED_WEEK_IDS = {
            R.id.tvSunUnselected, R.id.tvMonUnselected, R.id.tvTuesUnselected,
            R.id.tvWednesUnselected, R.id.tvThursUnselected,
            R.id.tvFriUnselected, R.id.tvSaturUnselected
        };

        private static final int[] TV_SELECTED_WEEK_IDS = {
            R.id.tvSunSelected, R.id.tvMonSelected, R.id.tvTuesSelected, R.id.tvWednesSelected,
            R.id.tvThursSelected, R.id.tvFriSelected, R.id.tvSaturSelected
        };

        private static final int[] WEEK_IDS = {
            R.id.sunSelect, R.id.monSelect, R.id.tuesSelect, R.id.wednesSelect,
            R.id.thursSelect, R.id.friSelect, R.id.saturSelect
        };

        private final TextView tvTimeDeactivated;
        private final TextView tvTimeActivated;
        private final TextView tvDate;
        private final TextView[] tvUnselectedWeekdays = new TextView[7];
        private final TextView[] tvSelectedWeekdays = new TextView[7];
        private final View[] weekdaySelects = new View[7];
        private final SwitchMaterial switchAlarm;

        public AlarmListViewHolder(
            @NonNull View view, OnItemClickListener clickListener, OnItemDragListener dragListener
        ) {
            super(view);
            tvTimeDeactivated = view.findViewById(R.id.tvTimeDeactivated);
            tvTimeActivated = view.findViewById(R.id.tvTimeActivated);
            tvDate = view.findViewById(R.id.tvDate);
            switchAlarm = view.findViewById(R.id.switchAlarm);
            initWeekdayViews(view);

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

            switchAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    tvTimeDeactivated.setVisibility(TextView.GONE);
                    tvTimeActivated.setVisibility(TextView.VISIBLE);
                } else {
                    tvTimeDeactivated.setVisibility(TextView.VISIBLE);
                    tvTimeActivated.setVisibility(TextView.GONE);
                }
            });
        }

        private void initWeekdayViews(@NonNull View view) {
            for (int i = 0; i < 7; i++) {
                tvUnselectedWeekdays[i] = view.findViewById(TV_UNSELECTED_WEEK_IDS[i]);
                tvSelectedWeekdays[i] = view.findViewById(TV_SELECTED_WEEK_IDS[i]);
                weekdaySelects[i] = view.findViewById(WEEK_IDS[i]);
            }
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
    public AlarmListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater
            .from(context)
            .inflate(R.layout.alarm_listviewitem, parent, false);

        return new AlarmListViewHolder(view, clickListener, dragListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmListViewHolder holder, int position) {
        AlarmData data = alarmList.get(position);
        setTimeView(holder, data);
        setDateView(holder, data);

        holder.switchAlarm.setChecked(data.isActive());
    }

    private void setTimeView(AlarmListViewHolder holder, AlarmData data) {
        int hour = data.getTimeInMin() / 60, minute = data.getTimeInMin() % 60;
        String timeText = String.format(locale, "%02d:%02d", hour, minute);

        holder.tvTimeDeactivated.setText(timeText);
        holder.tvTimeActivated.setText(timeText);
    }

    private void setDateView(AlarmListViewHolder holder, AlarmData data) {
        StringBuilder tvDateTextStringBuilder = new StringBuilder();
        byte weekBit = data.getPeriodicWeekBit();
        if (weekBit > 0) {
            setPeriodicDateView(holder, weekBit, tvDateTextStringBuilder);
        } else {
            setSpecificDateView(data.getSpecificDateInMillis(), tvDateTextStringBuilder);
        }
        holder.tvDate.setText(tvDateTextStringBuilder.toString());
    }

    private void setPeriodicDateView(AlarmListViewHolder holder, byte weekBit, StringBuilder sb) {
        sb.append("매 주");
        for (int i = 0; i < 7; i++) {
            if ((weekBit & (1 << i)) == 0) {
                continue;
            }
            sb.append(" ").append(WEEK[i]);
            holder.tvUnselectedWeekdays[i].setVisibility(View.GONE);
            holder.tvSelectedWeekdays[i].setVisibility(View.VISIBLE);
            holder.weekdaySelects[i].setVisibility(View.VISIBLE);
        }
    }

    private void setSpecificDateView(long date, StringBuilder sb) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", locale);
        sb.append(format.format(new Date(date)));
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
