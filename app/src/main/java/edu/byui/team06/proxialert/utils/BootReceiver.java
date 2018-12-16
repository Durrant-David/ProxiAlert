package edu.byui.team06.proxialert.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.ProxiDB;

public class BootReceiver extends BroadcastReceiver {

    private Context contextBootReceiver;
    private List<ProxiDB> taskList = new ArrayList<>();
    private Activity activity = (Activity) contextBootReceiver;
    
    @Override
    public void onReceive(final Context context, Intent intent) {
        contextBootReceiver = context;
        DatabaseHelper db = new DatabaseHelper(contextBootReceiver);
        taskList.addAll(db.getAllTasks());
        int taskCount = db.getTaskCount();
        Geofences geofences = new Geofences(activity, contextBootReceiver);

        geofences.initGeofencing(taskList);

        for(ProxiDB task : taskList) {
            if(!Boolean.parseBoolean(task.getComplete())) {
                scheduleNotification(task);
            }
        }
    }

    private void scheduleNotification(ProxiDB task) {

        if(Boolean.parseBoolean(task.getComplete()))
            return;

        //create the pending intent
        Intent notificationIntent = new Intent(contextBootReceiver, ScheduledNotificationPublisher.class);
        notificationIntent.putExtra("TaskID", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(contextBootReceiver, task.getId(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //set the time for the notification
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(contextBootReceiver);
        String time = pref.getString("notifyTime", "10:00 AM");
        String myDateFormat = "MM/dd/yyyy HH:mm a";
        SimpleDateFormat sdfDate = new SimpleDateFormat(myDateFormat);
        Date date;
        try {
            date = sdfDate.parse(task.getDueDate() + time);
        } catch (ParseException e) {
            try {
                date = sdfDate.parse(task.getDueDate() + " 10:00 AM");
            } catch (ParseException e1) {
                e1.printStackTrace();
                return;
            }
        }

        //setup the time to send.
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        AlarmManager alarmManager = (AlarmManager) contextBootReceiver.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }
}