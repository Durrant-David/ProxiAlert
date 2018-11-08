package edu.byui.team06.proxialert.database.model;

public class ProxiDB {
    public static final String TABLE_NAME = "notes";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_DUEDATE = "duedate";
    public static final String COLUMN_RADIUS = "radius";

    private int _id;
    private String _task;
    private String _address;
    private String _dueDate;
    private int _radius;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TASK + " TEXT,"
                    + COLUMN_ADDRESS + " TEXT,"
                    + COLUMN_DUEDATE + " TEXT,"
                    + COLUMN_RADIUS + " INTEGER"
                    + ")";

    public ProxiDB() {
    }

    public ProxiDB(int id, String task, String address, String dueDate, int radius) {
        _id = id;
        _task = task;
        _address = address;
        _dueDate = dueDate;
        _radius = radius;
    }

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String getTask() {
        return _task;
    }

    public void setTask(String _task) {
        this._task = _task;
    }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String _address) {
        this._address = _address;
    }

    public String getDueDate() {
        return _dueDate;
    }

    public void setDueDate(String _dueDate) {
        this._dueDate = _dueDate;
    }

    public int getRadius() {
        return _radius;
    }

    public void setRadius(int _radius) {
        this._radius = _radius;
    }
}