package com.example.systemperingatan.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.systemperingatan.App;

public class GeofenceDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "geologi.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + GeofenceContract.GeofenceEntry.TABLE_NAME +
                    "(_ID INTEGER PRIMARY KEY AUTOINCREMENT, keys TEXT,lat TEXT,lng TEXT,expires TEXT)";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + GeofenceContract.GeofenceEntry.TABLE_NAME;

    private GeofenceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static GeofenceDbHelper get() {
        return new GeofenceDbHelper(App.getInstance());
    }

    public static void saveToDb(String key, double latitude, double longitude, long expires) {
        GeofenceDbHelper helper = GeofenceDbHelper.get();

        //save to db
        ContentValues values = new ContentValues();
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_KEY, key);
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_EXPIRES, expires + "");
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_LAT, latitude + "");
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_LNG, longitude + "");
        helper.getWritableDatabase().insert(GeofenceContract.GeofenceEntry.TABLE_NAME, null, values);
        Log.i("", "Row inserted id=" + helper + ", ContentValues=" + values);

    }

}