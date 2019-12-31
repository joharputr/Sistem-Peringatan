package com.example.systemperingatan.User.SQLite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.example.systemperingatan.App

class GeofenceDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {
        val query = "CREATE TABLE Geofences(numbers INTEGER PRIMARY KEY AUTOINCREMENT,latitude TEXT,longitude TEXT,expires TEXT,messages TEXT,distances TEXT,type TEXT,level TEXT, radius TEXT)"
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val delete = "DROP TABLE IF EXISTS Geofences"
        db.execSQL(delete)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    //jika memperbarui tabel atau kolom diharapkan untuk menambah database_version
    companion object {

        private val DATABASE_VERSION = 30
        private val DATABASE_NAME = "Geofences.db"

        fun get(): GeofenceDbHelper {
            return GeofenceDbHelper(App.instance!!)
        }
    }

    fun saveToDb(numbers: String, latitude: Double, longitude: Double, expires: Long, message: String?, distance: Double, type: String?, level: String?, radius : String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_NUMBERS, numbers)
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_LAT, latitude.toString() + "")
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_LNG, longitude.toString() + "")
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_EXPIRES, expires.toString() + "")
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_MESSAGE, message + "")
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_DISTANCE, distance.toString() + "")
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_TYPE, type + "")
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_LEVEL, level + "")
        values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_RADIUS, radius + "")
        db.insert("Geofences", null, values)
        Log.d("CLOGsqlite", "data = " + values)

        db.close()
        Log.v("cekSQL ", " Record Inserted Sucessfully")
    }

    fun getCursor(): Cursor {

        val db = readableDatabase
        val MY_QUERY = "SELECT *, ( SELECT b.messages FROM Geofences b WHERE type = 'point' ORDER BY type desc, distances + 0 ASC LIMIT 1 ) AS 'minim_distance',( SELECT b.numbers FROM Geofences b WHERE type = 'point' ORDER BY type desc, distances + 0 ASC LIMIT 1 ) AS 'id_minim_distance' FROM Geofences a"

        /*  return db.query(
                  "Geofences",
                  columns,
                  null,
                  null,
                  null,
                  null,
                  GeofenceContract.GeofenceEntry.COLUMN_NAME_DISTANCE+ " ASC"
          )*/

        return db.rawQuery(
                MY_QUERY,
                null
        )
    }


    fun DeleteAll() {
        val db = readableDatabase
        db.execSQL("delete from Geofences");
    }
}