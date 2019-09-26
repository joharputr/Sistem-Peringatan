package com.example.systemperingatan.API

import com.google.gson.annotations.SerializedName


data class Response(

        @field:SerializedName("data")
        val data: ArrayList<DataItem?>? = null,

        @field:SerializedName("message")
        val message: String? = null,

        @field:SerializedName("status")
        val status: Int? = null
)