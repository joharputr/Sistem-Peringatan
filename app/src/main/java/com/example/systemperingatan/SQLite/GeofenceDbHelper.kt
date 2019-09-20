package com.example.systemperingatan.SQLite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.example.systemperingatan.App

class GeofenceDbHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {

        private val DATABASE_VERSION = 5
        private val DATABASE_NAME = "geologi.db"
        private val SQL_CREATE_ENTRIES = "CREATE TABLE " + GeofenceContract.GeofenceEntry.TABLE_NAME +
                "(_ID INTEGER PRIMARY KEY AUTOINCREMENT, keys TEXT,lat TEXT,lng TEXT,expires TEXT)"


        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + GeofenceContract.GeofenceEntry.TABLE_NAME

        fun get(): GeofenceDbHelper {
            return GeofenceDbHelper(App.instance!!)
        }

        fun saveToDb(key: String, latitude: Double, longitude: Double, expires: Long) {
            val helper = GeofenceDbHelper.get()

            //save to db
            val values = ContentValues()
            values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_KEY, key)
            values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_EXPIRES, expires.toString() + "")
            values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_LAT, latitude.toString() + "")
            values.put(GeofenceContract.GeofenceEntry.COLUMN_NAME_LNG, longitude.toString() + "")
            helper.writableDatabase.insert(GeofenceContract.GeofenceEntry.TABLE_NAME, null, values)
            Log.i("", "Row inserted id=$helper, ContentValues=$values")

        }
    }

}