package edu.byui.team06.proxialert.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.Fence;
import edu.byui.team06.proxialert.database.model.ProxiDB;

public class PendingIntentHelper extends BroadcastReceiver {

    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private static final long DURATION = Geofence.NEVER_EXPIRE;
    private static final int DWELL = 1;
    private static final String TAG = "BootReceiver";
    Context context;
    private boolean isBoot;
    int taskCount;
    private Activity activity;
    private List<ProxiDB> taskList = new ArrayList<>();
    GeofencingClient mGeofencingClient;

    public PendingIntentHelper(Context c, Activity activity) {
        this.context = c;
        isBoot = false;
        this.activity = activity;
    }
    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;
        DatabaseHelper db;
        Fence fence;

        isBoot = false;
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            if (context != null) {
                db = new DatabaseHelper(context);
                isBoot = true;
                taskList.addAll(db.getAllTasks());
                taskCount = db.getTaskCount();

                Geofence geofence;
                mGeofenceList = new ArrayList<>();
                // if there are tasks in the DB than add them to geofence
                if (taskCount > 0) {
                    ProxiDB element;
                    for (int i = 0; i < taskCount; i++) {
                        element = taskList.get(i);
                        fence = new Fence(element);
                        scheduleNotification(element);
                        fence.setDuration(DURATION);
                        fence.setDwell(DWELL);
                        geofence = buildGeofence(fence);
                        mGeofenceList.add(geofence);
                    }
                    // add geofences to geofence client list
                    addGeofences();

                }

                db.close();
            }
        }
    }

    public GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(mGeofenceList)
                .build();
    }

    public PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    public Geofence buildGeofence(Fence f) {
        return new Geofence.Builder()
                .setRequestId(f.getStringId())
                .setCircularRegion(f.getLat(), f.getLng(), f.getRadius())
                .setExpirationDuration(f.getDuration())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(f.getDwell())
                .build();
    }

    public void addGeofences() {


        // Location permissions
        Permissions permissions = new Permissions(context);
        if ( permissions.checkMapsPermission(context)){
            mGeofencingClient = new GeofencingClient(context);
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i (TAG, "successfully added Geofences");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "failed to add Geofences - " + e);
                        }
                    });
        } else if(!isBoot) {
           permissions.askMapsPermission(activity);
        }
    }

    public void scheduleNotification(ProxiDB task) {

        if(Boolean.parseBoolean(task.getComplete()))
            return;

        //create the pending intent
        Intent notificationIntent = new Intent(context.getApplicationContext(), ScheduledNotificationPublisher.class);
        notificationIntent.putExtra("TaskID", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), task.getId(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //set the time for the notification
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
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
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }

    public void removeScheduledNotification(ProxiDB task) {
        Intent notificationIntent = new Intent(context.getApplicationContext(), ScheduledNotificationPublisher.class);
        notificationIntent.putExtra("TaskID", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), task.getId(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }

    public void clearGeofenceClient() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener((Executor) this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "successfully removed all Geofences");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "failed to remove all Geofences - " + e);
                    }
                });
    }
}