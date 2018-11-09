package edu.byui.team06.proxialert.database.model;

public class ProxiDB {
    public static final String TABLE_NAME = "tasks";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TASK = "task";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_DUEDATE = "dueDate";
    public static final String COLUMN_RADIUS = "radius";

    private int _id;
    private String _task;
    private String _address;
    private String _dueDate;
    private String _radius;


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

    public ProxiDB(int id, String task, String address, String dueDate, String radius) {
        _id = id;
        _task = task;
        _address = address;
        _dueDate = dueDate;
        _radius = radius;
    }

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
}