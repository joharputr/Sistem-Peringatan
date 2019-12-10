package com.example.systemperingatan.User.Notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.App
import com.example.systemperingatan.BuildConfig
import com.example.systemperingatan.R
import com.example.systemperingatan.User.UI.UserActivity
import com.google.android.gms.location.Geofence
import org.json.JSONException
import org.json.JSONObject
import java.util.*


//const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"
fun getFirstReminder(triggeringGeofences: List<Geofence>): DataItem? {
    val firstGeofence = triggeringGeofences[0]
    return UserActivity.get(firstGeofence.requestId)
}

fun postDataEnterToServer(context: Context, id: String, id_min_dis: String, name: String?, zone: String?, date: String) {

    val tag_string_req = "req_postdata"
    val strReq = object : StringRequest(Method.POST,
            NetworkAPI.postDataEnter, { response ->
        Log.d("CLOG", "responh: $response")
        try {

            val jObj = JSONObject(response)
            val status1 = jObj.getString("status")
            Log.d("statuspost  = ", status1)
            if (status1.contains("200")) {
                Log.d("sukses data =", jObj.toString())
            } else {
                val msg = jObj.getString("message")
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            Log.d("error catch = ", e.toString())
        }

    }, { error ->
        Log.d("CLOG", "verespon: ${error.localizedMessage}")
        val json: String?
        val response = error.networkResponse
        if (response != null && response.data != null) {
            json = String(response.data)
            val jObj: JSONObject?
            try {
                jObj = JSONObject(json)
                val msg = jObj.getString("message")
                Toast.makeText(context, error.localizedMessage, Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }) {
        override fun getParams(): Map<String, String> {
            val params = HashMap<String, String>()
            if (App.preferenceHelper.tipe == "admin") {
                params["phone"] = "Admin"
            } else {
                params["phone"] = App.preferenceHelper.phonefb
            }
            params["area"] = name.toString()
            params["waktu"] = date
            params["nama_zona_terdekat"] = zone.toString()
            params["id_area_masuk"] = id
            params["id_zona_terdekat"] = id_min_dis
            return params
        }
    }

    // Adding request to request queue
    App.instance?.addToRequestQueue(strReq, tag_string_req)

}

fun postDataExitToServer(context: Context, number: String, name: String?, waktu: String?) {

    val tag_string_req = "req_postdata"
    val strReq = object : StringRequest(Method.POST,
            NetworkAPI.postDataExit, { response ->
        Log.d("CLOG", "responh: $response")
        try {

            val jObj = JSONObject(response)
            val status1 = jObj.getString("status")
            Log.d("statuspost  = ", status1)
            if (status1.contains("200")) {
                Log.d("Success", "post data exit")
            } else {
                val msg = jObj.getString("message")
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            Log.d("error catch = ", e.toString())
        }

    }, { error ->
        Log.d("CLOG", "verespon: ${error.localizedMessage}")
        val json: String?
        val response = error.networkResponse
        if (response != null && response.data != null) {
            json = String(response.data)
            val jObj: JSONObject?
            try {
                jObj = JSONObject(json)
                val msg = jObj.getString("message")
                Toast.makeText(context, error.localizedMessage, Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }) {
        override fun getParams(): Map<String, String> {
            val params = HashMap<String, String>()
            if (App.preferenceHelper.tipe == "admin") {
                params["phone"] = "Admin"
            } else {
                params["phone"] = App.preferenceHelper.phonefb
            }
            params["area"] = name.toString()
            params["waktu"] = waktu.toString()
            params["id_area_keluar"] = number.toString()

            return params
        }
    }

    // Adding request to request queue
    App.instance?.addToRequestQueue(strReq, tag_string_req)

}

fun postDataAman(context: Context, id: String, name: String?, date: String) {

    val tag_string_req = "req_postdata"
    val strReq = object : StringRequest(Method.POST,
            NetworkAPI.postDataAman, { response ->
        Log.d("CLOG", "responh: $response")
        try {

            val jObj = JSONObject(response)
            val status1 = jObj.getString("status")
            Log.d("statuspost  = ", status1)
            if (status1.contains("200")) {
            } else {
                val msg = jObj.getString("message")
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            Log.d("error catch = ", e.toString())
        }

    }, { error ->
        Log.d("CLOG", "verespon: ${error.localizedMessage}")
        val json: String?
        val response = error.networkResponse
        if (response != null && response.data != null) {
            json = String(response.data)
            val jObj: JSONObject?
            try {
                jObj = JSONObject(json)
                val msg = jObj.getString("message")
                Toast.makeText(context, error.localizedMessage, Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }) {
        override fun getParams(): Map<String, String> {
            val params = HashMap<String, String>()
            if (App.preferenceHelper.tipe == "admin") {
                params["phone"] = "Admin"
            } else {
                params["phone"] = App.preferenceHelper.phonefb
            }
            params["nama_zona"] = name.toString()
            params["waktu"] = date
            params["id_zona"] = id

            return params
        }
    }

    // Adding request to request queue
    App.instance?.addToRequestQueue(strReq, tag_string_req)

}

fun sendNotification(applicationContext: Context, id: Int, msg: String, minim_distance: String) {
    Log.i("notif", "sendNotification: $msg")
    val notificationIntent = Intent(applicationContext, UserActivity::class.java)
    val stackBuilder = TaskStackBuilder.create(applicationContext)
    stackBuilder.addParentStack(UserActivity::class.java)
    stackBuilder.addNextIntent(notificationIntent)

    val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    val notificatioMng = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Geofence", importance)
        mChannel.enableLights(true)
        mChannel.lightColor = Color.RED
        mChannel.enableVibration(true)
        mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        mChannel.setShowBadge(true)
        notificatioMng.createNotificationChannel(mChannel)
    } else {
        Log.d("CLOG", "dibawah 0")
    }

    val random = Random()
    val randomInt = random.nextInt(9999 - 1000) + 1000

    notificatioMng.notify(
            randomInt,
            createNotification(applicationContext, id, msg, minim_distance, notificationPendingIntent))

    //startForeground(id, createNotification(id, msg, minim_distance, notificationPendingIntent))

}

private fun createNotification(context: Context, id: Int, msg: String, minim_distance: String, notificationPendingIntent: PendingIntent): Notification {
    val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
    val bigText: String
    if (id == 1) {
        bigText = "Anda Berada di " + msg + ", zona evakuasi terdekat adalah  " + minim_distance
    } else if (id == 2) {
        bigText = "Anda diluar $msg"
    } else if (id == 3) {
        bigText = "Anda sudah terlalu lama di $msg zona evakuasi terdekat adalah $minim_distance"
    } else if (id == 4) {
        bigText = "Anda berada sekitar 100 meter di zona evakuasi  $msg"
    } else if (id == 5) {
        bigText = "Anda berada di area  $msg"
    } else {
        bigText = "Error = $msg"
    }

    notificationBuilder.setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
            .setColor(Color.RED)
            .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(bigText))
            .setContentText(bigText)
            /*.setAutoCancel(true)*/
            .setContentTitle("Notifikasi Sistem Peringatan")
            .setContentIntent(notificationPendingIntent)
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
    /*.setPriority(NotificationManager.IMPORTANCE_HIGH)*/



    return notificationBuilder.build()
}


