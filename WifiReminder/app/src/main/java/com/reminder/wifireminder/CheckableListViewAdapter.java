package com.reminder.wifireminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class CheckableListViewAdapter extends BaseAdapter {
    private final ArrayList<ListViewItem> itemlist = new ArrayList<>();
    private final boolean is_night;
    private boolean check_mode;
    private int title_padding_left;

    public CheckableListViewAdapter(boolean is_dark, boolean select_mode) {
        this.is_night = is_dark;
        this.check_mode = select_mode;
    }

    @Override
    public int getCount() {
        return itemlist.size();
    }

    @Override
    public Object getItem(int position) {
        return itemlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = is_night
                    ? inflater.inflate(R.layout.chk_listview_items_night, parent, false)
                    : inflater.inflate(R.layout.chk_listview_items, parent, false);
        }
        TextView title = convertView.findViewById(R.id.name);
        if (title.getPaddingLeft() != 0)
            title_padding_left = title.getPaddingLeft();
        CheckBox checkBox = convertView.findViewById(R.id.checkbox);
        TextView g_m_date = convertView.findViewById(R.id.g_m_date);

        if (check_mode) {
            checkBox.setVisibility(View.VISIBLE);
            title.setPadding(0, title.getPaddingTop(), title.getPaddingRight(), title.getPaddingBottom());
        } else {
            checkBox.setVisibility(View.GONE);
            title.setPadding(title_padding_left, title.getPaddingTop(), title.getPaddingRight(), title.getPaddingBottom());
        }

        ListViewItem item = itemlist.get(position);
        title.setText(item.getTitle());
        g_m_date.setText(item.getContent());

        return convertView;
    }

    public void addItem(String title, String g_date, String m_date) {
        ListViewItem item = new ListViewItem();
        item.setTitle(title);
        item.setContent("생성 : " + g_date + ", 수정 : " + m_date);

        itemlist.add(item);
    }

    public void removeItem(int index) {
        itemlist.remove(index);
    }

    public void toggleCheckBox(boolean l_click) {
        check_mode = l_click;
        notifyDataSetChanged();
    }
}
