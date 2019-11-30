package com.example.systemperingatan.API

import com.example.systemperingatan.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object NetworkAPI {
    val url = "192.168.1.14"
    var post = "http://$url/ci-restserver/Api/tambah"
    var RegisterUser = "http://$url/ci-restserver/Api/register"
    var LoginUser = "http://$url/ci-restserver/Api/login"
    var logout = "http://$url/ci-restserver/Api/logout"
    var edit = "http://$url/ci-restserver/Api/edit/"
    var editUser = "http://$url/ci-restserver/Api/edituser/"
    var delete = "http://$url/ci-restserver/Api/hapus/"
    var postDataEnter = "http://$url/ci-restserver/Api/postDataEnter"
    var postDataExit = "http://$url/ci-restserver/Api/postDataExit"
    var postDataAman = "http://$url/ci-restserver/Api/postDataAman"
    var edit_notif_masuk = "http://$url/ci-restserver/Api/edit_notif_masuk"
    var edit_notif_keluar = "http://$url/ci-restserver/Api/edit_notif_keluar"
    var edit_notif_zona = "http://$url/ci-restserver/Api/edit_notif_zona"
    var edit_zona_di_notif_masuk = "http://$url/ci-restserver/Api/edit_zona_di_notif_masuk"
    private val base_url = "http://$url/ci-restserver/Api/"

    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
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
        return HttpLoggingInterceptor().apply {
            //pake run eror why?
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
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