package com.example.systemperingatan.API.Pojo


import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
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
        var id: String? = null,

        @field:SerializedName("message")
        var message: String? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("longitude")
        var longitude: String? = null,

        @field:SerializedName("radius")
        var radius: String? = null,

        @field:SerializedName("address")
        var address: String? = null,

        @field:SerializedName("distance")
        var distance: String? = null,

        @field:SerializedName("minim_distance")
        var minim_distance: String? = null,

        @field:SerializedName("id_minim_distance")
        var id_minim_distance: String? = null,

        @field:SerializedName("level")
        var level: String? = null,

        @field:SerializedName("no_hp")
        var no_hp: String? = null,

        @field:SerializedName("nama_p_jawab")
        var nama_p_jawab: String? = null

) : Parcelable