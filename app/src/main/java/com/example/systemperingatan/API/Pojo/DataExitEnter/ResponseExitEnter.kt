package com.example.systemperingatan.API.Pojo.DataExitEnter

import com.google.gson.annotations.SerializedName


data class ResponseExitEnter(

        @field:SerializedName("data")
        val data: List<DataItemExitEnter?>? = null,

        @field:SerializedName("message")
        val message: String? = null,

        @field:SerializedName("status")
        val status: Int? = null
)