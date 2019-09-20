package com.example.systemperingatan.SQLite

import android.database.Cursor
import android.util.Log

object GeofenceStorage {

    private val TAG = "GeofenceStorage"

    val cursor: Cursor
        get() {
            val columns = arrayOf(GeofenceContract.GeofenceEntry._ID, GeofenceContract.GeofenceEntry.COLUMN_NAME_KEY, GeofenceContract.GeofenceEntry.COLUMN_NAME_LNG, GeofenceContract.GeofenceEntry.COLUMN_NAME_LAT, GeofenceContract.GeofenceEntry.COLUMN_NAME_EXPIRES)
            return GeofenceDbHelper.get().readableDatabase.query(GeofenceContract.GeofenceEntry.TABLE_NAME, columns, null, null, null, null, GeofenceContract.GeofenceEntry._ID + " DESC")
        }

    fun removeGeofence(requestId: String) {
        val where = GeofenceContract.GeofenceEntry.COLUMN_NAME_KEY + " = '" + requestId + "'"
        val result = GeofenceDbHelper.get().readableDatabase.delete(GeofenceContract.GeofenceEntry.TABLE_NAME, where, null)
        Log.i(TAG, "removeGeofence requestId=$requestId, number of deleted rows=$result")
    }


}
