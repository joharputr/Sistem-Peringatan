package com.example.systemperingatan

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.util.Log
import com.example.systemperingatan.API.Result
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import java.util.*

class GeofenceTransitionService : IntentService(TAG) {
    internal var CHANNEL_ID = "my_channel_01"
    internal var name: CharSequence = "my_channel"
    internal var Description = "This is my channel"

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        // Handling errors
        if (geofencingEvent.hasError()) {
            val errorMsg = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG, errorMsg)
            return
        }
        val geoFenceTransition = geofencingEvent.geofenceTransition

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences)
          //  sendNotification(geofenceTransitionDetails)

            val reminder = getFirstReminder(geofencingEvent.triggeringGeofences)
            val message = reminder?.message
            val latLng = reminder?.longitude
            Log.d("datareminder = ", reminder.toString())
            if (message != null) {
                sendNotification(message)
            }
        }
    }

    private fun getFirstReminder(triggeringGeofences: List<Geofence>): Result? {
        val firstGeofence = triggeringGeofences[0]
        return UserActivity.get(firstGeofence.requestId)
    }



    private fun getGeofenceTrasitionDetails(geoFenceTransition: Int, triggeringGeofences: List<Geofence>): String {
        // get the ID of each geofence triggered
        val triggeringGeofencesList = ArrayList<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesList.add(geofence.requestId)
        }

        var status: String? = null
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering "
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            status = "Exiting "

        return status!! + TextUtils.join(", ", triggeringGeofencesList)
    }

    private fun sendNotification(msg: String) {
        Log.i(TAG, "sendNotification: $msg")

        // Intent to start the main Activity
        val notificationIntent = Intent(applicationContext, MapsActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MapsActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Creating and sending Notification
        val notificatioMng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = msg
            mChannel.description = msg
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mChannel.setShowBadge(false)
            notificatioMng.createNotificationChannel(mChannel)
        }
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent))
    }

    // Create notification
    private fun createNotification(msg: String, notificationPendingIntent: PendingIntent): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder
                .setSmallIcon(R.drawable.cast_ic_notification_pause)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
        return notificationBuilder.build()
    }


    companion object {

        val GEOFENCE_NOTIFICATION_ID = 0
        private val TAG = GeofenceTransitionService::class.java.simpleName


        internal fun getErrorString(errorCode: Int): String {
            when (errorCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return "GeoFence not available"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return "Too many GeoFences"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return "Too many pending intents"
                else -> return "Unknown error."
            }
        }
    }
}