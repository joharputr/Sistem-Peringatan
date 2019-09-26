package com.example.systemperingatan.API

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkConfig {
    private val base_url = "http://192.168.1.20/api/hakiki95tutorial/"
    var post = "http://192.168.1.20/ci-restserver/Api/tambah"

    private var retrofit: Retrofit? = null

    val client: Retrofit
        get() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build()

            val gson = GsonBuilder()
                    .setLenient()
                    .create()

            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                        .baseUrl(base_url)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build()
            }

            return retrofit!!
        }
}
