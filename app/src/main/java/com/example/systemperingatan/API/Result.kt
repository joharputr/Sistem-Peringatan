package com.example.systemperingatan.API

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Result (

    @SerializedName("id")
    @Expose
    var id: String? = null,
    @SerializedName("numbers")
    @Expose
    var numbers: String? = null,
    @SerializedName("latitude")
    @Expose
    var latitude: String? = null,
    @SerializedName("longitude")
    @Expose
    var longitude: String? = null,
    @SerializedName("expires")
    @Expose
    var expires: String? = null,
    @SerializedName("message")
    @Expose
    var message: String? = null

)