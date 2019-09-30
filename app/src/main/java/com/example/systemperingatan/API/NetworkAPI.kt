package com.example.systemperingatan.API

import com.example.systemperingatan.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkAPI {
    var post = "http://192.168.1.20/ci-restserver/Api/tambah"
    var edit = "http://192.168.1.20/ci-restserver/Api/edit/"
    private val base_url = "http://192.168.1.20/ci-restserver/Api/"

    fun getRetrofit(): Retrofit {
        return  Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient()) //retrofit butuh client
                .build()
    }
    //client
    private fun getOkHttpClient(): OkHttpClient {
        val timeOut = 60L
        return OkHttpClient.Builder()
                .readTimeout(timeOut, TimeUnit.SECONDS)
                .connectTimeout(timeOut, TimeUnit.SECONDS)
                .writeTimeout(timeOut, TimeUnit.SECONDS)
                .addInterceptor(getInterseptor())  //client butuh interseptor
                .build()
    }

    //interseptor
    //muncul di logcat
    private fun getInterseptor(): Interceptor {
        return HttpLoggingInterceptor().apply {//pake run eror why?
            level = if (BuildConfig.DEBUG){
                HttpLoggingInterceptor.Level.BODY
            }else{
                HttpLoggingInterceptor.Level.NONE
            }
        }

        //style java
        /*val interseptor = HttpLoggingInterceptor()
        interseptor.level =  if (BuildConfig.DEBUG){
            HttpLoggingInterceptor.Level.BODY
        }else{
            HttpLoggingInterceptor.Level.NONE
        }*/
    }
}