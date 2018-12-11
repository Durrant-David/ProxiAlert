package edu.byui.team06.proxialert.view.tasks;

//notification imports (many could probably be removed
//since it was moved to its own class

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.Fence;
import edu.byui.team06.proxialert.database.model.ProxiDB;
import edu.byui.team06.proxialert.utils.GeofenceTransitionsIntentService;
import edu.byui.team06.proxialert.utils.MyDividerItemDecoration;
import edu.byui.team06.proxialert.utils.MyNotification;
import edu.byui.team06.proxialert.utils.Permissions;
import edu.byui.team06.proxialert.utils.RecyclerTouchListener;
import edu.byui.team06.proxialert.view.TaskAdapter;
import edu.byui.team06.proxialert.view.settings.SettingsActivity;

//database imports

/**@author David Durrant, Chase Busacker, Kristina Hayes
 * @version  1.0
 * @since 1.0
 * <p>
 * The main activity class handles the Main view and interactions with buttons in the
 * main screen when the program starts up. It also handles the perimeters
 * around each task to determine when to notify user. It shows all the
 * tasks both active and inactive. The user can add a task by clicking
 * the green floating action button in the corner. They can update a
 * task by clicking on the respective task. They can navigate to the task, delete
 * the task, or mark the task as complete or uncomplete by long clicking
 * on the respective task.
 * </p>
 */
public class MainActivity extends AppCompatActivity {

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
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private Fence fence;
    private static final long DURATION = Geofence.NEVER_EXPIRE;
    private static final int DWELL = 1;
    private FloatingActionButton fab;
    private static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseHelper db;
    GeofencingClient mGeofencingClient;
    /**
     * <p>
     * OnOptionsItemSelected gets called when a menu item is selected.
     * This menu appears when the dots in the corner of the screen are
     * selected. The id depends on which button is pressed.
     * </p>
     *
     * @return the original result from the AppCompatActivity::onOptionsItemSelected()
     * */
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

    /**
     * <p>
     * onCreate gets called to initialize all the variables as well as to reset the theme
     * The theme must be reset if the theme setting is changed within the app.
     * </p>
     * @param savedInstanceState the current state of the application used for recreation;
     */
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

