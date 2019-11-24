package com.example.systemperingatan.API

import com.example.systemperingatan.API.Pojo.DataExitEnter.ResponseDataUser
import com.example.systemperingatan.API.Pojo.Response
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

    @GET("lihatDataExit")
    @Headers("Content-Type: application/json")
    fun dataExit(): Call<ResponseDataUser>

    @GET("lihatDataAman")
    @Headers("Content-Type: application/json")
    fun dataAman(): Call<ResponseDataUser>

    @GET("search")
    fun getSearch(
            @Query(
                    "q"
            ) querySearch: String?
    ): Call<ResponseDataUser>
}
