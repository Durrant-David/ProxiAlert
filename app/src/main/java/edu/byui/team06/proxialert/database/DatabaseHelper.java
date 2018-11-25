package edu.byui.team06.proxialert.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.byui.team06.proxialert.database.model.ProxiDB;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ProxiDB";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(ProxiDB.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + ProxiDB.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertTask(String task, String address, String dueDate, String radius, String timeStamp, String latitude, String longitude) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProxiDB.COLUMN_TASK, task);
        values.put(ProxiDB.COLUMN_ADDRESS, address);
        values.put(ProxiDB.COLUMN_DUEDATE, dueDate);
        values.put(ProxiDB.COLUMN_RADIUS, radius);
        values.put(ProxiDB.COLUMN_TS, timeStamp);
        values.put(ProxiDB.COLUMN_LAT, latitude);
        values.put(ProxiDB.COLUMN_LONG, longitude);

        // insert row
        long id = db.insert(ProxiDB.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public ProxiDB getProxiDB(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(ProxiDB.TABLE_NAME,
                new String[]{ProxiDB.COLUMN_ID,
                        ProxiDB.COLUMN_TASK,
                        ProxiDB.COLUMN_ADDRESS,
                        ProxiDB.COLUMN_DUEDATE,
                        ProxiDB.COLUMN_RADIUS,
                        ProxiDB.COLUMN_TS,
                        ProxiDB.COLUMN_LAT,
                        ProxiDB.COLUMN_LONG},
                ProxiDB.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Log.v("test", "-" + cursor.getInt(cursor.getColumnIndex(ProxiDB.COLUMN_ID)));
        ProxiDB proxiDB = new ProxiDB(
                cursor.getInt(cursor.getColumnIndex(ProxiDB.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_TASK)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_ADDRESS)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_DUEDATE)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_RADIUS)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_TS)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_LAT)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_LONG)));

        // close the db connection
        cursor.close();

        return proxiDB;
    }

    public List<ProxiDB> getAllTasks() {
        List<ProxiDB> tasks = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + ProxiDB.TABLE_NAME + " ORDER BY " +
                ProxiDB.COLUMN_TS + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ProxiDB proxiDB = new ProxiDB();
                proxiDB.setId(cursor.getInt(cursor.getColumnIndex(ProxiDB.COLUMN_ID)));
                proxiDB.setTask(cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_TASK)));
                proxiDB.setAddress(cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_ADDRESS)));
                proxiDB.setDueDate(cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_DUEDATE)));
                proxiDB.setRadius(cursor.getString(cursor.getColumnIndex(proxiDB.COLUMN_RADIUS)));
                proxiDB.setTimeStamp(cursor.getString(cursor.getColumnIndex(proxiDB.COLUMN_TS)));
                proxiDB.setLat(cursor.getString(cursor.getColumnIndex(proxiDB.COLUMN_LAT)));
                proxiDB.setLong(cursor.getString(cursor.getColumnIndex(proxiDB.COLUMN_LONG)));
                tasks.add(proxiDB);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();
        cursor.close();
        
        // return tasks list
        return tasks;
    }

    public int getTaskCount() {
        String countQuery = "SELECT  * FROM " + ProxiDB.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateTask(ProxiDB proxiDB) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProxiDB.COLUMN_TASK, proxiDB.getTask());
        values.put(ProxiDB.COLUMN_ADDRESS, proxiDB.getAddress());
        values.put(ProxiDB.COLUMN_DUEDATE, proxiDB.getDueDate());
        values.put(ProxiDB.COLUMN_RADIUS, proxiDB.getRadius());
        values.put(ProxiDB.COLUMN_TS, proxiDB.getTimeStamp());
        values.put(ProxiDB.COLUMN_LAT, proxiDB.getLat());
        values.put(ProxiDB.COLUMN_LONG, proxiDB.getLong());

        // updating row
        return db.update(ProxiDB.TABLE_NAME, values, ProxiDB.COLUMN_ID + " = ?",
                new String[]{String.valueOf(proxiDB.getId())});
    }

    public void deleteTask(ProxiDB task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ProxiDB.TABLE_NAME, ProxiDB.COLUMN_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
    }
}