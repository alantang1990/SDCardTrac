/*
 * SDCardTrac application - keeps track of the /sdcard usage
 * Copyright (C) 2012 Narendra M.A.
*/

package com.nma.util.sdcardtrac;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// Class to manage the SQLite database

class DatabaseManager {
    public static final String MYDATABASE_NAME = "SDCARD_TRACK";
    public static final String MYDATABASE_TABLE = "TRACKING_TABLE";
    public static final int MYDATABASE_VERSION = 1;
    public static final String ID_COLUMN = "_id";
    public static final String DELTA_COLUMN = "DeltaSize";
    public static final String LOG_COLUMN = "ChangeLog";

    //create table MY_DATABASE (ID integer primary key, Content text not null);
    static final String SCRIPT_CREATE_DATABASE =
	"create table " + MYDATABASE_TABLE + " ("
	+ ID_COLUMN + " integer primary key not null,"
	+ DELTA_COLUMN + " integer, "
	+ LOG_COLUMN + " string);";

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    private Context context;

    // Helper
    public DatabaseManager(Context c){
	context = c;
    }

    public DatabaseManager openToRead() throws android.database.SQLException {
	sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
	sqLiteDatabase = sqLiteHelper.getReadableDatabase();
	return this; 
    }

    public DatabaseManager openToWrite() throws android.database.SQLException {
	sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
	sqLiteDatabase = sqLiteHelper.getWritableDatabase();
	return this; 
    }

    public void close(){
	sqLiteHelper.close();
    }

    // TODO Use table name
    public long insert(String toTable, long timeStamp, long deltaSize, String changeLog){
	ContentValues contentValues = new ContentValues();
	contentValues.put(ID_COLUMN, timeStamp);
	contentValues.put(DELTA_COLUMN, deltaSize);
	contentValues.put(LOG_COLUMN, changeLog);
	return sqLiteDatabase.insert(MYDATABASE_TABLE, null, contentValues);
    }

    public int deleteAll(){
        if (SettingsActivity.ENABLE_DEBUG)
            Log.d(getClass().getName(), "Deleting all!!!");
	    return sqLiteDatabase.delete(MYDATABASE_TABLE, "1", null);
    }

    public void deleteRows(long upto) {
        String selectCrit;
        selectCrit = ID_COLUMN + " < " + upto;

        if (SettingsActivity.ENABLE_DEBUG)
            Log.d(getClass().getName(), "Running delete: " + selectCrit);
        int numRows = sqLiteDatabase.delete(MYDATABASE_TABLE, selectCrit, null);
        if (SettingsActivity.ENABLE_DEBUG)
            Log.d(getClass().getName(), "Deleted " + numRows + " rows");
    }

    // Retrieve database content between startTime and endTime (0 means don't care)
    public List<ContentValues> getValues(String fromTable, long startTime, long endTime){
	String selectCrit;
		
	if (startTime == 0 && endTime == 0) {
	    selectCrit = null;
	} else if (startTime == 0) {
	    selectCrit = ID_COLUMN + " <= " + Long.toString(endTime);
	} else if (endTime == 0) {
	    selectCrit = ID_COLUMN + " >= " + Long.toString(startTime);
	} else {
	    selectCrit = ID_COLUMN + " <= " + Long.toString(endTime)
		+ " and " + ID_COLUMN + " >= " + Long.toString(startTime);
	}
	
	return runSelect(selectCrit);	
    }

    public List<ContentValues> searchValues(String fromTable, String query) {
	String selectCrit;

	selectCrit = LOG_COLUMN + " like '%" + query + "%'";
	return runSelect(selectCrit);
    }

    private List<ContentValues> runSelect(String selectCrit) {
	ArrayList <ContentValues> retList = new ArrayList<ContentValues>();
	if (SettingsActivity.ENABLE_DEBUG)
	    Log.d(getClass().getName(), "Running select: " + selectCrit);
	//Cursor cursor = sqLiteDatabase.rawQuery
	//    ("select * from " + MYDATABASE_TABLE + " " + selectCrit + ";", null);
	Cursor cursor = sqLiteDatabase.query(MYDATABASE_TABLE, null, selectCrit,
					     null, null, null, ID_COLUMN + " ASC", null);

	int indTime = cursor.getColumnIndex(ID_COLUMN);
	int indDelta = cursor.getColumnIndex(DELTA_COLUMN);
	int indLog = cursor.getColumnIndex(LOG_COLUMN);
	for(cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()){
	    ContentValues currRow = new ContentValues();
	    currRow.put(ID_COLUMN, cursor.getString(indTime));
	    currRow.put(DELTA_COLUMN, cursor.getString(indDelta));
	    currRow.put(LOG_COLUMN, cursor.getString(indLog));
			
	    retList.add(currRow);
	}

	return retList;
    }
}
