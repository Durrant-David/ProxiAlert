package edu.byui.team06.proxialert.view.tasks;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.byui.team06.proxialert.R;
import edu.byui.team06.proxialert.database.DatabaseHelper;
import edu.byui.team06.proxialert.database.model.ProxiDB;
import edu.byui.team06.proxialert.view.maps.MapsActivity;

public class TaskActivity extends AppCompatActivity {
    private List<ProxiDB> taskList = new ArrayList<>();
    private DatabaseHelper db;
    private boolean isUpdate;
    private int position;
    private EditText inputTask;
    private EditText inputRadius;
    private TextView inputAddress;
    private EditText inputDueDate;
    Spinner radiusUnits;
    private long id;
    private int mDay;
    private int mMonth;
    private int mYear;
    private boolean theme;
    private String latitudeString;
    private String longitudeString;
    final private String myDateFormat = "MM/dd/yyyy";
    final private String [] items = {
            "Units...",
            "Miles",
            "Km",
            "Feet",
            "Meters"};

    final private int MAP_ACTIVITY_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        boolean themeName = pref.getBoolean("themes", false);
        if (themeName) {
            setTheme(R.style.ThemeOverlay_MaterialComponents_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        theme = themeName;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        db = new DatabaseHelper(this);

        Intent intent = getIntent();
        isUpdate = intent.getBooleanExtra("UPDATE", false);
        position = intent.getIntExtra("POSITION", -1);
        inputTask = findViewById(R.id.task);
        inputAddress = findViewById(R.id.address);
        inputDueDate = findViewById(R.id.dueDate);
        radiusUnits = findViewById(R.id.radiusUnits);
        inputRadius = findViewById(R.id.radius);



        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, items) {
            @Override
        public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }
        @Override
        public View getDropDownView(int position, View convertView,
                ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView tv = (TextView) view;
            if(position == 0){
                // Set the hint text color gray
                tv.setTextColor(Color.GRAY);
            }
            else {
                if (theme){
                    tv.setTextColor(Color.WHITE);
            } else {
                    tv.setTextColor(Color.BLACK);
                }
            }
            return view;
        }



    };
        radiusUnits.setAdapter(adapter);
        if(theme) {
            radiusUnits.setBackgroundColor(getResources().getColor(R.color.dropDownGray));
        }
        inputDueDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //To show current date in the datepicker
                Calendar myCalendar = Calendar.getInstance();
                mYear = myCalendar.get(Calendar.YEAR);
                mMonth = myCalendar.get(Calendar.MONTH);
                mDay = myCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(TaskActivity.this,
                        theme ? R.style.UserDialogDark : R.style.UserDialog,
                        new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR, selectedyear);
                        myCalendar.set(Calendar.MONTH, selectedmonth);
                        myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);
                        //Change as you need
                        SimpleDateFormat sdf = new SimpleDateFormat(myDateFormat, Locale.ENGLISH);
                        inputDueDate.setText(sdf.format(myCalendar.getTime()));
                        mDay = selectedday;
                        mMonth = selectedmonth;
                        mYear = selectedyear;
                    }
                }, mYear, mMonth, mDay);
                //mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });

        if (isUpdate) {
            TextView title = findViewById(R.id.dialog_title);
            title.setText("Update Task");
            inputTask.setText(intent.getStringExtra("TASK"));
            inputAddress.setText(intent.getStringExtra("ADDRESS"));
            inputDueDate.setText(intent.getStringExtra("DUE"));
            String radiusString = intent.getStringExtra("RADIUS");
            latitudeString = intent.getStringExtra("LAT");
            longitudeString = intent.getStringExtra("LONG");
            int count = 0;
            for(String s: items) {
                if (radiusString.contains(s))
                    break;
                count++;
            }

            radiusUnits.setSelection(count);
            radiusString = radiusString.replace(' ' + radiusUnits.getSelectedItem().toString(), "");
            inputRadius.setText(radiusString);
            //radius.setSelection(intent.getStringExtra("RADIUS"));
            id = intent.getIntExtra("ID", -1);

        }

        else {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(this);
            String units = sp.getString("ProxiUnits", "Units...");
            int count = 0;
            for(String s: items) {
                if (units.contains(s))
                    break;
                count++;
            }
            radiusUnits.setSelection(count);

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
        String unitsString = radiusUnits.getSelectedItem().toString();
        String radiusString = inputRadius.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat(myDateFormat, Locale.ENGLISH);
        Date date;
        long timeStamp;



        if(task.length() == 0) {
            Toast.makeText(TaskActivity.this, "Please enter a task name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.length() == 0) {
            Toast.makeText(TaskActivity.this, "Please set an address or location.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (unitsString.equals(items[0])) {
            Toast.makeText(TaskActivity.this, "Please select the proximity units.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(radiusString.length() == 0) {
            Toast.makeText(TaskActivity.this, "Please enter the proximity value.", Toast.LENGTH_SHORT).show();
            return;
        }
        //One more to see if the radius value is empty.

        try {
            date = sdf.parse(inputDueDate.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(TaskActivity.this, "Please enter a due date.", Toast.LENGTH_SHORT).show();
            return;
        }
        timeStamp = date.getTime();
        Long t = timeStamp;


        if (isUpdate) {
            ProxiDB element = db.getProxiDB(id);
            element.setAddress(address);
            element.setDueDate(dueDate);
            element.setRadius(radiusString + ' ' + unitsString);
            element.setTask(task);
            element.setTimeStamp(t.toString());
            element.setLong(latitudeString);
            element.setLat(longitudeString);
            db.updateTask(element);
        } else {
            id = db.insertTask(task, address, dueDate, radiusString + ' ' + unitsString, t.toString(), latitudeString, longitudeString);
        }

        intent.putExtra("id", id);
        setResult(RESULT_OK, intent);
        finish();

    }

    // Geofence
    // button to open MapsActivity
    protected void startMapActivity(View view) {
        Intent mapIntent = new Intent(TaskActivity.this, MapsActivity.class);
        startActivityForResult(mapIntent, MAP_ACTIVITY_CODE);
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
        if (requestCode == MAP_ACTIVITY_CODE) {

            //If the result was set to Ok, then we will update the Views.
            if (resultCode == RESULT_OK) {

               inputAddress.setText(data.getStringExtra("ADDRESS"));
               String coordinates = data.getStringExtra("COORDINATES");
               latitudeString = coordinates.substring(coordinates.indexOf('(') + 1, coordinates.indexOf(','));
               longitudeString = coordinates.substring(coordinates.indexOf(',') + 1, coordinates.indexOf(')'));
               //will also need to fetch the coordinates.
            }
        }
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