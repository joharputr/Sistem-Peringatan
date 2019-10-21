package com.example.systemperingatan.User.Notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.User.UI.UserActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.*

class IntentHandleWork : JobIntentService() {
    internal var CHANNEL_ID = "my_channel_02"
    internal var name: CharSequence = "my_channel_2"
    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        // Handling errors
        if (geofencingEvent.hasError()) {
            val errorMsg = GeofenceTransitionService.getErrorString(geofencingEvent.errorCode)
            Log.d("datanotif = ", "error" + errorMsg)
            Log.e("CLOGerror", errorMsg)
            return
        }
        val geoFenceTransition = geofencingEvent.geofenceTransition

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            //    val triggeringGeofences = geofencingEvent.triggeringGeofences
            //   val geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences)
            //  sendNotification(geofenceTransitionDetails)

            Log.d("datanotif = ", "masuk ")
            val data = getFirstReminder(geofencingEvent.triggeringGeofences)
            val message = data?.message
            val minim_distance = data?.minim_distance

            Log.d("datanotif = ", data.toString())
            if (message != null) {
                Log.d("datanotif = ", "message = " + message + " minim = " + minim_distance)
                sendNotification(1,message, minim_distance!!)
            }
        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d("datanotifKeluar = ", "masuk ")
            val data = getFirstReminder(geofencingEvent.triggeringGeofences)
            val message = data?.message
            val minim_distance = data?.minim_distance

            Log.d("datanotifKeluar = ", data.toString())
            if (message != null) {
                Log.d("datanotifKeluar = ", "message = " + message + " minim = " + minim_distance)
                sendNotification(2 , message, minim_distance!!)
            }
        } else {
            Log.d("datanotif = ", "gagal")
        }
    }

    private fun getFirstReminder(triggeringGeofences: List<Geofence>): DataItem? {
        val firstGeofence = triggeringGeofences[0]
        return UserActivity.get(firstGeofence.requestId)
    }

    private fun sendNotification(id :Int,msg: String, minim_distance: String) {
        Log.i("notif", "sendNotification: $msg")

        // Intent to start the main Activity
        val notificationIntent = Intent(applicationContext, UserActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(UserActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Creating and sending Notification
        val notificatioMng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mChannel.setShowBadge(false)
            notificatioMng.createNotificationChannel(mChannel)
        }

        val random = Random()
        val randomInt = random.nextInt(9999 - 1000) + 1000

        notificatioMng.notify(
                randomInt,
                createNotification(id,msg, minim_distance, notificationPendingIntent))
    }

    private fun createNotification(id:Int,msg: String, minim_distance: String, notificationPendingIntent: PendingIntent): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        val bigText: String
        if (id == 1){
             bigText = "Anda Berada di " + msg + ", zona evakuasi terdekat adalah  " + minim_distance
        }
        else{
            bigText = "Anda diluar $msg"
        }
        notificationBuilder
                .setSmallIcon(com.example.systemperingatan.R.drawable.common_google_signin_btn_icon_dark)
                .setColor(Color.RED)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(bigText))
                .setContentText(bigText)
                .setContentTitle("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
        return notificationBuilder.build()
    }

    companion object {
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                    context,
                    IntentHandleWork::class.java, 11,
                    intent)
        }
    }
}