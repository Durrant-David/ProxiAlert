
package edu.byui.team06.proxialert.view.maps;

        import android.Manifest;
        import android.app.AlarmManager;
        import android.app.PendingIntent;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;
        import android.location.Address;
        import android.location.Criteria;
        import android.location.Geocoder;
        import android.location.Location;
        import android.location.LocationManager;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.app.FragmentActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.Toast;

        import com.google.android.gms.location.Geofence;
        import com.google.android.gms.maps.CameraUpdate;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;
        import com.google.android.gms.maps.model.MarkerOptions;

        import java.io.IOException;
        import java.lang.reflect.Array;
        import java.text.ParseException;
        import java.text.SimpleDateFormat;
        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.Date;
        import java.util.List;
        import java.util.Locale;

        import edu.byui.team06.proxialert.R;
        import edu.byui.team06.proxialert.database.model.Fence;
        import edu.byui.team06.proxialert.database.model.ProxiDB;
        import edu.byui.team06.proxialert.utils.Permissions;
        import edu.byui.team06.proxialert.database.DatabaseHelper;
        import edu.byui.team06.proxialert.utils.ScheduledNotificationPublisher;
        import edu.byui.team06.proxialert.view.tasks.MainActivity;
        import edu.byui.team06.proxialert.view.tasks.TaskActivity;


/**
 * MapsActivity it handles the activity
 * used for selecting location of the task
 */
public class MapViewActivity extends FragmentActivity
        implements
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener
{
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private ArrayList<Marker> searchMarkers = new ArrayList<>();
    private Permissions permissions;
    private String taskName;
    private DatabaseHelper db;
    private ArrayList <ProxiDB> TaskList = new ArrayList<>();
    private static final int TASK_ACTIVITY_CODE = 1;

    // TODO zoom in on current location onStart
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        permissions = new Permissions();
        if ( permissions.checkMapsPermission(this) ) {

        } else {
            permissions.askMapsPermission(this);
        }
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean themeName = pref.getBoolean("themes", false);
        if (themeName) {
            setTheme(R.style.ThemeOverlay_MaterialComponents_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        db = new DatabaseHelper(this);
        TaskList.addAll(db.getAllTasks());

       }

    /**
     * onMapReady asks user for permission
     * to access for Google maps.
     * It marks user's current location
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));
        LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(myLoc, 1);
        mMap.animateCamera(camera);

        for (ProxiDB task : TaskList) {
            setSearchMarker(task);
        }

    }





    /**
     * MapClick sets a marker to where
     * the user clicked. Sets the text
     * of the location search bar
     * to the nearest address.
     * @param task
     */

    private void setSearchMarker(ProxiDB task) {

        LatLng latLng = new LatLng(Double.parseDouble(task.getLat()), Double.parseDouble(task.getLong()));
        String taskName = task.getTask();
        Log.i(TAG, "setSearchMarker("+latLng+")");
        String title;
        if(taskName.length() > 0) {
            title = taskName;
        } else {
            title = latLng.latitude + ", " + latLng.longitude;
        }

        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .draggable(true)
                .visible(!Boolean.parseBoolean(task.getComplete()));
        if ( mMap!=null ) {
            searchMarkers.add(mMap.addMarker(markerOptions));

        }
    }



    /**
     * onMapCancel
     * It sets the result to cancelled and closes
     * the activity.
     * @param view
     */
    public void onMapCancel(View view) {
        finish();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        String address = getMarkerAddress(latLng);
        Intent taskIntent = new Intent(MapViewActivity.this, TaskActivity.class);
        taskIntent.putExtra("ADDRESS", address);
        startActivityForResult(taskIntent, TASK_ACTIVITY_CODE);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        //TODO solve problem with marker moving!
        int index = searchMarkers.indexOf(marker);
        ProxiDB task = TaskList.get(index);
        //this represents a long click.
        //TODO start the update task activity.
    }
    @Override
    public void onMarkerDrag(Marker marker) {
        return;
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        return;
    }
    public String getMarkerAddress(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        String address = addresses.get(0).getAddressLine(0);

        Log.d(TAG, "Marker address = ("+address +")");
        return address;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TASK_ACTIVITY_CODE represents results with the Task Activity.
        if (resultCode == RESULT_OK) {

            //If the result was set to Ok, then we will update the Views.
            if (requestCode == TASK_ACTIVITY_CODE) {

                //get whether or not is(an)Update and the id of the task concerned.
                //then select the correct element.
                boolean isUpdate = data.getBooleanExtra("UPDATE", false);
                long id = data.getLongExtra("id", 0);
                ProxiDB element = db.getProxiDB(id);
                //IF it's an update, change the element
                //If it's NOT an update, add it to the end.
                if (isUpdate) {
                    TaskList.set(data.getIntExtra("POSITION", 0), element);
                    removeScheduledNotification(element);
                    scheduleNotification(element);
                } else {
                    TaskList.add(TaskList.size(), element);
                    setSearchMarker(element);
                    scheduleNotification(element);
                }
                //TODO reset Geofences here.
                //somehow call remove Geofences class when it is built!
               // resetGeofences();

            }
        }
    }

    public void scheduleNotification(ProxiDB task) {

        if(Boolean.parseBoolean(task.getComplete()))
            return;

        //create the pending intent
        Intent notificationIntent = new Intent(getApplicationContext(), ScheduledNotificationPublisher.class);
        notificationIntent.putExtra("TaskID", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), task.getId(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        //set the time for the notification
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
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
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }

    public void removeScheduledNotification(ProxiDB task) {
        Intent notificationIntent = new Intent(getApplicationContext(), ScheduledNotificationPublisher.class);
        notificationIntent.putExtra("TaskID", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), task.getId(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }


}

//TODO add a function for on Result?
