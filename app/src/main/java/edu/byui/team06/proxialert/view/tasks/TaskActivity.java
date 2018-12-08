

package edu.byui.team06.proxialert.view.tasks;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
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
import edu.byui.team06.proxialert.utils.Permissions;
import edu.byui.team06.proxialert.utils.ProgressBarAdapter;
import edu.byui.team06.proxialert.view.maps.MapsActivity;

import static android.media.AudioFormat.CHANNEL_CONFIGURATION_MONO;

/**@author
 * @verion
 * TaskActivityClass handles
 * the task creation and update
 */
public class TaskActivity extends AppCompatActivity {
    final private String TAG = Permissions.class.getSimpleName();
    private DatabaseHelper db;
    private boolean isUpdate;
    private int position;
    private EditText inputTask;
    private EditText inputRadius;
    private TextView inputAddress;
    private EditText inputDueDate;
    private EditText inputTaskDesc;
    Spinner radiusUnits;
    private long id;
    private int mDay;
    private int mMonth;
    private int mYear;
    private boolean theme;
    private String units;
    private String latitudeString;
    private String longitudeString;
    private String isComplete;
    private MediaRecorder mRecorder;
    final private String myDateFormat = "MM/dd/yyyy";
    final private String [] items = {
            "Units...",
            "Miles",
            "Km",
            "Feet",
            "Meters"};

    private String _audioFilename;
    private boolean isRecorderStarted;
    long recorderStartTime;
    private ProgressBarAdapter pba;
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
        inputTaskDesc = findViewById(R.id.taskDescription);
        isRecorderStarted = false;
        _audioFilename = "";

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, items) {
            /**
             * isEnabled
             * disables the first item of the units drop down.
             *
             * @param position
             * @return
             */
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

            /**
             * getDropDownView creates the drop down and
             * sets the color of each item in the list based
             * on the Theme.
             * @param position
             * @param convertView
             * @param parent
             * @return
             */
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    if (theme) {
                        tv.setTextColor(Color.WHITE);
                    } else {
                        tv.setTextColor(Color.BLACK);
                    }
                }
                return view;
            }


        };
        radiusUnits.setAdapter(adapter);
        if (theme) {
            radiusUnits.setBackgroundColor(getResources().getColor(R.color.dropDownGray));
        }
        inputDueDate.setOnClickListener(new View.OnClickListener() {
            /**
             * onClick opens up a Calendar picker
             * for due date selection
             *
             * @param v
             */
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
                mDatePicker.show();
            }
        });

        if (isUpdate) {
            TextView title = findViewById(R.id.dialog_title);
            title.setText("Update Task");
            inputTask.setText(intent.getStringExtra("TASK"));
            inputAddress.setText(intent.getStringExtra("ADDRESS"));
            inputDueDate.setText(intent.getStringExtra("DUE"));
            latitudeString = intent.getStringExtra("LAT");
            longitudeString = intent.getStringExtra("LONG");
            units = intent.getStringExtra("UNITS");
            isComplete = intent.getStringExtra("COMPLETE");
            int count = 0;
            for (String s : items) {
                if (units.equals(s))
                    break;
                count++;
            }


            radiusUnits.setSelection(count);
            inputRadius.setText(intent.getStringExtra("RADIUS"));
            inputTaskDesc.setText(intent.getStringExtra("DESCRIPTION"));
            //radius.setSelection(intent.getStringExtra("RADIUS"));
            id = intent.getIntExtra("ID", -1);
            _audioFilename = intent.getStringExtra("AUDIO");

        } else {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(this);
            String prefUnits = sp.getString("ProxiUnits", "Units...");
            int count = 0;
            for (String s : items) {
                if (prefUnits.contains(s))
                    break;
                count++;
            }
            radiusUnits.setSelection(count);

        }
        Permissions mic = new Permissions();
        if(!mic.checkMicPermission(this)) {
            mic.askMicPermission(this);
        }
    }


    public void onStartRecording(View view) {
        if (isRecorderStarted) {
            pba.cancel(true);
            mRecorder.stop();
            mRecorder.release();
            isRecorderStarted = false;
        } else {
            if (new Permissions().checkMicPermission(this)) {

                //TODO Start a ten second timer which will stop the recorder AND fill up the status bar.
                File f = getFilesDir();
                Integer nextId = db.getLastInsertId() + 1;
                String nextIdString = nextId.toString();
                _audioFilename = f.getAbsolutePath() + "/ProxiVoiceOver" + nextIdString + ".3gp";
                ProgressBar bar = findViewById(R.id.progressBar);
                pba = new ProgressBarAdapter(bar);
                pba.execute("");
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mRecorder.setAudioEncodingBitRate(133333);
                mRecorder.setAudioSamplingRate(44100);
                mRecorder.setOutputFile(_audioFilename);

                try {
                    mRecorder.prepare();
                } catch (IOException e) {
                    Log.e(TAG, "prepare() failed");
                }

                mRecorder.start();
                isRecorderStarted = true;
                new CountDownTimer(10000, 10) {

                    public void onTick(long millis) {
                        ; //do nothing.
                    }

                    @Override
                    public void onFinish() {
                        if (isRecorderStarted) {
                            mRecorder.stop();
                            mRecorder.release();
                            isRecorderStarted = false;
                            Toast.makeText(TaskActivity.this, "Max Length is 10 seconds. Recorder Stopped", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.start();


            } else {
                new Permissions().askMicPermission(this);

            }
        }
    }



    public void onCancelButton(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onSaveButton(View view) {


        Intent intent = new Intent();
        intent.putExtra("UPDATE", isUpdate);
        intent.putExtra("POSITION", position);
        String task = inputTask.getText().toString();
        String address = inputAddress.getText().toString();
        String dueDate = inputDueDate.getText().toString();
        String unitsString = radiusUnits.getSelectedItem().toString();
        String radiusString = inputRadius.getText().toString();
        String description = inputTaskDesc.getText().toString();
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

        if(description.length() == 0) {
            Toast.makeText(TaskActivity.this, "Please enter a description.", Toast.LENGTH_SHORT).show();
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
            element.setRadius(radiusString);
            element.setUnits(unitsString);
            element.setTask(task);
            element.setTimeStamp(t.toString());
            element.setLong(longitudeString);
            element.setLat(latitudeString);
            element.setDescription(description);
            element.setComplete(isComplete);
            element.setAudio(_audioFilename);
            db.updateTask(element);
        } else {
            id = db.insertTask(task, address, dueDate, radiusString, unitsString, t.toString(),
                    latitudeString, longitudeString, description, "false", _audioFilename);
        }

        intent.putExtra("id", id);
        setResult(RESULT_OK, intent);
        finish();

    }

    // Geofence
    // button to open MapsActivity
    public void startMapActivity(View view) {
        Intent mapIntent = new Intent(TaskActivity.this, MapsActivity.class);
        mapIntent.putExtra("TaskName", inputTask.getText().toString());
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
