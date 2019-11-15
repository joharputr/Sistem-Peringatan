package com.example.systemperingatan.API.Pojo.DataExitEnter

import com.google.gson.annotations.SerializedName


data class DataItemExitEnter(

        @field:SerializedName("phone")
        val phone: String? = null,

        @field:SerializedName("waktu")
        val waktu: String? = null,

        @field:SerializedName("nama_area")
        val namaArea: String? = null,

        @field:SerializedName("id")
        val id: String? = null,

        @field:SerializedName("nama_zona_terdekat")
        val namaZonaTerdekat: String? = null
)