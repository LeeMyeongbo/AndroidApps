package com.reminder.wifireminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Searched_ListViewAdapter extends BaseAdapter {

    private final ArrayList<ListViewItem> Item_list = new ArrayList<>();
    private final boolean is_night;

    public Searched_ListViewAdapter(boolean is_dark) {
        this.is_night = is_dark;
    }

    @Override
    public int getCount() {
        return Item_list.size();
    }

    @Override
    public Object getItem(int position) {
        return Item_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = is_night
                    ? inflater.inflate(R.layout.searched_listview_item_night, parent, false)
                    : inflater.inflate(R.layout.searched_listview_item, parent, false);
        }
        TextView title = convertView.findViewById(R.id.searched_result);

        ListViewItem item = Item_list.get(position);
        title.setText(item.getTitle());

        return convertView;
    }

    public void addItem(String title) {
        ListViewItem item = new ListViewItem();
        item.setTitle(title);

        Item_list.add(item);
    }

    public void removeItem(int index) {
        Item_list.remove(index);
    }

}
