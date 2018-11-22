package edu.byui.team06.proxialert.view.tasks;

//notification imports (many could probably be removed
//since it was moved to its own class
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

//database imports
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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
import edu.byui.team06.proxialert.utils.RecyclerTouchListener;
import edu.byui.team06.proxialert.utils.Notification;
import edu.byui.team06.proxialert.view.TaskAdapter;
import edu.byui.team06.proxialert.view.settings.SettingsActivity;


import static java.lang.Math.sqrt;


public class MainActivity extends AppCompatActivity {
    protected TaskAdapter mAdapter;
    private List<ProxiDB> taskList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noTaskView;
    public static final String UPDATE = "UPDATE";
    public static final String POSITION = "POSITION";
    int taskCount;
    private static final int TASK_ACTIVITY_CODE = 0;

    private DatabaseHelper db;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    public void startSettings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //This is temporary. We can send a lot more notifications later, but for now
        //it just sends immediately.
        Notification n = new Notification("Test", "This is working",
                "I'm working and this is longer " +
                        "text that can be read if the notification is expanded.",
                this.getApplicationContext());
        n.send();
      
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

        mAdapter = new TaskAdapter(this, taskList);
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
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }


    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private void deleteTask(int position) {
        // deleting the note from db
        db.deleteTask(taskList.get(position));

        // removing the note from the list
        taskList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyTasks();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                    taskIntent.putExtra("ID", element.getId());
                    taskIntent.putExtra("TASK", element.getTask());
                    taskIntent.putExtra("DUE", element.getDueDate());
                    startActivityForResult(taskIntent, TASK_ACTIVITY_CODE);
                    //showTaskDialog(true, taskList.get(position), position);
                } else {
                    deleteTask(position);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that it is the SecondActivity with an OK result
        if (requestCode == TASK_ACTIVITY_CODE) {
            if (resultCode == RESULT_OK) {

                boolean isUpdate = data.getBooleanExtra("UPDATE", false);
                long id = data.getLongExtra("id", 0);
                ProxiDB element = db.getProxiDB(id);
                if(isUpdate) {
                    taskList.set(data.getIntExtra("POSITION", 0), element);

                } else {

                    taskList.add(0, element);
                }
                mAdapter.notifyDataSetChanged();
                toggleEmptyTasks();
                // Set text view with string
            }
        }
    }


    /**
     * Toggling list and empty notes view
     */
    public void toggleEmptyTasks() {
        // you can check notesList.size() > 0

        if (db.getTaskCount() > 0) {
            noTaskView.setVisibility(View.GONE);
        } else {
            noTaskView.setVisibility(View.VISIBLE);
        }
    }


    static public Boolean getDistance(float x1, float y1, float x2, float y2, float distance) {
        return distance <= sqrt(Math.pow(x2 - x1, 2) + Math.pow((y2-y1), 2));
    }
}
