package edu.byui.team06.proxialert.database.model;

public class ProxiDB {
    public static final String TABLE_NAME = "tasks";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_DUEDATE = "dueDate";
    public static final String COLUMN_RADIUS = "radius";
    public static final String COLUMN_TS = "Timestamp";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_LONG = "longitude";
    public static final String COLUMN_UNITS = "units";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COMPLETE = "complete";
    public static final String COLUMN_AUDIO = "audio";
    public static final String COLUMN_CONTACT = "contact";


    private int _id;
    private String _task;
    private String _address;
    private String _dueDate;
    private String _radius;
    private String _timeStamp;
    private String _latitude;
    private String _longitude;
    private String _units;
    private String _description;
    private String _complete;
    private String _audio;
    private String _contact;
    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TASK + " TEXT,"
                    + COLUMN_ADDRESS + " TEXT,"
                    + COLUMN_DUEDATE + " TEXT,"
                    + COLUMN_RADIUS + " TEXT,"
                    + COLUMN_UNITS + " TEXT,"
                    + COLUMN_TS + " TEXT,"
                    + COLUMN_LAT + " TEXT,"
                    + COLUMN_LONG + " TEXT,"
                    + COLUMN_DESCRIPTION + " TEXT,"
                    + COLUMN_COMPLETE + " TEXT,"
                    + COLUMN_AUDIO + " TEXT,"
                    + COLUMN_CONTACT + " TEXT"
                    +")";

    public ProxiDB() {
    }

    public ProxiDB(int id, String task, String address, String dueDate, String radius,
                   String radiusUnits, String timeStamp, String latitude, String longitude,
                   String description, String complete, String audio, String contactInfo) {
        _id = id;
        _task = task;
        _address = address;
        _dueDate = dueDate;
        _radius = radius;
        _units = radiusUnits;
        _timeStamp = timeStamp;
        _latitude = latitude;
        _longitude = longitude;
        _description = description;
        _complete = complete;
        _audio = audio;
        _contact = contactInfo;
    }

    public String getLat() { return _latitude; }

    public void setLat(String latitude) { _latitude = latitude; }

    public String getLong() { return _longitude;}

    public void setLong(String longitude) { _longitude = longitude; }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public String getTask() {
        return _task;
    }

    public void setTask(String task) {
        _task = task;
    }

    public void setTimeStamp(String timeStamp) { _timeStamp = timeStamp; }

    public String getTimeStamp() { return _timeStamp; }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String address) {
        _address = address;
    }

    public String getDueDate() {
        return _dueDate;
    }

    public void setDueDate(String dueDate) {
        _dueDate = dueDate;
    }

    public String getRadius() {
        return _radius;
    }

    public void setRadius(String radius) {
        _radius = radius;
    }

    public String getUnits() { return _units; }

    public void setUnits(String units) { _units = units; }

    public void setDescription(String description) { _description = description; }

    public String getDescription() { return _description; }

    public void setComplete(String isComplete) { _complete = isComplete; }

    public String getComplete() { return _complete;}

    public void setAudio(String audio) { _audio = audio; }

    public String getAudio () { return _audio; }

    public void setContactInfo(String contactInfo) { _contact = contactInfo; }

    public String getContact() { return _contact; }
}