package com.alarm.newsalarm.alarmlist;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemActionListener listener;

    public ItemTouchHelperCallback(ItemActionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getMovementFlags(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        int dragFlags = ItemTouchHelper.DOWN | ItemTouchHelper.UP;
        int swipeFlags = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;

        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {
        listener.onItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onItemSwiped(viewHolder.getAdapterPosition());
    }
}
