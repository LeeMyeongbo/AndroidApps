package com.reminder.wifireminder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ViewPageAdapter extends RecyclerView.Adapter<ViewPageAdapter.MyViewHolder> {

    private final ArrayList<Integer> images_id;
    private final ArrayList<String> permission_title, permission_content;
    private final Activity activity;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView p_title, p_content;

        public MyViewHolder(Activity activity, @NonNull View view) {
            super(view);
            imageView = view.findViewById(R.id.permission_img);
            p_title = view.findViewById(R.id.permission_title);
            p_content = view.findViewById(R.id.permission_content);

            Button button = view.findViewById(R.id.allow_button);
            button.setOnClickListener(v -> {
                if (p_title.getText().toString().equals("'Wifi Reminder'의 백그라운드 위치 정보 접근 허용 필요"))
                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            0
                    );
                else if (p_title.getText().toString().equals("'Wifi Reminder'의 쓰기 권한 허용 필요")) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + v.getContext().getPackageName()));
                    ((Activity)v.getContext()).startActivityForResult(intent, 1);
                } else if (p_title.getText().toString().equals("'Wifi Reminder'의 방해 금지 권한 허용 필요")) {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    ((Activity) v.getContext()).startActivityForResult(intent, 2);
                }
            });
        }

        public void bind_page(int img_id, String title, String content) {
            imageView.setImageResource(img_id);
            p_title.setText(title);
            p_content.setText(content);
        }
    }

    public ViewPageAdapter(
            Activity activity,
            ArrayList<Integer> image_id,
            ArrayList<String> title,
            ArrayList<String> content
    ) {
        this.activity = activity;
        this.images_id = image_id;
        this.permission_title = title;
        this.permission_content = content;
    }

    @NonNull
    @Override
    public ViewPageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.viewpager_item, parent, false);
        return new MyViewHolder(activity, view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind_page(
                images_id.get(position),
                permission_title.get(position),
                permission_content.get(position)
        );
    }

    @Override
    public int getItemCount() {
        return images_id.size();
    }
}
