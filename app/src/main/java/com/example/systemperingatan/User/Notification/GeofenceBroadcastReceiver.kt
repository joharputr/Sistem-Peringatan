package com.example.systemperingatan.User.Notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import com.example.systemperingatan.App
import com.example.systemperingatan.User.UI.UserActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.text.SimpleDateFormat
import java.util.*

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
       IntentHandleWork.enqueueWork(context, intent)

      /*  val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(context,
                    geofencingEvent.errorCode)
            sendNotification(context,4, errorMessage, "")
            Log.e("ERRORGEOFENCE", errorMessage)
        }

        val geoFenceTransition = geofencingEvent.geofenceTransition

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            val data = getFirstReminder(geofencingEvent.triggeringGeofences)
            val id = data?.number
            val message = data?.message
            val minim_distance = data?.minim_distance
            val id_minim_distance = data?.id_minim_distance

            val sdf = SimpleDateFormat("dd/M/yyyy HH:mm:ss")
            val currentDate = sdf.format(Date())

            Log.d("testIdMasuk = ", id + " nama  =  " + data?.message + " dateis  = " + currentDate)
            Log.d("testtypepoint = ", id + " nama  =  " + data?.message + " point  = " + data?.type)

            if (data?.type == "circle" && message != null && minim_distance != null) {
                if (App.preferenceHelper.tipe != "admin"){
                    postDataEnterToServer(context, id.toString(), id_minim_distance.toString(), message, minim_distance, currentDate)
                }
                sendNotification(context,1, message, minim_distance)
            } else if (data?.type == "circle" && message != null && minim_distance == null) {
                sendNotification(context,5, message, "")

            } else if (data?.type == "point" && message != null) {
                if (App.preferenceHelper.tipe != "admin"){
                    postDataAman(context,id.toString(), message, currentDate)
                }
                sendNotification(context,4, message, "")
            }

        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            val data = getFirstReminder(geofencingEvent.triggeringGeofences)
            if (data?.type == "circle") {
                val sdf = SimpleDateFormat("dd/M/yyyy HH:mm:ss")
                val currentDate = sdf.format(Date())
                val message = data.message
                val minim_distance = data.minim_distance

                if(App.preferenceHelper.tipe != "admin"){
                    postDataExitToServer(context,data.number.toString(), data.message, currentDate)
                }

                Log.d("namaEXIT = ", message + " minim = " + minim_distance)
                if (message != null) {
                    sendNotification(context,2, message, minim_distance!!)
                }
            }

        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            val data = getFirstReminder(geofencingEvent.triggeringGeofences)
            val message = data?.message
            val minim_distance = data?.minim_distance

            if (message != null)
                sendNotification(context,3, message, minim_distance!!)

        } else {
            Log.d("datanotif = ", "gagal")
        }*/
    }

}