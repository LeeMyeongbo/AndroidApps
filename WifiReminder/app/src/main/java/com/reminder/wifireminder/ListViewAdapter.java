package com.reminder.wifireminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    private final boolean is_night;
    private final ArrayList<ListViewItem> listViewItemlist = new ArrayList<>();

    public ListViewAdapter(boolean is_dark) {
        this.is_night = is_dark;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = is_night
                    ? inflater.inflate(R.layout.listview_item_night, parent, false)
                    : inflater.inflate(R.layout.listview_item, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.title);
        TextView contentTextView = convertView.findViewById(R.id.content);
        ListViewItem listViewItem = listViewItemlist.get(position);

        titleTextView.setText(listViewItem.getTitle());
        contentTextView.setText(listViewItem.getContent());

        return convertView;
    }

    @Override
    public int getCount() {
        return listViewItemlist.size();
    }

    @Override
    public Object getItem(int position) {
        return listViewItemlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(String title, String content) {
        ListViewItem item = new ListViewItem();
        item.setTitle(title);
        item.setContent(content);

        listViewItemlist.add(item);
    }
}
