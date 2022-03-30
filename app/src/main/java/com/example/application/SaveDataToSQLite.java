package com.example.application;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.List;

public class SaveDataToSQLite extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ALARM_APPLICATION_DATABASE.sqlite";
    public static final int VERSION = 2;
    public static final String TABLE_NAME = "List_Item_Alarm";
    public static final String COLUMN_NAME_ID = "ID";
    public static final String COLUMN_NAME_HOURS = "HOURS";
    public static final String COLUMN_NAME_MINUTES = "MINUTES";
    public static final String COLUMN_NAME_NOTES = "NOTES";
    public static final String COLUMN_NAME_REGULARS = "REGULARS";
    public static final String COLUMN_NAME_TIME_COUNTDOWN = "TIME_COUNTDOWN";
    public static final String COLUMN_NAME_STATE_ALARM = "STATE_ALARM";
    public static final String COLUMN_NAME_STATE_VIBRATE = "STATE_VIBRATE";

    public static final String TABLE_NAME_PENDING_INTENT = "Pending_Intent_Table";
    public static final String COLUMN_NAME_REQUEST_CODE = "REQUEST_CODE";
    public static final String COLUMN_NAME_ID_BYTE_ARRAY_PENDING_INTENT = "BYTE_ARRAY_PENDING_INTENT";

    public SaveDataToSQLite(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    //CREATE TABLE WITH NAME TABLE_NAME
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            String query = " CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    "(" + COLUMN_NAME_ID + " INT NOT NULL PRIMARY KEY, " +
                    COLUMN_NAME_HOURS + " VARCHAR(2), " +
                    COLUMN_NAME_MINUTES + " VARCHAR(2), " +
                    COLUMN_NAME_NOTES + " TEXT, " +
                    COLUMN_NAME_REGULARS + " TEXT, " +
                    COLUMN_NAME_TIME_COUNTDOWN + " NVARCHAR(30), " +
                    COLUMN_NAME_STATE_ALARM + " INT ," +
                    COLUMN_NAME_STATE_VIBRATE + " INT " + ")";
            String queryCreateTablePendingIntent = " CREATE TABLE IF NOT EXISTS " + SaveDataToSQLite.TABLE_NAME_PENDING_INTENT
                    + " ( " + SaveDataToSQLite.COLUMN_NAME_REQUEST_CODE + " INT NOT NULL PRIMARY KEY ,"
                    + SaveDataToSQLite.COLUMN_NAME_ID_BYTE_ARRAY_PENDING_INTENT + " BLOB " + ")";

            sqLiteDatabase.execSQL(query);
            sqLiteDatabase.execSQL(queryCreateTablePendingIntent);
        } catch (Exception e) {
            //Log.i("AAA","ERROR CREATE DATABASE : "+e.getMessage());
        }
    }


    //DROP TABLE TABLE_NAME AND RE-CREATE TABLE_NAME
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS listItemAlarm");
        onCreate(sqLiteDatabase);
    }

    //QUERY TO SAVE DATA TO DATABASE DO NOT RETURN DATA
    public void queryToSaveDataToDatabase(String query) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(query);
        database.close();
    }


    public void queryToUpdateDataToDatabase(TimeElement timeElement) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_HOURS, timeElement.getHour());
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_MINUTES, timeElement.getMinute());
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_NOTES, timeElement.getNote());
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_REGULARS, timeElement.getRegular());
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_TIME_COUNTDOWN, timeElement.getTimeCountdown());
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_STATE_ALARM, timeElement.getOnOrOff() ? 1 : 0);
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_STATE_VIBRATE, timeElement.getVibrate() ? 1 : 0);
        database.update(SaveDataToSQLite.TABLE_NAME, contentValues, SaveDataToSQLite.COLUMN_NAME_ID + "=?", new String[]{String.valueOf(timeElement.getIdAlarm())});
        database.close();
    }

    public void queryUpdateTimeRemain(String timeRemain, int id) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_TIME_COUNTDOWN, timeRemain);
        database.update(SaveDataToSQLite.TABLE_NAME, contentValues, SaveDataToSQLite.COLUMN_NAME_ID + "=?", new String[]{String.valueOf(id)});
    }

    //QUERY TO GET DATA FROM DATABASE RETURN CURSOR
    public Cursor queryToGetDataReturn(String query) {
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(query, null);
    }


    public void deleteItemsInDatabase(List<String> list) {

        SQLiteDatabase database = getWritableDatabase();
        for (String item : list) {
            database.delete(SaveDataToSQLite.TABLE_NAME, SaveDataToSQLite.COLUMN_NAME_ID + "=?", new String[]{item});
            database.delete(SaveDataToSQLite.TABLE_NAME_PENDING_INTENT, SaveDataToSQLite.COLUMN_NAME_REQUEST_CODE + "=?", new String[]{item});
        }
        database.close();
    }

    public void insertItemInDatabase(ContentValues contentValues) {
        SQLiteDatabase database = getWritableDatabase();
        database.insert(SaveDataToSQLite.TABLE_NAME, null, contentValues);
        database.close();
    }

    public void saveDataToTablePendingIntent(int request_code, byte[] bytes) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_REQUEST_CODE, request_code);
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_ID_BYTE_ARRAY_PENDING_INTENT, bytes);
        database.insert(SaveDataToSQLite.TABLE_NAME_PENDING_INTENT, null, contentValues);
        database.close();
    }

    public void updateDataToTablePendingIntent(int request_code, byte[] bytes) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SaveDataToSQLite.COLUMN_NAME_ID_BYTE_ARRAY_PENDING_INTENT, bytes);
        database.update(SaveDataToSQLite.TABLE_NAME_PENDING_INTENT, contentValues, SaveDataToSQLite.COLUMN_NAME_REQUEST_CODE + "=?", new String[]{String.valueOf(request_code)});
        database.close();
    }

}
