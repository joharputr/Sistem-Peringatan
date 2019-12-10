package com.example.systemperingatan.API

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class dataNotifCoba(

        @field:SerializedName("nama")
        val nama: String? = null

): Parcelable