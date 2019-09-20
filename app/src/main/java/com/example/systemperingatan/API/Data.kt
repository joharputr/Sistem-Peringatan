package com.example.systemperingatan.API

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {
    @SerializedName("kode")
    @Expose
    var kode: Int? = null
    @SerializedName("result")
    @Expose
    var result: List<Result>? = null


}
