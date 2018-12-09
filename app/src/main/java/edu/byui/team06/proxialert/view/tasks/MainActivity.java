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

/**@author
 * @version  1.0
 * @since
 * <p>
 * The main activity handles views and interactions with buttons
 * in the main screen when the program starts up.
 * It also handles the perimeters around each task to determine when to notify user.
 * It also adds the task to the database (when the task is updated/created)
 * </p>
 *
 * @param
 * @return
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

    private static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseHelper db;
    /** @author
     * @version  1.0
     * @since
     *
     * This class is detecting what menu button is selected.
     *
     * @param
     * @return
     *
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

        // Geofence
        GeofencingClient mGeofencingClient;
        mGeofencingClient = new GeofencingClient(this);

        // First remove all geofences, to get a fresh start
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



        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
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
                startActivityForResult(taskIntent, TASK_ACTIVITY_CODE);

            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }
    /**@author
     * @version  1.0
     * @since
     * onStart
     * This method checks and loads the Theme sharedPreference onStart
     * @param
     * @return
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

    /** onStop
     * When app is stopped close the database
     */
    @Override
    public void onStop() {
        super.onStop();
        db.close();
    }

    /**@author
     * @version  1.0
     * @since
     * onCreateOptionsMenu
     * This method displays the drop down list when dots
     * in the corner are clicked on.
     * @param
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
/**@author
 * @version  1.0
 * @since
 * StartSettings
 * This method starts the settings activity
 * @param
 * @return
 */

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

        // remove Geofence connected to task
        List<String> geofenceList = new ArrayList<>();
        GeofencingClient mGeofencingClient;
        geofenceList.add(String.valueOf(position));

        mGeofencingClient = new GeofencingClient(this);
        mGeofencingClient.removeGeofences(geofenceList)
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

        // deleting the note from db
        db.deleteTask(taskList.get(position));

        // removing the note from the list
        taskList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyTasks();

    }
/**@author
 * @version  1.0
 * @since
 * showActionDialog
 * Opens dialog with Edit/Delete options
 * Edit option starts the TaskActivity
 * Delete option calls deleteTask
 * @param
 * @return
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
                        //TODO put geofence back since the task has now been re-added
                    } else {
                        element.setComplete("true");
                        //TODO remove geofence since the task is complete.

                    }
                    db.updateTask(element);
                    taskList.clear();
                    taskList.addAll(db.getAllTasks());
                    mAdapter.notifyDataSetChanged();
                }
                
            }
        });
        builder.show();
    }
/**@author
 * @version  1.0
 * @since
 * onActivityResult handles any intents
 * that are returned to main via StartActivityForResult
 * @param
 * @return
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
                MyNotification mn = new MyNotification(element, getApplicationContext());
                mn.send();
                //IF it's an update, change the element
                //If it's NOT an update, add it to the end.
                if (isUpdate) {
                    taskList.set(data.getIntExtra("POSITION", 0), element);
                    mAdapter.notifyItemChanged(data.getIntExtra("POSITION", 0));
                } else {
                    taskList.add(taskList.size(), element);
                    mAdapter.notifyDataSetChanged();
                }

                //TODO Update Old geofence if update occurred.
                // Geofence
                Geofence geofence;
                fence = new Fence(element);
                fence.setDuration(DURATION);
                fence.setDwell(DWELL);
                geofence = buildGeofence(fence);
                mGeofenceList.add(geofence);

                addGeofences();

                //Update the view.
                mAdapter.notifyDataSetChanged();
                toggleEmptyTasks();
            }
        }
    }

/**@author
 * @version  1.0
 * @since
 * <p>
 *   The toggleEmptytasks performs the following:
 *   toggleEmptyTasks
 *   toggleEmptyTasks will toggle the Empty task
 *   visibility on or off depending on the amount
 *   of tasks saved in the database.
 * </p>
 *
 * @param
 * @return
 */

    public void toggleEmptyTasks() {
        // you can check notesList.size() > 0

        if (db.getTaskCount() > 0) {
            noTaskView.setVisibility(View.GONE);
        } else {
            noTaskView.setVisibility(View.VISIBLE);
        }
    }

    // Geofence

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
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
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

    void startDirectionsActivity(final int position) {

        ProxiDB task = taskList.get(position);
        Uri navUri = Uri.parse("google.navigation:q="+task.getLat()+","+task.getLong());
        Intent navigationIntent =new Intent(Intent.ACTION_VIEW, navUri);
        navigationIntent.setPackage("com.google.android.apps.maps");
        startActivity(navigationIntent);


    }

}
