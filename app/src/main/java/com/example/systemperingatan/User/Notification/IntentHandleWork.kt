package com.example.systemperingatan.User.Notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.BuildConfig
import com.example.systemperingatan.R
import com.example.systemperingatan.User.UI.UserActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.util.*
private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

class IntentHandleWork : JobIntentService() {
    override fun onHandleWork(intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.errorCode)
            sendNotification(3, errorMessage,"")
            Log.e("ERRORGEOFENCE", errorMessage)
            return
        }
        val geoFenceTransition = geofencingEvent.geofenceTransition

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            val data = getFirstReminder(geofencingEvent.triggeringGeofences)
            val message = data?.message
            val minim_distance = data?.minim_distance

            if (message != null) {
                sendNotification(1, message, minim_distance!!)
            }
        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val data = getFirstReminder(geofencingEvent.triggeringGeofences)
            val message = data?.message
            val minim_distance = data?.minim_distance

               if (message != null) {
                   sendNotification(2, message, minim_distance!!)
            }
        } else {
            Log.d("datanotif = ", "gagal")
        }
    }

    private fun getFirstReminder(triggeringGeofences: List<Geofence>): DataItem? {
        val firstGeofence = triggeringGeofences[0]
        return UserActivity.get(firstGeofence.requestId)
    }

    private fun sendNotification(id: Int, msg: String, minim_distance: String) {
        Log.i("notif", "sendNotification: $msg")
        val notificationIntent = Intent(applicationContext, UserActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(UserActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)

        val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificatioMng = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Geofence", importance)
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
                createNotification(id, msg, minim_distance, notificationPendingIntent))
    }

    private fun createNotification(id: Int, msg: String, minim_distance: String, notificationPendingIntent: PendingIntent): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val bigText: String
        if (id == 1) {
            bigText = "Anda Berada di " + msg + ", zona evakuasi terdekat adalah  " + minim_distance
        } else if(id == 2) {
            bigText = "Anda diluar $msg"
        }else{
            bigText = "Error = $msg"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    .setColor(Color.RED)
                    .setStyle(NotificationCompat.BigTextStyle()
                            .bigText(bigText))
                    .setContentText(bigText)
                    .setContentTitle("Geofence Notification!")
                    .setContentIntent(notificationPendingIntent)
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
        }


        return notificationBuilder.build()
    }

    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())

    companion object {
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                    context,
                    IntentHandleWork::class.java, 500,
                    intent)
        }
    }
}