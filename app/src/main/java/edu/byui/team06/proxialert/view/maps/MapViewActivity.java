
package edu.byui.team06.proxialert.view.maps;

        import android.Manifest;
        import android.app.AlarmManager;
        import android.app.PendingIntent;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.content.pm.PackageManager;
        import android.location.Address;
        import android.location.Criteria;
        import android.location.Geocoder;
        import android.location.Location;
        import android.location.LocationManager;
        import android.net.Uri;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.app.FragmentActivity;
        import android.support.v7.app.AlertDialog;
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
        import com.google.android.gms.maps.model.CameraPosition;
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
        import edu.byui.team06.proxialert.utils.Geofences;
        import edu.byui.team06.proxialert.utils.PendingIntentHelper;
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
        OnMapReadyCallback
{
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String UPDATE = "UPDATE";
    private static final String POSITION = "POSITION";
    private boolean theme;
    private GoogleMap mMap;
    private ArrayList<Marker> searchMarkers = new ArrayList<>();
    private Permissions permissions;
    private String taskName;
    private DatabaseHelper db;
    private ArrayList <ProxiDB> TaskList = new ArrayList<>();
    private static final int TASK_ACTIVITY_CODE = 1;
    private Geofences geofences;

    // TODO zoom in on current location onStart
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        permissions = new Permissions(getApplicationContext());
        if ( permissions.checkMapsPermission(this) ) {

        } else {
            permissions.askMapsPermission(this);
        }
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        theme = pref.getBoolean("themes", false);
        if (theme) {
            setTheme(R.style.ThemeOverlay_MaterialComponents_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_maps);

        db = new DatabaseHelper(this);
        TaskList.addAll(db.getAllTasks());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        mMap.setOnMapClickListener(this);

        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);


        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));
        LatLng myLoc;
        CameraUpdate camera;
        if (location != null) {
            myLoc = new LatLng(location.getLatitude(), location.getLongitude());
            camera = CameraUpdateFactory.newLatLngZoom(myLoc, 1);
            mMap.animateCamera(camera);
        }


        for (ProxiDB task : TaskList) {
            setSearchMarker(task);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showActionsDialog(searchMarkers.indexOf(marker));
                return false;
            }
        });
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
                .visible(!Boolean.parseBoolean(task.getComplete()));
        if ( mMap!=null ) {
            searchMarkers.add(mMap.addMarker(markerOptions));

        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        String address = getMarkerAddress(latLng);
        Intent taskIntent = new Intent(MapViewActivity.this, TaskActivity.class);
        taskIntent.putExtra("ADDRESS", address);
        startActivityForResult(taskIntent, TASK_ACTIVITY_CODE);
    }



    private void startUpdateTaskActivity(int position) {
        ProxiDB element = TaskList.get(position);
        Intent taskIntent = new Intent(MapViewActivity.this, TaskActivity.class);
        taskIntent.putExtra(UPDATE, true);
        taskIntent.putExtra(POSITION, position);
        taskIntent.putExtra("ADDRESS", element.getAddress());
        taskIntent.putExtra("RADIUS", element.getRadius());
        taskIntent.putExtra("UNITS", element.getUnits());
        taskIntent.putExtra("ID", element.getId());
        taskIntent.putExtra("TASK", element.getTask());
        taskIntent.putExtra("DUE", element.getDueDate());
        taskIntent.putExtra("TIMESTAMP", element.getTimeStamp());
        taskIntent.putExtra("LAT", element.getLat());
        taskIntent.putExtra("LONG", element.getLong());
        taskIntent.putExtra("DESCRIPTION", element.getDescription());
        taskIntent.putExtra("COMPLETE", element.getComplete());
        taskIntent.putExtra("AUDIO", element.getAudio());
        taskIntent.putExtra("CONTACT", element.getContact());
        startActivityForResult(taskIntent, TASK_ACTIVITY_CODE);
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

        if (!addresses.isEmpty()) {
            String address = addresses.get(0).getAddressLine(0);
            Log.d(TAG, "Marker address = (" + address + ")");
            return address;
        }
        return "";
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
               geofences.resetGeofences(TaskList.size(), TaskList);

            }
        }
    }


    private void showActionsDialog(final int position) {


        ProxiDB element = TaskList.get(position);
        boolean isComplete = Boolean.parseBoolean(element.getComplete());
        CharSequence colors[];
        if(isComplete)
        {
            colors = new CharSequence[]{"Edit Task...", "Navigate to...", "Delete", "Mark As Incomplete"};
        }
        else
        {
            colors = new CharSequence[]{"Edit Task...", "Navigate to...", "Delete", "Mark As Complete"};
        }


        AlertDialog.Builder builder;
        if(theme) {
            builder = new AlertDialog.Builder(this, R.style.Dark_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ProxiDB element = TaskList.get(position);
                if (which == 0) {
                    startUpdateTaskActivity(position);
                } else if (which == 1) {
                    startDirectionsActivity(position);
                } else if (which == 2){
                    deleteTask(position);
                } else {
                    if(element.getComplete().equals("true")) {
                        element.setComplete("false");
                        geofences.clearGeofenceClient();
                        scheduleNotification(element);
                    } else {
                        element.setComplete("true");
                        removeScheduledNotification(element);
                    }

                    db.updateTask(element);
                   geofences.resetGeofences(TaskList.size(), TaskList);
                }

            }
        });
        builder.show();
    }

    /**<p>
     * startDirectionsActivity takes a task position in the taskList and gives
     * executes an Activity that will take the user to the task Location.
     * @param position - the position of the task in the list.
     * </p>
     */
    void startDirectionsActivity(final int position) {

        ProxiDB task = TaskList.get(position);
        Uri navUri = Uri.parse("google.navigation:q="+task.getLat()+","+task.getLong());
        Intent navigationIntent =new Intent(Intent.ACTION_VIEW, navUri);
        navigationIntent.setPackage("com.google.android.apps.maps");
        startActivity(navigationIntent);

    }

    private void deleteTask(int position) {

        // deleting the note from db
        db.deleteTask(TaskList.get(position));
        removeScheduledNotification(TaskList.get(position));
        TaskList.remove(position);
        geofences.resetGeofences(TaskList.size(), TaskList);

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


