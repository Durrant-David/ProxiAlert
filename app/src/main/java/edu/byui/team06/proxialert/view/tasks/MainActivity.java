package edu.byui.team06.proxialert.view.tasks;

//notification imports (many could probably be removed
//since it was moved to its own class

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.ProxiDB;
import edu.byui.team06.proxialert.utils.MyDividerItemDecoration;
import edu.byui.team06.proxialert.utils.Permissions;
import edu.byui.team06.proxialert.utils.RecyclerTouchListener;
import edu.byui.team06.proxialert.view.TaskAdapter;
import edu.byui.team06.proxialert.view.settings.SettingsActivity;

//database imports


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
    private Permissions permissions;

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
        // Location permissions
        permissions = new Permissions();
        if ( permissions.checkMapsPermission(this)){

        } else {
            permissions.askMapsPermission(this);
        }
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
}
