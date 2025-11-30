package com.reminder.wifireminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Search extends AppCompatActivity {

    private Routine_Database rd;
    private List<Routine_Table> routine_list;
    private ArrayList<Routine_Table> show_list;
    private Searched_ListViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("test", MODE_PRIVATE);
        rd = Routine_Database.getRoutineDatabase(getApplicationContext());
        boolean is_night = sp.getBoolean("is_night", false);
        if (is_night) {
            setTheme(R.style.NightTheme);
            setContentView(R.layout.search_night);
        } else {
            setTheme(R.style.DayTheme);
            setContentView(R.layout.search);
        }

        try {
            routine_list = new DaoAsyncTask(rd.routine_dao(), "GET_ALL").execute().get();
            routine_list.sort(Comparator.comparing(o -> o.name));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        show_list = new ArrayList<>();
        ListView result_list = findViewById(R.id.result_list);
        adapter = new Searched_ListViewAdapter(is_night);

        EditText search = findViewById(R.id.search_bar);
        search.requestFocus();
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String typed = Objects.requireNonNull(search.getText()).toString();
                int size = show_list.size();
                for (int i = size - 1; i >= 0; i--) {
                    adapter.removeItem(i);
                    show_list.remove(i);
                }

                for (Routine_Table rt : routine_list) {
                    if (!typed.isEmpty() && rt.name.toLowerCase().contains(typed)) {
                        adapter.addItem(rt.name);
                        show_list.add(rt);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        result_list.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Routine_Table selected = new DaoAsyncTask(rd.routine_dao(), "SELECT")
                        .execute(show_list.get(position))
                        .get()
                        .get(0);
                Intent it = new Intent(this, Routine.class);
                it.putExtra("selected", true);
                it.putExtra("routine", selected);
                startActivity(it);
                finish();
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        result_list.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.appear, R.anim.disappear);
    }
}
