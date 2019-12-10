package com.example.systemperingatan.Admin.UI.Settings

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.systemperingatan.API.Pojo.DataExitEnter.DataUser
import com.example.systemperingatan.R
import java.util.*
import kotlin.collections.ArrayList

class NotifikasiDataUser : BroadcastReceiver() {

    companion object {

        private var notifId: Int = 0

    }

    override fun onReceive(context: Context, intent: Intent) {

        notifId = intent.getIntExtra("id", 0)
        val nama = intent.getParcelableArrayListExtra<DataUser>("nama")
        Log.d("3TESNAMARECEIVE = ", nama.toString())
        Log.d("1TESNAMARECEIVESIZE = ",  nama.size.toString())
        for (i in 0 until nama.size) {
            Log.d("2TESNAMARECEIVENAMA =", nama[i].namaArea)
         //   showAlarmNotification(context, nama[i].namaArea!!, notifId)
        }

    }

    private fun showAlarmNotification(context: Context, title: String, notifId: Int) {
        val CHANNEL_ID = "Channel_1"
        val CHANNEL_NAME = "Job scheduler channel"
        val notificationManagerCompat =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Log.d("alarm", "showAlarmNotification: $notifId")

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("masuk $title ")
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            builder.setChannelId(CHANNEL_ID)

            notificationManagerCompat.createNotificationChannel(channel)
        }
        val notification = builder.build()

        val random = Random()
        val randomInt = random.nextInt(9999 - 1000) + 1000
        notificationManagerCompat.notify(randomInt, notification)
    }

    val list = ArrayList<DataUser>()
    val nama = ArrayList<String>()
    fun setRepeatingAlarm(context: Context, dataUser: DataUser) {

        list.addAll(listOf(dataUser))
        Log.d("TESNAMA =", list.toString())

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotifikasiDataUser::class.java)

        intent.putParcelableArrayListExtra("nama", list)
        intent.putExtra("id", notifId)
        intent.putExtra("hp", nama)
        context.sendBroadcast(intent)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)
        //    val pendingIntent = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        //   alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent)
    }


}
