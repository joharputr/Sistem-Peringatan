package com.example.systemperingatan

import android.app.Application
import android.content.Context
import android.text.TextUtils

import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

import com.android.volley.VolleyLog.TAG

class App : Application() {
    private var mRequestQueue: RequestQueue? = null

    val requestQueue: RequestQueue
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(applicationContext)
            }

            return mRequestQueue!!
        }


    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun <T> addToRequestQueue(req: Request<T>, tag: String) {
        req.setShouldCache(false)
        req.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        req.retryPolicy = DefaultRetryPolicy(15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        requestQueue.add(req)
    }

    companion object {
        var instance: App? = null
            private set
    }

}