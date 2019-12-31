package com.example.systemperingatan.API

import com.example.systemperingatan.API.Pojo.DataExitEnter.ResponseDataUser
import com.example.systemperingatan.API.Pojo.Response
import com.example.systemperingatan.User.Helper.GoogleMapDTO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

    @GET("lihat")
    @Headers("Content-Type: application/json")
    fun allData(): Call<Response>

    @GET("lihatDataEnter")
    @Headers("Content-Type: application/json")
    fun dataEnter(): Call<ResponseDataUser>

    @GET("lihatDataEnter_jarak")
    @Headers("Content-Type: application/json")
    fun dataEnter_jarak(): Call<ResponseDataUser>

    @GET("lihatDataExit")
    @Headers("Content-Type: application/json")
    fun dataExit(): Call<ResponseDataUser>

    @GET("lihatDataAman")
    @Headers("Content-Type: application/json")
    fun dataAman(): Call<ResponseDataUser>

    @GET("search_aman")
    fun getSearch_aman(
            @Query(
                    "q"
            ) querySearch: String?
    ): Call<ResponseDataUser>


    @GET("search_enter")
    fun getSearch_enter(
            @Query(
                    "q"
            ) querySearch: String?
    ): Call<ResponseDataUser>


    @GET("search_exit")
    fun getSearch_exit(
            @Query(
                    "q"
            ) querySearch: String?
    ): Call<ResponseDataUser>

    @GET("https://maps.googleapis.com/maps/api/directions/json")
    fun get_route(
            @Query(
                    "origin"
            ) origin: String?,  @Query(
                    "destination"
            ) destination: String?, @Query(
                    "sensor"
            ) sensor: String?, @Query(
                    "mode"
            ) mode: String?, @Query(
                    "key"
            ) key: String?
    ): Call<com.example.systemperingatan.API.Pojo.Route.Response>
}