        initDatabase();
        initAllViews();
        attachResponseToViews();
        initGeofencing();

    }
    /**
     * <p>
     * onStart
     * Gets called after OnCreate as well as any time the activity is returned to.
     * It checks the theme, and calls recreate if necessary to "refresh" the theme.
     * </p>
     */
    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean themeName = pref.getBoolean("themes", false);
        if(theme != themeName) {
            recreate();
        }

    }

    /**
     * <p>
     * onDestroy
     * When the app is destroyed we close the database since
     * it will not be required anymore.
     * </p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        db.close();
    }

    /**
     * <p>
     * initDatabase
     * In charge of initializing the database, adding all the tasks to the taskList,
     * getting the taskCount, and connecting the the taskList to the TaskAdapter.
     * </p>
     */
    private void initDatabase() {
        db = new DatabaseHelper(this);
        taskList.addAll(db.getAllTasks());
        taskCount = db.getTaskCount();
        mAdapter = new TaskAdapter(taskList);
    }

    /**
     * <p>
     * initAllViews
     * Fetches all the views using findViewById, sets up the functions to run when the tasks
     * are selected.
     * </p>
     */
    private void initAllViews() {
        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noTaskView = findViewById(R.id.empty_tasks_view);
        fab = findViewById(R.id.fab);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);
        toggleEmptyTasks();
    }

    /**
     * start Update TaskActivity creates a intent for the TaskActivity
     * and adds extras to it to fill in the data for the given Task.
     * @param position - the position of the task that is selected.
     */
    private void startUpdateTaskActivity(int position) {
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
        taskIntent.putExtra("DESCRIPTION", element.getDescription());
        taskIntent.putExtra("COMPLETE", element.getComplete());
        taskIntent.putExtra("AUDIO", element.getAudio());
        taskIntent.putExtra("CONTACT", element.getContact());
        startActivityForResult(taskIntent, TASK_ACTIVITY_CODE);
    }

    /**
     * attachResponseToViews attaches the correct responses to the floating Action Button
     * and the recycler view which includes all the tasks.
     */
    private void attachResponseToViews() {
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

        /**
         * On a click, we start the TaskActivity to update the task.
         * On a long click, open a dialogBox to give the user the option to choose.
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                startUpdateTaskActivity(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    /**
     * initGeofencing Removes all the Geofences then add them all back in to the Geofence list
     * Then, add them to the Geofence Client so that checking can be done in the background.
     */
    private void initGeofencing() {
        // Geofence
        mGeofencingClient = new GeofencingClient(this);

        // First remove all geofences, to get a fresh start
        clearGeofenceClient();

        Geofence geofence;
        mGeofenceList = new ArrayList<>();
        // if there are tasks in the DB than add them to geofence
        if (taskCount > 0) {
            for (int i = 0; i < taskCount; i++) {
                ProxiDB task = taskList.get(i);
                if (!Boolean.parseBoolean(task.getComplete())) {
                    fence = new Fence(taskList.get(i));
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
     * resetGeofences removes all the Geofences that were being used and puts them all
     * back in the the GeofenceClient.
     * </p>
     */
    private void resetGeofences() {

        // Remove all geofences, to get a fresh start
        clearGeofenceClient();
        mGeofenceList.clear();

        Geofence geofence;
        // if there are tasks in the DB than add them to geofence
        if (taskCount > 0) {
            for (int i = 0; i < taskCount; i++) {
                ProxiDB task = taskList.get(i);
                if(Boolean.parseBoolean(task.getComplete())) {
                    fence = new Fence(task);
                    fence.setDuration(DURATION);
                    fence.setDwell(DWELL);
                    geofence = buildGeofence(fence);
                    mGeofenceList.add(geofence);
                }
            }
            // add geofences to geofence client list
            addGeofences();
        }
    }


    /**
     * clearGeoFence Client removes all the geofences to give us a fresh start.
     */
    private void clearGeofenceClient() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
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
     * onCreateOptionsMenu
     * This method displays the drop down list when the dots
     * in the top left corner are clicked on.
     * @param menu is the parameter for which menu was clicked on.
     * @return true to allow the program to continue running other checks.
     * </p>
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    /**
    * StartSettings
    * This method starts the settings activity for the user to start changing settings.
    * It is called when the "Settings" menu list item is selected.
    * @param
    */
    public void startSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * <p>
     * deleteTasks
     * Deletes note from SQLite and removes the item from the ListView based on the position
     * item from the ListView by its position
     * @param position - the position of the task in the list
     * </p>
     */
    private void deleteTask(int position) {

        // deleting the note from db
        db.deleteTask(taskList.get(position));
        taskList.remove(position);
        mAdapter.notifyItemRemoved(position);
        toggleEmptyTasks();
        resetGeofences();

    }

    /**
 * <p>
 * showActionDialog
 * Opens dialog with the following options and responses:
 * Navigate to... - opens navigation to the selected task.
 * Edit - starts the TaskActivity
 * Delete - calls deleteTask function
 * @param position - the position of the task in the list
 * </p>
 */
    private void showActionsDialog(final int position) {


        ProxiDB element = taskList.get(position);
        boolean isComplete = Boolean.parseBoolean(element.getComplete());
        CharSequence colors[];
        if(isComplete)
        {
            colors = new CharSequence[]{"Navigate to...", "Delete", "Unmark As Complete"};
        }
        else
        {
            colors = new CharSequence[]{"Navigate to...", "Delete", "Mark As Complete"};
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
                ProxiDB element = taskList.get(position);
                if (which == 0) {
                    startDirectionsActivity(position);
                } else if (which == 1){
                    deleteTask(position);
                } else {
                    if(element.getComplete().equals("true")) {
                        element.setComplete("false");
                        clearGeofenceClient();
                    } else {
                        element.setComplete("true");

                    }

                    db.updateTask(element);
                    taskList.clear();
                    taskList.addAll(db.getAllTasks());
                    mAdapter.notifyDataSetChanged();
                    resetGeofences();
                }
                
            }
        });
        builder.show();
    }
/**<p>
 * onActivityResult handles any intents that are returned to main when a StartActivityForResult
 * Activity was called. This allows the MainActivity to retrieve data from other Activities.
 * @param requestCode - specifies which task is returning a Result
 * @param resultCode - specifies the result type (Could be OK or Canceled)
 * @param data - the Intent that was created to return to the MainActivity
 * </p>
 */

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
                    mAdapter.notifyItemChanged(data.getIntExtra("POSITION", 0));
                } else {
                    taskList.add(taskList.size(), element);
                    mAdapter.notifyDataSetChanged();
                }

                taskList.clear();
                taskList.addAll(db.getAllTasks());
                mAdapter.notifyDataSetChanged();
                resetGeofences();

                //Update the view.
                mAdapter.notifyDataSetChanged();
                toggleEmptyTasks();
            }
        }
    }

