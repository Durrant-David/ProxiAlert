package edu.byui.team06.proxialert.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.ProxiDB;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    Context contextBootReceiver;
    int taskCount;
    private List<ProxiDB> taskList = new ArrayList<>();
    Activity activity = (Activity) contextBootReceiver;

    @Override
    public void onReceive(final Context context, Intent intent) {
        contextBootReceiver = context;
        DatabaseHelper db = new DatabaseHelper(contextBootReceiver);
        taskList.addAll(db.getAllTasks());
        taskCount = db.getTaskCount();
        Geofences geofences = new Geofences(activity, contextBootReceiver);

        geofences.initGeofencing(taskCount, taskList);
    }
}