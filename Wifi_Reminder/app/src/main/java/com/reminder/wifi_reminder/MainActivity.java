package com.reminder.wifi_reminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sp;
    private Routine_Database rd;
    private ImageButton Register, Setting, Search, Delete;
    private Button start_service, stop_service;
    private List<Routine_Table> routine_list;
    private ListView listview;
    private CheckableListViewAdapter adapter;
    private CheckBox select_all;
    private TextView edit;
    private PopupWindow popupmenu;
    private boolean check_mode, is_night;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rd = Routine_Database.getRoutineDatabase(getApplicationContext());
        sp = getSharedPreferences("test", MODE_PRIVATE);

        if (sp.getInt("review", 0) == 25 && !sp.getBoolean("reviewed", false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.myDialogTheme)
                    .setPositiveButton("리뷰 작성하기", (dialog, which) -> {
                        ReviewManager manager = ReviewManagerFactory.create(this);
                        Task<ReviewInfo> request = manager.requestReviewFlow();
                        request.addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                ReviewInfo reviewInfo = task.getResult();
                                manager.launchReviewFlow(this, reviewInfo);
                            }
                        });
                        sp.edit().putBoolean("reviewed", true).apply();
                        dialog.dismiss();
                    })
                    .setNeutralButton("나중에", ((dialog, which) -> dialog.dismiss()))
                    .setNegativeButton("괜찮습니다.", ((dialog, which) -> {
                        sp.edit().putBoolean("reviewed", true).apply();
                        dialog.dismiss();
                    }))
                    .setTitle("현재 앱이 어떠세요?")
                    .setMessage("현재 사용하시는 앱에 대해서 리뷰를 남겨주세요~");
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialog1 -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.WHITE);
            });
            dialog.show();
            sp.edit().putInt("review", -1).apply();
        }
        sp.edit().putInt("review", sp.getInt("review", 0) + 1).apply();

        is_night = sp.getBoolean("is_night", false);
        if (is_night) {
            setTheme(R.style.NightTheme);
            setContentView(R.layout.activity_main_night);
        }
        else {
            setTheme(R.style.DayTheme);
            setContentView(R.layout.activity_main);
        }

        if (!Settings.canDrawOverlays(getApplicationContext()))
            sp.edit().putBoolean("msg_show", false).apply();

        start_service = findViewById(R.id.start_remind);
        start_service.setOnClickListener(v -> {
            boolean chk = !sp.getBoolean("notify", false);
            sp.edit().putBoolean("notify", chk).apply();

            start_service.setVisibility(View.GONE);
            stop_service.setVisibility(View.VISIBLE);

            Intent intent = new Intent(getApplicationContext(), MyService.class);
            Toast.makeText(this, "Remind 기능이 설정되었습니다.", Toast.LENGTH_SHORT).show();
            startService(intent);
            sp.edit().putBoolean("not_show_again", false).apply();
        });
        start_service.getViewTreeObserver().addOnGlobalLayoutListener(my_listener);

        stop_service = findViewById(R.id.stop_remind);
        stop_service.setOnClickListener(v -> {
            boolean chk = !sp.getBoolean("notify", false);
            sp.edit().putBoolean("notify", chk).apply();

            start_service.setVisibility(View.VISIBLE);
            stop_service.setVisibility(View.GONE);

            Intent intent = new Intent(getApplicationContext(), MyService.class);
            Toast.makeText(this, "Remind 기능이 해제되었습니다.", Toast.LENGTH_SHORT).show();
            stopService(intent);
            sp.edit().putBoolean("not_show_again", true).apply();
        });

        if (sp.getBoolean("notify", false)) {    // 현재 서비스 실행 중이라면
            start_service.setVisibility(View.GONE);
            stop_service.setVisibility(View.VISIBLE);
        } else {                                            // 그렇지 않다면
            start_service.setVisibility(View.VISIBLE);
            stop_service.setVisibility(View.GONE);
        }

        setPopupWindow();
        Register = findViewById(R.id.add_Button);
        Register.setOnClickListener(this);
        Search = findViewById(R.id.search_Button);
        Search.setOnClickListener(this);
        Setting = findViewById(R.id.setting_Button);
        Setting.setOnClickListener(this);
        select_all = findViewById(R.id.select_all);
        Delete = findViewById(R.id.delete_Button);
        Delete.setOnClickListener(this);

        try {
            routine_list = new DaoAsyncTask(rd.routine_dao(), "GET_ALL").execute().get();
            int criteria = sp.getInt("criteria", 1);
            boolean asc = sp.getBoolean("form", false);                 // 기본값은 생성 날짜 내림차순으로 함

            if (criteria == 0) {                                                            // 루틴 이름 기준으로
                if (asc)
                    routine_list.sort((o1, o2) -> o1.name.compareTo(o2.name));              // 오름차순 정렬
                else
                    routine_list.sort((o1, o2) -> o2.name.compareTo(o1.name));              // 내림차순 정렬
            } else if (criteria == 1) {                                                     // 생성 날짜 기준으로
                if (asc)
                    routine_list.sort((o1, o2) -> o1.g_date.compareTo(o2.g_date));          // 오름차순 정렬
                else
                    routine_list.sort((o1, o2) -> o2.g_date.compareTo(o1.g_date));          // 내림차순 정렬
            } else {                                                                        // 수정 날짜 기준으로
                if (asc)
                    routine_list.sort((o1, o2) -> o1.m_date.compareTo(o2.m_date));          // 오름차순 정렬
                else
                    routine_list.sort((o1, o2) -> o2.m_date.compareTo(o1.m_date));          // 내림차순 정렬
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        listview = findViewById(R.id.checkable_list);
        adapter = new CheckableListViewAdapter(is_night, check_mode);
        for (Routine_Table r : routine_list)
            adapter.addItem(r.name, r.g_date, r.m_date);

        listview.setOnItemClickListener((parent, view, position, id) -> {
            if (!check_mode) {
                Intent it = new Intent(this, Routine.class);
                it.putExtra("selected", true);
                it.putExtra("routine", routine_list.get(position));
                startActivity(it);
                finish();
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        });
        listview.setOnItemLongClickListener((parent, view, position, id) -> {
            if (!check_mode)
                edit.performClick();
            return true;
        });
        listview.setAdapter(adapter);
        select_all.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int count = adapter.getCount();
            for (int i = 0; i < count; i++)
                listview.setItemChecked(i, isChecked);
        });

        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        boolean ignoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        TextView caution = findViewById(R.id.caution);
        if (!ignoring) {
            caution.setVisibility(View.VISIBLE);
        } else
            caution.setVisibility(View.GONE);
    }

    private final ViewTreeObserver.OnGlobalLayoutListener my_listener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int height = start_service.getHeight();
            boolean is_all_granted = true;
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            ArrayList<Integer> img_id = new ArrayList<>();
            ArrayList<String> p_title = new ArrayList<>();
            ArrayList<String> p_content = new ArrayList<>();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                img_id.add(R.drawable.location);
                String title = "'Wifi Reminder'의 백그라운드 위치 정보 접근 허용 필요";
                String content = "이 앱은 연결된 와이파이 정보에 실시간으로 접근하여 사용자가 지정한 대로 휴대폰을 자동 설정하기 위해 앱이 사용 중이지 않을 때에도 사용자 위치 정보를 필요로 합니다. \n항상 허용을 선택하셔서 해당 앱의 백그라운드 위치 정보 접근을 허용해주세요.";
                p_title.add(title);
                p_content.add(content);
                is_all_granted = false;
            }
            if (!Settings.System.canWrite(getApplicationContext())) {
                img_id.add(R.drawable.write_setting);
                String title = "'Wifi Reminder'의 쓰기 권한 허용 필요";
                String content = "이 앱은 연결된 와이파이 정보에 따라 볼륨, 밝기 등을 자동으로 제어할 수 있습니다. 앱의 정상적인 작동을 위해 해당 권한을 허용해 주세요.";
                p_title.add(title);
                p_content.add(content);
                is_all_granted = false;
            }
            if (!manager.isNotificationPolicyAccessGranted()) {
                img_id.add(R.drawable.no_banghae);
                String title = "'Wifi Reminder'의 방해 금지 권한 허용 필요";
                String content = "이 앱은 사용자 설정에 따라 진동 및 무음 모드로 자동 설정할 수 있습니다. 앱의 정상적인 작동을 위해 해당 권한을 허용해 주세요.";
                p_title.add(title);
                p_content.add(content);
                is_all_granted = false;
            }
            if (!is_all_granted)
                start_viewpager2(img_id, p_title, p_content);
            if (is_all_granted && sp.getBoolean("new_here", true)) {
                Intent intent = new Intent(getApplicationContext(), HowToUse.class);
                intent.putExtra("button_height", height);
                startActivity(intent);
            }
            removeOnGlobalLayoutListener(start_service.getViewTreeObserver(), my_listener);
        }
    };

    private void start_viewpager2(ArrayList<Integer> img_id, ArrayList<String> p_title, ArrayList<String> p_content) {
        Intent intent = new Intent(this, Permission_Viewpager.class);
        intent.putIntegerArrayListExtra("img_ids", img_id);
        intent.putStringArrayListExtra("titles", p_title);
        intent.putStringArrayListExtra("contents", p_content);
        startActivity(intent);
        finish();
    }

    private static void removeOnGlobalLayoutListener(ViewTreeObserver observer, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (observer != null)
            observer.removeOnGlobalLayoutListener(listener);
    }

    @SuppressLint("InflateParams")
    public void setPopupWindow() {
        View view;
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (is_night)
            view = inflater.inflate(R.layout.popupmenu_night, null);
        else
             view = inflater.inflate(R.layout.popupmenu, null);

        TextView set = view.findViewById(R.id.set);
        edit = view.findViewById(R.id.edit);
        TextView sort = view.findViewById(R.id.sort);
        TextView review = view.findViewById(R.id.review);
        popupmenu = new PopupWindow(view, 450, RelativeLayout.LayoutParams.WRAP_CONTENT, true);

        set.setOnClickListener(v -> {
            Intent intent = new Intent(this, Setting.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        });

        edit.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.from_up_to_down);
            Register.setVisibility(View.GONE);
            Search.setVisibility(View.GONE);
            Setting.setVisibility(View.GONE);
            if (sp.getBoolean("notify", false)) {
                stop_service.startAnimation(anim);
                stop_service.setVisibility(View.GONE);
            } else {
                start_service.startAnimation(anim);
                start_service.setVisibility(View.GONE);
            }
            anim = AnimationUtils.loadAnimation(this, R.anim.from_down_to_up);
            Delete.setVisibility(View.VISIBLE);
            Delete.startAnimation(anim);

            select_all.setVisibility(View.VISIBLE);
            check_mode = true;
            adapter.toggleCheckBox(true);
            popupmenu.dismiss();
        });
        
        sort.setOnClickListener(v -> {
            popupmenu.dismiss();
            new BottomSheetDialog(is_night, routine_list, adapter).show(getSupportFragmentManager(), "bottomSheet");
        });

        review.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.reminder.wifi_reminder"));
            intent.setPackage("com.android.vending");
            startActivity(intent);
        });
    }

    @Override
    public void onClick(View v) {
        if (v == Register) {
            Intent intent = new Intent(this, Routine.class);
            intent.putExtra("selected", false);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        } else if (v == Search) {
            Intent intent = new Intent(this, Search.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.appear, R.anim.disappear);
        } else if (v == Setting) {
            popupmenu.showAsDropDown(v, 0, -v.getHeight(), Gravity.END);
        } else if (v == Delete) {
            if (listview.getCheckedItemCount() == 0)
                Toast.makeText(this, "하나 이상 선택해 주세요", Toast.LENGTH_SHORT).show();
            else if (!sp.getBoolean("not_show", false))
                show_DeleteDialog();
            else
                delete_from_List();
        }
    }

    @SuppressLint("InflateParams")
    public void show_DeleteDialog() {
        View dialogView;
        if (is_night)
            dialogView = getLayoutInflater().inflate(R.layout.delete_dialog_night, null);
        else
            dialogView = getLayoutInflater().inflate(R.layout.delete_dialog, null);
        CheckBox not_show = dialogView.findViewById(R.id.not_show_again);
        TextView cancel = dialogView.findViewById(R.id.cancel);
        TextView confirm = dialogView.findViewById(R.id.confirm);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        cancel.setOnClickListener(v -> alertDialog.dismiss());
        confirm.setOnClickListener(v -> {
            sp.edit().putBoolean("not_show", not_show.isChecked()).apply();
            delete_from_List();
            alertDialog.dismiss();
        });
    }

    public void delete_from_List() {
        SparseBooleanArray checkedItems = listview.getCheckedItemPositions();
        int count = adapter.getCount();

        for (int i = count - 1; i >= 0; i--) {
            if (checkedItems.get(i)) {
                new DaoAsyncTask(rd.routine_dao(), "DELETE").execute(routine_list.get(i));
                adapter.removeItem(i);
                routine_list.remove(i);
            }
        }
        onBackPressed();
        Toast.makeText(this, "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (check_mode) {
            listview.clearChoices();
            check_mode = false;
            select_all.setChecked(false);
            select_all.setVisibility(View.GONE);
            adapter.toggleCheckBox(false);

            Animation anim = AnimationUtils.loadAnimation(this, R.anim.from_up_to_down);
            anim.setInterpolator(new AccelerateInterpolator());
            Delete.startAnimation(anim);
            Delete.setVisibility(View.GONE);

            Register.setVisibility(View.VISIBLE);
            Search.setVisibility(View.VISIBLE);
            Setting.setVisibility(View.VISIBLE);

            anim = AnimationUtils.loadAnimation(this, R.anim.from_down_to_up);
            if (sp.getBoolean("notify", false)) {    // 현재 서비스 실행 중이라면
                stop_service.startAnimation(anim);
                stop_service.setVisibility(View.VISIBLE);
            } else {                                            // 그렇지 않다면
                start_service.startAnimation(anim);
                start_service.setVisibility(View.VISIBLE);
            }
        } else if (popupmenu.isShowing())
            popupmenu.dismiss();
        else
            finish();
    }
}