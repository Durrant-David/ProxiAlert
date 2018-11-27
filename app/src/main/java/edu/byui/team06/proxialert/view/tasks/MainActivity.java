package edu.byui.team06.proxialert.view.tasks;

//notification imports (many could probably be removed
//since it was moved to its own class

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.ProxiDB;
import edu.byui.team06.proxialert.utils.GeofenceTrasitionService;
import edu.byui.team06.proxialert.utils.MyDividerItemDecoration;
import edu.byui.team06.proxialert.utils.Permissions;
import edu.byui.team06.proxialert.utils.RecyclerTouchListener;
import edu.byui.team06.proxialert.view.TaskAdapter;
import edu.byui.team06.proxialert.view.settings.SettingsActivity;

//database imports


public class MainActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status>
{
    private TaskAdapter mAdapter;
    private List<ProxiDB> taskList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noTaskView;
    public static final String UPDATE = "UPDATE";
    public static final String POSITION = "POSITION";
    int taskCount;
    private static final int TASK_ACTIVITY_CODE = 0;
    private int SETTINGS_ACTION = 1;
    private boolean theme;
    private Permissions permissions;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseHelper db;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                startActivityForResult(new Intent(this,
                        SettingsActivity.class), SETTINGS_ACTION);
        }
        return super.onOptionsItemSelected(item);
    }


    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean themeName = pref.getBoolean("themes", false);
        if (themeName) {
            theme = themeName;
            setTheme(R.style.ThemeOverlay_MaterialComponents_Dark);
        } else {
            theme = themeName;
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Geofence
        // Location permissions
        permissions = new Permissions();
        if ( permissions.checkMapsPermission(this)){

        } else {
            permissions.askMapsPermission(this);
        }

        // create GoogleApiClient
        createGoogleApi();
        startGeofence();

        //This is temporary. We can send a lot more notifications later, but for now
        //it just sends immediately.
        /*
        Notification n = new Notification("Test", "This is working",
                "I'm working and this is longer " +
                        "text that can be read if the notification is expanded.",
                this.getApplicationContext());
        n.send();
        */
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noTaskView = findViewById(R.id.empty_tasks_view);

        db = new DatabaseHelper(this);

        taskList.addAll(db.getAllTasks());
        taskCount = db.getTaskCount();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent taskIntent = new Intent(MainActivity.this, TaskActivity.class);
                taskIntent.putExtra(UPDATE, false);
                taskIntent.putExtra(POSITION, -1);
                startActivityForResult(taskIntent, TASK_ACTIVITY_CODE);
                //showTaskDialog(false, null, -1);
            }
        });

        mAdapter = new TaskAdapter(taskList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyTasks();

        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //Maybe we can start navigation to the location.
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    //Geofence
    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
            googleApiClient.connect();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean themeName = pref.getBoolean("themes", false);
        if(theme != themeName) {
            recreate();
        }

        // Geofence
        // Call GoogleApiClient connection when starting the Activity
        if(googleApiClient != null){
            googleApiClient.connect();
        } else {
            createGoogleApi();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Geofence
        // Disconnect GoogleApiClient when stopping Activity
        if(googleApiClient != null){
            googleApiClient.disconnect();
        }
    }

    /****************************************************
     * onCreateOptionsMenu
     * displays the drop down list when dots in the corner
     * are clicked on.
     *****************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /****************************************************
     * startSettings
     * start the settings Activity
     *****************************************************/
    public void startSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    // TODO clear geofence linked to task
    /****************************************************
     * deleteTasks
     * Deletes note from SQLite and removing the
     * item from the ListView by its position
     *****************************************************/
    private void deleteTask(int position) {
        // deleting the note from db
        db.deleteTask(taskList.get(position));

        // removing the note from the list
        taskList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyTasks();
    }

    /***************************************************
     * showActionsDialog
     * Opens dialog with Edit - Delete options
     * Edit - Starts the TaskActivity
     * Delete - Calls deleteTask
     ****************************************************/
    private void showActionsDialog(final int position) {

        CharSequence colors[] = new CharSequence[]{"Edit", "Delete", "Mark As Complete"};
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
                if (which == 0) {
                    ProxiDB element = taskList.get(position);
                    Intent taskIntent = new Intent(MainActivity.this, TaskActivity.class);
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
                    startActivityForResult(taskIntent, TASK_ACTIVITY_CODE);
                } else {
                    deleteTask(position);
                }
            }
        });
        builder.show();
    }

    /*******************************************************
     * onActivityResult
     * Handles any intents that are returned to main via
     * StartActivityForResult
     *******************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TASK_ACTIVITY_CODE represents results with the Task Activity.
        if (requestCode == TASK_ACTIVITY_CODE) {

            //If the result was set to Ok, then we will update the Views.
            if (resultCode == RESULT_OK) {

                //get whether or not is(an)Update and the id of the task concerned.
                //then select the correct element.
                boolean isUpdate = data.getBooleanExtra("UPDATE", false);
                long id = data.getLongExtra("id", 0);
                ProxiDB element = db.getProxiDB(id);

                //IF it's an update, change the element
                //If it's NOT an update, add it to the end.
                if (isUpdate) {
                    taskList.set(data.getIntExtra("POSITION", 0), element);
                } else {
                    taskList.add(taskList.size(), element);
                }

                //Update the view.
                mAdapter.notifyDataSetChanged();
                toggleEmptyTasks();
            }
        }
    }


    /*******************************************************
     * toggleEmptyTasks
     * toggleEmptyTasks will toggle the Empty task
     * visibility on or off depending on the amount
     * of tasks saved in the database.
     *******************************************************/
    public void toggleEmptyTasks() {
        // you can check notesList.size() > 0

        if (db.getTaskCount() > 0) {
            noTaskView.setVisibility(View.GONE);
        } else {
            noTaskView.setVisibility(View.VISIBLE);
        }
    }

    //Geofence



    private LocationRequest locationRequest;
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    // TODO move to settings
    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;

    // Start location Updates
    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if ( permissions.checkMapsPermission(this) )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( permissions.checkMapsPermission(this) ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else permissions.askMapsPermission(this);
    }


    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        Geofence geofence = createGeofence(
                "0",
                40.77340882,
                -111.88904945,
                5000,
                1000);
        GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
        addGeofence( geofenceRequest );
    }

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    // Create a Geofence
    private Geofence createGeofence( String id, double lat, double lng, float radius, long duration) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion( lat, lng, radius)
                .setExpirationDuration( duration )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (permissions.checkMapsPermission(this))
            if(googleApiClient != null){
                googleApiClient.connect();
            } else {
                createGoogleApi();
            }
            LocationServices.getGeofencingClient(this).addGeofences(
                    request,
                    createGeofencePendingIntent()
            ).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i(TAG, "add geofence success");
                }
            }
            ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "add geofence failure");
                }
            });
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
        } else {
            // inform about fail
        }
    }

}
