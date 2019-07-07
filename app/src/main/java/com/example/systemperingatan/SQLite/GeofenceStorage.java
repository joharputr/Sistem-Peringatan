package com.example.systemperingatan.SQLite;

import android.database.Cursor;
import android.util.Log;

public class GeofenceStorage {

    private static final String TAG = "GeofenceStorage";

    public static Cursor getCursor() {
        String[] columns = new String[]{GeofenceContract.GeofenceEntry._ID, GeofenceContract.GeofenceEntry.COLUMN_NAME_KEY, GeofenceContract.GeofenceEntry.COLUMN_NAME_LNG, GeofenceContract.GeofenceEntry.COLUMN_NAME_LAT, GeofenceContract.GeofenceEntry.COLUMN_NAME_EXPIRES};
        return GeofenceDbHelper.get().getReadableDatabase().query(GeofenceContract.GeofenceEntry.TABLE_NAME, columns, null, null, null, null, GeofenceContract.GeofenceEntry._ID + " DESC");
    }

    public static void removeGeofence(String requestId) {
        String where = GeofenceContract.GeofenceEntry.COLUMN_NAME_KEY + " = '" + requestId + "'";
        int result = GeofenceDbHelper.get().getReadableDatabase().delete(GeofenceContract.GeofenceEntry.TABLE_NAME, where, null);
        Log.i(TAG, "removeGeofence requestId=" + requestId + ", number of deleted rows=" + result);
    }


}
