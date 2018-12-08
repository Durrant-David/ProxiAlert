package edu.byui.team06.proxialert.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.Fence;
import edu.byui.team06.proxialert.database.model.ProxiDB;

public class BootReceiver extends BroadcastReceiver {

    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private static final long DURATION = Geofence.NEVER_EXPIRE;
    private static final int DWELL = 1;
    private static final String TAG = "BootReceiver";
    Context contextBootReceiver;
    int taskCount;
    private List<ProxiDB> taskList = new ArrayList<>();

    @Override
    public void onReceive(final Context context, Intent intent) {
        contextBootReceiver = context;
        DatabaseHelper db;
        Fence fence;

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            if (context != null) {
                db = new DatabaseHelper(contextBootReceiver);

                taskList.addAll(db.getAllTasks());
                taskCount = db.getTaskCount();

                Geofence geofence;
                mGeofenceList = new ArrayList<>();
                // if there are tasks in the DB than add them to geofence
                if (taskCount > 0) {
                    for (int i = 0; i < taskCount; i++) {
                        fence = new Fence(taskList.get(i));
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

    private GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(mGeofenceList)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(contextBootReceiver, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(contextBootReceiver, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private Geofence buildGeofence(Fence f) {
        return new Geofence.Builder()
                .setRequestId(f.getStringId())
                .setCircularRegion(f.getLat(), f.getLng(), f.getRadius())
                .setExpirationDuration(f.getDuration())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(f.getDwell())
                .build();
    }

    private void addGeofences() {
        GeofencingClient mGeofencingClient;

        // Location permissions
        Permissions permissions = new Permissions();
        if ( permissions.checkMapsPermission(contextBootReceiver)){
            mGeofencingClient = new GeofencingClient(contextBootReceiver);
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
        } else {
           // permissions.askMapsPermission(contextBootReceiver);
        }
    }
}