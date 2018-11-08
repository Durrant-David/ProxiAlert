package edu.byui.team06.proxialert.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import edu.byui.team06.proxialert.database.model.ProxiDB;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "notes_db";


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

    public long insertNote(String note) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(ProxiDB.COLUMN_TASK, note);

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
                new String[]{ProxiDB.COLUMN_ID, ProxiDB.COLUMN_TASK, ProxiDB.COLUMN_ADDRESS},
                ProxiDB.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        ProxiDB proxiDB = new ProxiDB(
                cursor.getInt(cursor.getColumnIndex(ProxiDB.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_TASK)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_ADDRESS)),
                cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_DUEDATE)),
                cursor.getInt(cursor.getColumnIndex(ProxiDB.COLUMN_RADIUS)));

        // close the db connection
        cursor.close();

        return proxiDB;
    }

    public List<ProxiDB> getAllNotes() {
        List<ProxiDB> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + ProxiDB.TABLE_NAME + " ORDER BY " +
                ProxiDB.COLUMN_ADDRESS + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ProxiDB proxiDB = new ProxiDB();
                proxiDB.setId(cursor.getInt(cursor.getColumnIndex(ProxiDB.COLUMN_ID)));
                proxiDB.setTask(cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_TASK)));
                proxiDB.setAddress(cursor.getString(cursor.getColumnIndex(ProxiDB.COLUMN_ADDRESS)));

                notes.add(proxiDB);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + ProxiDB.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateNote(ProxiDB note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ProxiDB.COLUMN_TASK, note.getTask());

        // updating row
        return db.update(ProxiDB.TABLE_NAME, values, ProxiDB.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }

    public void deleteNote(ProxiDB note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ProxiDB.TABLE_NAME, ProxiDB.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
    }
}