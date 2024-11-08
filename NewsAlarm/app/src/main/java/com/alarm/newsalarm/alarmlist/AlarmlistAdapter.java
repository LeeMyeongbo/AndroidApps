package com.alarm.newsalarm.alarmlist;

import android.annotation.SuppressLint;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alarm.newsalarm.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

public class AlarmlistAdapter extends RecyclerView.Adapter<AlarmlistAdapter.ViewHolder>
        implements ItemActionListener {

    private final ArrayList<Pair<String, Boolean>> alarmList;
    private final OnItemDragListener dragListener;
    private final OnItemClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView text;
        private final SwitchMaterial sm;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(
                @NonNull View itemView,
                OnItemClickListener clickListener,
                OnItemDragListener dragListener
        ) {
            super(itemView);
            text = itemView.findViewById(R.id.timeText);
            sm = itemView.findViewById(R.id.alarm_switch);

            itemView.setOnClickListener(v -> {
                int curPos = getAdapterPosition();
                if (curPos != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(itemView, curPos);
                }
            });

            itemView.setOnLongClickListener(v -> {
                dragListener.onStartDrag(this);
                return false;
            });
        }
    }

    public AlarmlistAdapter(
            ArrayList<Pair<String, Boolean>> dataset,
            OnItemClickListener clickListener,
            OnItemDragListener dragListener
    ) {
        alarmList = dataset;
        this.clickListener = clickListener;
        this.dragListener = dragListener;
    }

    @NonNull
    @Override
    public AlarmlistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.alarm_listviewitem, parent, false);

        return new ViewHolder(view, clickListener, dragListener);
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

    @Override
    public void onItemMoved(int from, int to) {
        if (from == to) {
            return;
        }

        Pair<String, Boolean> fromItem = alarmList.remove(from);
        alarmList.add(to, fromItem);
        notifyItemMoved(from, to);
    }

    @Override
    public void onItemSwiped(int position) {
        alarmList.remove(position);
        notifyItemRemoved(position);
    }
}
