package com.example.systemperingatan.API.Pojo.DataExitEnter

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DataUser(

        @field:SerializedName("phone")
        val phone: String? = null,

        @field:SerializedName("waktu")
        val waktu: String? = null,

        @field:SerializedName("area")
        val namaArea: String? = null,

        @field:SerializedName("id")
        val id: String? = null,

        @field:SerializedName("nama_zona")
        val nama_zona: String? = null,

        @field:SerializedName("nama_zona_terdekat")
        val namaZonaTerdekat: String? = null
):Parcelable