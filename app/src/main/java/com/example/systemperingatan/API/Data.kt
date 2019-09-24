package com.example.systemperingatan.API

import com.google.gson.annotations.SerializedName

data class Data(
        @SerializedName("kode")
        var kode: Int? = null,
        @SerializedName("result")
        var result: ArrayList<Result>? = null

) {
    fun dataResult(): ArrayList<Result>? {
        return result
    }
}