/**
 * <p>
 *   The toggleEmptytasks checks how many tasks are in the list of tasks. If there are zero
 *   It sets the noTaskView to visible which says "No Tasks Found!" If there are tasks in
 *   the ListView, the noTaskView is set to Visible.
 * </p>
 */
    public void toggleEmptyTasks() {
        // you can check notesList.size() > 0

        if (db.getTaskCount() > 0) {
            noTaskView.setVisibility(View.GONE);
        } else {
            noTaskView.setVisibility(View.VISIBLE);
        }
    }

    /** <p>
     * getGeofencingRequest creates a GeofenceBuilder which triggers when the user enters the
     * Geofence or is inside the Geofence. Then, adds a list of Geofences to the Builder.
     * @return GeofenceingRequest
     * </p>
     */
    private GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(mGeofenceList)
                .build();
    }

    /**<p>
     * getGeofencePendingIntent creates a pending intent for the Notification to receive when
     * triggered.
     * @return PendingIntent
     * </p>
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    /**
     * <p>
     * buildGeofence builds perimeter around a given location given a Fence Object.
     * </p>
     * @param f -holds the data used to build the geofence
     * @return Geofence
     */
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

    /**
     * <p>
     * addGeofences Checks if the app has permission to use the users current location,
     * then, removes and re-adds all the Geofences to the GeofencingClient.
     * </p>
     */
    private void addGeofences() {
        // Location permissions
        Permissions permissions = new Permissions();
        if ( permissions.checkMapsPermission(this)){
            mGeofencingClient = new GeofencingClient(this);
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
            permissions.askMapsPermission(this);
        }
    }

    /**<p>
     * startDirectionsActivity takes a task position in the taskList and gives
     * executes an Activity that will take the user to the task Location.
     * @param position - the position of the task in the list.
     * </p>
     */
    void startDirectionsActivity(final int position) {

        ProxiDB task = taskList.get(position);
        Uri navUri = Uri.parse("google.navigation:q="+task.getLat()+","+task.getLong());
        Intent navigationIntent =new Intent(Intent.ACTION_VIEW, navUri);
        navigationIntent.setPackage("com.google.android.apps.maps");
        startActivity(navigationIntent);


    }

     /* THIS CODE IS NO LONGER USED. Instead, we reset the Geofence list everytime to ensure
     * that all the geofences are in the correct positions due to some of the Geofences
     * being disabled and re-enabled when "Set to UnComplete" gets clicked.
     *
     *
     * removeGeofence removes a single Geofence from the Geofence Client
     * @param position = the position of the task that is getting removed.
     *
    private void removeGeofence(int position) {
        List<String> geofenceRemoveList = new ArrayList<>();

        //TODO For David are you sure we want to remove the geofence based on the
        //position in the list? Shouldn't this be the ID of the element?
        geofenceRemoveList.add(String.valueOf(position));
        mGeofencingClient.removeGeofences(geofenceRemoveList)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "successfully removed Geofence");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "failed to remove Geofence - " + e);
                    }
                });
    }
    */

}
