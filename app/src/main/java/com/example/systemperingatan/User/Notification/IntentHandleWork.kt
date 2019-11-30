package com.example.systemperingatan.User.Notification

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.App
import com.example.systemperingatan.BuildConfig
import com.example.systemperingatan.R
import com.example.systemperingatan.User.UI.UserActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

class IntentHandleWork : JobIntentService() {
    @SuppressLint("SimpleDateFormat")
    override fun onHandleWork(intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.errorCode)
            sendNotification(4, errorMessage, "")
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
                postDataEnterToServer(id.toString(), id_minim_distance.toString(), message, minim_distance, currentDate)
                sendNotification(1, message, minim_distance)
            } else if (data?.type == "circle" && message != null && minim_distance == null) {
                sendNotification(5, message, "")

            } else if (data?.type == "point" && message != null) {
                postDataAman(id.toString(), message, currentDate)
                sendNotification(4, message, "")
            }

        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            val data = getFirstReminder(geofencingEvent.triggeringGeofences)
            if (data?.type == "circle") {
                val sdf = SimpleDateFormat("dd/M/yyyy HH:mm:ss")
                val currentDate = sdf.format(Date())
                val message = data.message
                val minim_distance = data.minim_distance
                postDataExitToServer(data.number.toString(), data.message, currentDate)
                Log.d("namaEXIT = ", message + " minim = " + minim_distance)
                if (message != null) {
                    sendNotification(2, message, minim_distance!!)
                }
            }

        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            val data = getFirstReminder(geofencingEvent.triggeringGeofences)
            val message = data?.message
            val minim_distance = data?.minim_distance

            if (message != null)
                sendNotification(3, message, minim_distance!!)

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
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Geofence", importance)
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mChannel.setShowBadge(false)
            notificatioMng.createNotificationChannel(mChannel)
        } else {
            Log.d("CLOG", "dibawah 0")
        }

        val random = Random()
        val randomInt = random.nextInt(9999 - 1000) + 1000

        notificatioMng.notify(
                randomInt,
                createNotification(id, msg, minim_distance, notificationPendingIntent))

        startForeground(id, createNotification(id, msg, minim_distance, notificationPendingIntent))
    }

    private fun createNotification(id: Int, msg: String, minim_distance: String, notificationPendingIntent: PendingIntent): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
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
                .setContentTitle("Notifikasi Sistem Peringatan")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
        /*.setPriority(NotificationManager.IMPORTANCE_HIGH)*/



        return notificationBuilder.build()
    }

    private fun postDataEnterToServer(id: String, id_min_dis: String, name: String?, zone: String?, date: String) {

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
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
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

    private fun postDataExitToServer(number: String, name: String?, waktu: String?) {

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
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
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

    private fun postDataAman(id: String, name: String?, date: String) {

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
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
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

    companion object {
        private const val JOB_ID = 573
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                    context,
                    IntentHandleWork::class.java, JOB_ID,
                    intent)
        }
    }


    //redmi 6
    // redmi 4
    //rrealme busuk
    //redmi note 4
}