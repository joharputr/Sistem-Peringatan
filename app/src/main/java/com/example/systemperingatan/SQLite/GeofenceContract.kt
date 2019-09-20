package com.example.systemperingatan.SQLite

import android.provider.BaseColumns

object GeofenceContract {


    class GeofenceEntry : BaseColumns {
        companion object {
            val _ID: String = "id"
            val TABLE_NAME = "geofences"
            val COLUMN_NAME_KEY = "keys"
            val COLUMN_NAME_LAT = "lat"
            val COLUMN_NAME_LNG = "lng"
            val COLUMN_NAME_EXPIRES = "expires"

        }
    }
}
