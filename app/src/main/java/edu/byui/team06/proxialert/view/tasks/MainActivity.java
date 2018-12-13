package edu.byui.team06.proxialert.view.tasks;
//TODO Separate Geofencing into a class
//TODO Connect V-tiger (if time permits)
//TODO Mapview of Tasks
//TODO Write up the Post-Mortem.
//TODO Make a video of the application.

//notification imports (many could probably be removed
//since it was moved to its own class

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.ProxiDB;
import edu.byui.team06.proxialert.utils.Geofences;
import edu.byui.team06.proxialert.utils.MyDividerItemDecoration;
import edu.byui.team06.proxialert.utils.RecyclerTouchListener;
import edu.byui.team06.proxialert.utils.ScheduledNotificationPublisher;
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
    private FloatingActionButton fab;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Geofences geofences = new Geofences(this, this);

    private DatabaseHelper db;
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
        geofences.initGeofencing(taskCount, taskList);

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
        removeScheduledNotification(taskList.get(position));
        taskList.remove(position);
        mAdapter.notifyItemRemoved(position);
        toggleEmptyTasks();
        geofences.resetGeofences(taskCount, taskList);

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
                        geofences.clearGeofenceClient();
                        scheduleNotification(element);
                    } else {
                        element.setComplete("true");
                        removeScheduledNotification(element);
                    }

                    db.updateTask(element);
                    taskList.clear();
                    taskList.addAll(db.getAllTasks());
                    mAdapter.notifyDataSetChanged();
                    geofences.resetGeofences(taskCount, taskList);
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
                    removeScheduledNotification(element);
                    scheduleNotification(element);
                } else {
                    taskList.add(taskList.size(), element);
                    mAdapter.notifyDataSetChanged();
                    scheduleNotification(element);
                }

                taskList.clear();
                taskList.addAll(db.getAllTasks());
                mAdapter.notifyDataSetChanged();
                geofences.resetGeofences(taskCount, taskList);

                //Update the view.
                mAdapter.notifyDataSetChanged();
                toggleEmptyTasks();
            }
            else if(requestCode == SETTINGS_ACTION)
            {
                //Remove
                for(ProxiDB task : taskList)
                {
                    //rescheduling all the tasks overwrites ALL
                    //the previously scheduled notifications.
                    scheduleNotification(task);
                }
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


    public void scheduleNotification(ProxiDB task) {

        if(Boolean.parseBoolean(task.getComplete()))
            return;

        //create the pending intent
        Intent notificationIntent = new Intent(MainActivity.this, ScheduledNotificationPublisher.class);
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
        Intent notificationIntent = new Intent(MainActivity.this, ScheduledNotificationPublisher.class);
        notificationIntent.putExtra("TaskID", task.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), task.getId(), notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }
}
