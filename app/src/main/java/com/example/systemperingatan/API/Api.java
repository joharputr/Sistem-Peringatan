package com.example.systemperingatan.API;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Api {

    @GET("read.php")
    @Headers("Content-Type: application/json")
    Call<Data> getAllData();

    @DELETE("kontak/{id}")
    @Headers("Content-Type: application/json")
    Void deleteItem(@Path("id") int itemId, Callback<Data> callback);

    @POST("insert.php")
    @Headers("Content-Type: application/json")
    Call<Data> addData(@Query("numbers") String numbers,
                       @Query("latitude") String latitiude,
                       @Query("longitude") String longitude,
                       @Query("expires") String expires);

    @PUT("kontak/")
    @Headers("Content-Type: application/json")
    Call<Data> updateItem(@Path("id") int id, @Body Map<String, String> data);
}
