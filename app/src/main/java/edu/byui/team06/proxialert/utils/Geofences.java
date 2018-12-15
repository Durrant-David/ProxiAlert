package edu.byui.team06.proxialert.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import edu.byui.team06.proxialert.database.model.Fence;
import edu.byui.team06.proxialert.database.model.ProxiDB;
import edu.byui.team06.proxialert.view.tasks.MainActivity;

public class Geofences {
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private Fence fence;
    private static final long DURATION = Geofence.NEVER_EXPIRE;
    private static final int DWELL = 1;
    private static final String TAG = "Geofences";
    private GeofencingClient mGeofencingClient;
    private Activity activity;
    private Context context;

    public Geofences(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    /**
     * initGeofencing Removes all the Geofences then add them all back in to the Geofence list
     * Then, add them to the Geofence Client so that checking can be done in the background.
     */
    public void initGeofencing(int count, List<ProxiDB> list) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        mGeofencingClient = new GeofencingClient(context);
        long minutes = Long.parseLong(pref.getString("interval", "5"));
        LocationRequest lr = new LocationRequest();
        lr.setInterval(1000 * 60 * minutes);
        lr.setFastestInterval(1000 * 60 * minutes + 1000);
        // First remove all geofences, to get a fresh start
        clearGeofenceClient();

        com.google.android.gms.location.Geofence geofence;
        mGeofenceList = new ArrayList<>();
        // if there are tasks in the DB than add them to geofence
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                ProxiDB task = list.get(i);
                if (!Boolean.parseBoolean(task.getComplete())) {
                    fence = new Fence(list.get(i));
                    fence.setDuration(DURATION);
                    fence.setDwell(DWELL);
                    geofence = buildGeofence(fence);
                    mGeofenceList.add(geofence);
                }
            }
            // add geofences to geofence client list
            if (!mGeofenceList.isEmpty()) {
                addGeofences();
            }
        }
    }

    /**
     * <p>
     * getGeofencingRequest creates a GeofenceBuilder which triggers when the user enters the
     * Geofence or is inside the Geofence. Then, adds a list of Geofences to the Builder.
     *
     * @return GeofenceingRequest
     * </p>
     */
    private GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(mGeofenceList)
                .build();
    }

    /**
     * <p>
     * getGeofencePendingIntent creates a pending intent for the Notification to receive when
     * triggered.
     *
     * @return PendingIntent
     * </p>
     */
    private PendingIntent getGeofencePendingIntent() {
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

    /**
     * <p>
     * buildGeofence builds perimeter around a given location given a Fence Object.
     * </p>
     *
     * @param f -holds the data used to build the geofence
     * @return Geofence
     */
    private com.google.android.gms.location.Geofence buildGeofence(Fence f) {
        return new com.google.android.gms.location.Geofence.Builder()
                .setRequestId(f.getStringId())
                .setCircularRegion(f.getLat(), f.getLng(), f.getRadius())
                .setExpirationDuration(f.getDuration())
                .setTransitionTypes(com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER |
                        com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(f.getDwell())
                .build();
    }

    /**
     * <p>
     * addGeofences Checks if the app has permission to use the users current location,
     * then, removes and re-adds all the Geofences to the GeofencingClient.
     * </p>
     */
    private void addGeofences() {
        // Location permissions
        Permissions permissions = new Permissions(context);
        if (permissions.checkMapsPermission(context)) {
            mGeofencingClient = new GeofencingClient(context);
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "successfully added Geofences");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "failed to add Geofences - " + e);
                        }
                    });
        } else {
            permissions.askMapsPermission(activity);
        }
    }

    /**
     * clearGeoFence Client removes all the geofences to give us a fresh start.
     */
    public void clearGeofenceClient() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(activity, new OnSuccessListener<Void>() {
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

    /**
     * <p>
     * resetGeofences removes all the Geofences that were being used and puts them all
     * back in the the GeofenceClient.
     * </p>
     */
    public void resetGeofences(int count, List<ProxiDB>list) {

        // Remove all geofences, to get a fresh start
        clearGeofenceClient();
        mGeofenceList.clear();

        com.google.android.gms.location.Geofence geofence;
        // if there are tasks in the DB than add them to geofence
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                ProxiDB task = list.get(i);
                if(!Boolean.parseBoolean(task.getComplete())) {
                    fence = new Fence(task);
                    fence.setDuration(DURATION);
                    fence.setDwell(DWELL);
                    geofence = buildGeofence(fence);
                    mGeofenceList.add(geofence);
                }
            }
            // add geofences to geofence client list
            if(!mGeofenceList.isEmpty()) {
                addGeofences();
            }
        }
    }
}
