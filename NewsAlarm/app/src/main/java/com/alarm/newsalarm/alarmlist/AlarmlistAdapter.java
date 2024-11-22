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
import com.alarm.newsalarm.alarmmanager.AlarmSetter;
import com.alarm.newsalarm.database.AlarmData;
import com.alarm.newsalarm.database.AlarmDatabaseUtil;
import com.alarm.newsalarm.alarmlist.AlarmlistAdapter.AlarmListViewHolder;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class AlarmlistAdapter extends Adapter<AlarmListViewHolder> implements ItemActionListener {

    private static final String[] WEEK = {"일", "월", "화", "수", "목", "금", "토"};

    private final List<AlarmData> alarmList;
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
        private final Locale locale = new Locale("ko", "KR");

        private AlarmData curData;
        private TextView tvTimeDeactivated;
        private TextView tvTimeActivated;
        private TextView tvDateDeactivated;
        private TextView tvDateActivated;
        private SwitchMaterial switchAlarm;

        private final TextView[] tvUnselectedWeekdays = new TextView[7];
        private final TextView[] tvSelectedWeekdays = new TextView[7];
        private final View[] weekdaySelects = new View[7];

        public AlarmListViewHolder(
            @NonNull View view, OnItemClickListener clickListener, OnItemDragListener dragListener
        ) {
            super(view);
            initViews(view);

            setOnClick(view, clickListener);
            setOnLongClick(view, dragListener);
            setSwitchAlarmListener();
        }

        private void initViews(View view) {
            tvTimeDeactivated = view.findViewById(R.id.tvTimeDeactivated);
            tvTimeActivated = view.findViewById(R.id.tvTimeActivated);
            tvDateDeactivated = view.findViewById(R.id.tvDateUnselected);
            tvDateActivated = view.findViewById(R.id.tvDateSelected);
            switchAlarm = view.findViewById(R.id.switchAlarm);
            initWeekdayViews(view);
        }

        private void initWeekdayViews(View view) {
            for (int i = 0; i < 7; i++) {
                tvUnselectedWeekdays[i] = view.findViewById(TV_UNSELECTED_WEEK_IDS[i]);
                tvSelectedWeekdays[i] = view.findViewById(TV_SELECTED_WEEK_IDS[i]);
                weekdaySelects[i] = view.findViewById(WEEK_IDS[i]);
            }
        }

        private void setOnClick(View view, OnItemClickListener clickListener) {
            view.setOnClickListener(v -> {
                int curPos = getAdapterPosition();
                if (curPos != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(view, curPos);
                }
            });
        }

        private void setOnLongClick(View view, OnItemDragListener dragListener) {
            view.setOnLongClickListener(v -> {
                dragListener.onStartDrag(this);
                return false;
            });
        }

        private void setSwitchAlarmListener() {
            switchAlarm.setOnCheckedChangeListener((view, isChecked) -> {
                setTvByActivationChecked(isChecked);
                curData.setActive(isChecked);
                AlarmDatabaseUtil.update(view.getContext(), curData);
                setAlarmBySwitch(view, isChecked);
            });
        }

        private void setTvByActivationChecked(boolean isChecked) {
            if (isChecked) {
                setTvStatus(TextView.VISIBLE, TextView.GONE);
            } else {
                setTvStatus(TextView.GONE, TextView.VISIBLE);
            }
        }

        private void setTvStatus(int activationStatus, int deactivationStatus) {
            tvTimeActivated.setVisibility(activationStatus);
            tvDateActivated.setVisibility(activationStatus);
            tvTimeDeactivated.setVisibility(deactivationStatus);
            tvDateDeactivated.setVisibility(deactivationStatus);
            setTvWeekdayStatus(activationStatus, deactivationStatus);
        }

        private void setTvWeekdayStatus(int activationStatus, int deactivationStatus) {
            for (int i = 0; i < 7; i++) {
                if ((curData.getPeriodicWeekBit() & (1 << i)) > 0) {
                    tvSelectedWeekdays[i].setVisibility(activationStatus);
                    tvUnselectedWeekdays[i].setVisibility(deactivationStatus);
                }
            }
        }

        private void setAlarmBySwitch(View view, boolean isChecked) {
            AlarmSetter setter = new AlarmSetter(view.getContext());
            if (isChecked) {
                setter.registerAlarm(curData);
            } else {
                setter.cancelAlarm(curData);
            }
        }

        private void bind(AlarmData data) {
            curData = data;

            initWeekdayViews();
            setTimeView(data);
            setDateView(data);

            switchAlarm.setChecked(data.isActive());
            setTvByActivationChecked(switchAlarm.isChecked());
        }

        private void initWeekdayViews() {
            for (int i = 0; i < 7; i++) {
                tvSelectedWeekdays[i].setVisibility(TextView.GONE);
                tvUnselectedWeekdays[i].setVisibility(TextView.VISIBLE);
                weekdaySelects[i].setVisibility(View.GONE);
            }
        }

        private void setTimeView(AlarmData data) {
            String timeText = getAlarmTimeInfoText(data.getSpecificDateInMillis(), "HH:mm");

            tvTimeDeactivated.setText(timeText);
            tvTimeActivated.setText(timeText);
        }

        private String getAlarmTimeInfoText(long date, String format) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, locale);
            return dateFormat.format(new Date(date));
        }

        private void setDateView(AlarmData data) {
            String dateInfo;
            int weekBit = data.getPeriodicWeekBit();
            if (weekBit > 0) {
                setPeriodicDateView(weekBit);
                dateInfo = getPeriodicDateInfoText(weekBit);
            } else {
                dateInfo = getAlarmTimeInfoText(data.getSpecificDateInMillis(), "yyyy-MM-dd");
            }
            tvDateActivated.setText(dateInfo);
            tvDateDeactivated.setText(dateInfo);
        }

        private void setPeriodicDateView(int weekBit) {
            for (int i = 0; i < 7; i++) {
                if ((weekBit & (1 << i)) > 0) {
                    weekdaySelects[i].setVisibility(View.VISIBLE);
                } else {
                    weekdaySelects[i].setVisibility(View.GONE);
                }
            }
        }

        private String getPeriodicDateInfoText(int weekBit) {
            StringBuilder tvDateStringBuilder = new StringBuilder("매주");
            for (int i = 0; i < 7; i++) {
                if ((weekBit & (1 << i)) > 0) {
                    tvDateStringBuilder.append(" ").append(WEEK[i]);
                }
            }
            return tvDateStringBuilder.toString();
        }
    }

    public AlarmlistAdapter(
        List<AlarmData> dataset, OnItemClickListener clickListener, OnItemDragListener dragListener
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
        holder.bind(alarmList.get(position));
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
        notifyItemMoved(from, to);
    }

    @Override
    public void onItemSwiped(int position) {
        AlarmData data = alarmList.remove(position);
        AlarmDatabaseUtil.delete(context, data);
        new AlarmSetter(context).cancelAlarm(data);
        notifyItemRemoved(position);
    }

    public void addItem(AlarmData data) {
        ((LinkedList<AlarmData>) alarmList).push(data);
        notifyItemInserted(0);
    }

    public void updateItem(int position, AlarmData data) {
        alarmList.set(position, data);
        notifyItemChanged(position);
    }
}
