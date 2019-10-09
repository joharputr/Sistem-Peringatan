package com.example.systemperingatan.Notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        IntentHandleWork.enqueueWork(context, intent)
    }
}