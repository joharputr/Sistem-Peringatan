package com.example.systemperingatan.API

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

    @DELETE("hapus/{number}")
    fun deleteData(@Path("number") number: String): Call<Response>

    @POST("tambah")
    @Headers("Content-Type: application/json")
    fun addData(@Query("numbers") numbers: String,
                @Query("latitude") latitiude: String,
                @Query("longitude") longitude: String,
                @Query("expires") expires: String): Call<Response>

    @PUT("kontak/")
    @Headers("Content-Type: application/json")
    fun updateItem(@Path("id") id: Int, @Body data: Map<String, String>): Call<Response>
}
