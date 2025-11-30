package com.reminder.wifireminder;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class DaoAsyncTask extends AsyncTask<Routine_Table, Routine_Table, List<Routine_Table>> {

    private final Routine_Dao routine_dao;
    private final String task_type;

    public DaoAsyncTask(Routine_Dao routine_dao, String task_type) {
        this.routine_dao = routine_dao;
        this.task_type = task_type;
    }

    @Override
    protected List<Routine_Table> doInBackground(Routine_Table... routine_tables) {
        switch (task_type) {
            case "INSERT":
                routine_dao.insert(routine_tables[0]);
                break;
            case "DELETE":
                routine_dao.delete(routine_tables[0]);
                break;
            case "SELECT":
                ArrayList<Routine_Table> tables = new ArrayList<>();
                tables.add(routine_dao.select(routine_tables[0].BSSID));
                return tables;
            case "GET_ALL":
                return routine_dao.getAll();
            case "CLEAR":
                routine_dao.clear();
                break;
        }
        return null;
    }
}
