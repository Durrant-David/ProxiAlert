package edu.byui.team06.proxialert.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.ProxiDB;


public class ScheduledNotificationPublisher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int taskId = intent.getIntExtra("TaskID", 0);
        DatabaseHelper db = new DatabaseHelper(context);
        ProxiDB task = db.getProxiDB(taskId);
        MyNotification notification = new MyNotification(task, context, true);
        notification.send();

    }
}
