package com.example.systemperingatan.API.Pojo.Login

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PojoLogin(

        @field:SerializedName("password")
        val password: String? = null,

        @field:SerializedName("nama")
        val nama: String? = null,

        @field:SerializedName("hp")
        val hp: String? = null,

        @field:SerializedName("id")
        val id: String? = null,

        @field:SerializedName("tipe")
        val tipe: String? = null,

        @field:SerializedName("is_login")
        val isLogin: String? = null
) : Parcelable