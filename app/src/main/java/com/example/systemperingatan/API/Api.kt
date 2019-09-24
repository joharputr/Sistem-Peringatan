package com.example.systemperingatan.API

import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

    @GET("read.php")
    @Headers("Content-Type: application/json")
    fun allData(): Call<Data>

    @DELETE("kontak/{id}")
    @Headers("Content-Type: application/json")
    fun deleteItem(@Path("id") itemId: Int, callback: Callback<Data>): Void

    @POST("insert.php")
    @Headers("Content-Type: application/json")
    fun addData(@Query("numbers") numbers: String,
                @Query("latitude") latitiude: String,
                @Query("longitude") longitude: String,
                @Query("expires") expires: String): Call<Data>

    @PUT("kontak/")
    @Headers("Content-Type: application/json")
    fun updateItem(@Path("id") id: Int, @Body data: Map<String, String>): Call<Data>
}
