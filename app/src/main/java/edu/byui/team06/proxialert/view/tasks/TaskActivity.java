package edu.byui.team06.proxialert.view.tasks;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.ProxiDB;

public class TaskActivity extends AppCompatActivity {
    private List<ProxiDB> taskList = new ArrayList<>();
    private DatabaseHelper db;
    private boolean isUpdate;
    private int position;
    private EditText inputTask;
    private TextView inputAddress;
    private EditText inputDueDate;
    private EditText inputRadius;
    private long id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        db = new DatabaseHelper(this);

        Intent intent = getIntent();
        isUpdate = intent.getBooleanExtra("UPDATE", false);
        position = intent.getIntExtra("POSITION", -1);


        inputTask = findViewById(R.id.task);
        inputAddress = findViewById(R.id.address);
        inputDueDate = findViewById(R.id.dueDate);
        inputRadius = findViewById(R.id.radius);

        if(isUpdate)
        {
            TextView title = findViewById(R.id.dialog_title);
            title.setText("Update Task");
            inputTask.setText(intent.getStringExtra("TASK"));
            inputAddress.setText(intent.getStringExtra("ADDRESS"));
            inputDueDate.setText(intent.getStringExtra("DUE"));
            inputRadius.setText(intent.getStringExtra("RADIUS"));
            id = intent.getIntExtra("ID", -1);

        }
    }


    protected void onCancelButton(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void onSaveButton(View view) {


        Intent intent = new Intent();
        intent.putExtra("UPDATE", isUpdate);
        intent.putExtra("POSITION", position);
        String task = inputTask.getText().toString();
        String address = inputAddress.getText().toString();
        String dueDate = inputDueDate.getText().toString();
        String radius = inputRadius.getText().toString();


        if(isUpdate) {
            ProxiDB element = db.getProxiDB(id);
            element.setAddress(address);
            element.setDueDate(dueDate);
            element.setRadius(radius);
            element.setTask(task);
            db.updateTask(element);
        } else {
            id = db.insertTask(task, address, dueDate, radius);
        }

            intent.putExtra("id", id);
            setResult(RESULT_OK, intent);
            finish();

    }
}
    /*
    private void showTaskDialog(final boolean shouldUpdate, final ProxiDB proxiDB, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.task_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(TaskActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputTask = view.findViewById(R.id.task);
        final TextView inputAddress = view.findViewById(R.id.address);
        final EditText inputDueDate = view.findViewById(R.id.dueDate);
        final EditText inputRadius = view.findViewById(R.id.radius);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_task_title) : getString(R.string.lbl_edit_task_title));

        if (shouldUpdate && proxiDB != null) {
            inputTask.setText(proxiDB.getTask());
        }
//        alertDialogBuilderUserInput
//                .setCancelable(false)
//                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogBox, int id) {
//
//                    }
//                })
//                .setNegativeButton("cancel",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialogBox, int id) {
//                                dialogBox.cancel();
//                            }
//                        });
//
//        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
//        alertDialog.show();
//
//        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Show toast message when no text is entered
//                if (TextUtils.isEmpty(inputTask.getText().toString())) {
//                    Toast.makeText(TaskActivity.this, "Enter task!", Toast.LENGTH_SHORT).show();
//                    return;
//                } else {
//                    alertDialog.dismiss();
//                }
//
//                // check if user updating note
//                if (shouldUpdate && proxiDB != null) {
//                    // update note by it's id
//                    updateTask(inputTask.getText().toString(),
//                            inputAddress.getText().toString(),
//                            inputDueDate.getText().toString(),
//                            inputRadius.getText().toString(),
//                            position);
//                } else {
//                    // create new note
//                    createTask(inputTask.getText().toString(),
//                            inputAddress.getText().toString(),
//                            inputDueDate.getText().toString(),
//                            inputRadius.getText().toString()
//                    );
//                }
//            }
//        });
    }

    private void createTask(String task, String address, String dueDate, String radius) {
        // inserting task in db and getting
        // newly inserted task id

        //Toast toast = Toast.makeText(this, "test"+ id, Toast.LENGTH_SHORT);
       // toast.show();
        // get the newly inserted task from db
        //ProxiDB n = db.getProxiDB(id);

        //if (n != null) {
            // adding new task to array list at 0 position
            //taskList.add(0, n);

            // refreshing the list
            //mAdapter.notifyDataSetChanged();

            //toggleEmptyTasks();
       // }
    }

    private void updateTask(String task, String address, String dueDate, String radius, int position) {
        ProxiDB n = taskList.get(position);
        // updating task text
        n.setTask(task);
        n.setAddress(address);
        n.setDueDate(dueDate);
        n.setRadius(radius);

        // updating task in db
        db.updateTask(n);

        // refreshing the list
        taskList.set(position, n);
        //mAdapter.notifyItemChanged(position);

        //toggleEmptyTasks();
    }
}
*/