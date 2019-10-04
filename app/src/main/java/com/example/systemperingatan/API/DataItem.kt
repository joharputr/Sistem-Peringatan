package com.example.systemperingatan.API


import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class DataItem(

        @field:SerializedName("number")
        val number: String? = null,

        @field:SerializedName("latlang")
        var latlang: LatLng? = null,

        @field:SerializedName("expires")
        val expires: String? = null,

        @field:SerializedName("latitude")
        var latitude: String? = null,

        @field:SerializedName("id")
        val id: String? = null,

        @field:SerializedName("message")
        var message: String? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("longitude")
        var longitude: String? = null,

        @field:SerializedName("radius")
        var radius: String? = null,

        @field:SerializedName("distance")
        var distance: String? = null,

        @field:SerializedName("minim_distance")
        var minim_distance: String? = null

)