package com.example.systemperingatan.User.SQLite

import android.provider.BaseColumns

object GeofenceContract {


    class GeofenceEntry : BaseColumns {
        companion object {
            val _ID: String = "id"
            val TABLE_NAME = "geofences"
            val COLUMN_NAME_NUMBERS = "numbers"
            val COLUMN_NAME_LAT = "latitude"
            val COLUMN_NAME_LNG = "longitude"
            val COLUMN_NAME_EXPIRES = "expires"
            val COLUMN_NAME_MESSAGE = "messages"
            val COLUMN_NAME_DISTANCE = "distances"
            val COLUMN_NAME_TYPE = "type"
            val COLUMN_NAME_LEVEL = "level"
            val COLUMN_NAME_RADIUS = "radius"
            val COLUMN_NAME_MIN_DISTANCE = "minim_distance"
            val ID_COLUMN_NAME_MIN_DISTANCE = "id_minim_distance"

        }
    }
}
