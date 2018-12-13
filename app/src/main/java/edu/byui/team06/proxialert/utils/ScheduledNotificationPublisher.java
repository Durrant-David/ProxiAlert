package edu.byui.team06.proxialert.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;

import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.ProxiDB;


public class ScheduledNotificationPublisher extends BroadcastReceiver {

    public final static String NOTIFICATION = "notification";
    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("TaskID", 0);
        DatabaseHelper db = new DatabaseHelper(context);
        ProxiDB task = db.getProxiDB(taskId);
        MyNotification notification = new MyNotification(task, context, true);
        notification.send();

    }
}
