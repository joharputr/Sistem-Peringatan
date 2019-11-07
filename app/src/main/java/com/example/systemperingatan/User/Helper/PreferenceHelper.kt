package com.example.systemperingatan.User.Helper

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(app: Application) {
    private val sp: SharedPreferences by lazy {
        app.getSharedPreferences("binar_app", Context.MODE_PRIVATE)
    }


    private val spe: SharedPreferences.Editor by lazy {
        sp.edit()
    }


    var is_login: String
        set(value) = spe.putString("is_login", value).apply()
        get() = sp.getString("is_login", "") ?: ""

    var nama: String
        set(value) = spe.putString("nama", value).apply()
        get() = sp.getString("nama", "") ?: ""

    var tipe: String
        set(value) = spe.putString("tipe", value).apply()
        get() = sp.getString("tipe", "") ?: ""

    var hp: String
        set(value) = spe.putString("hp", value).apply()
        get() = sp.getString("hp", "") ?: ""

    var password: String
        set(value) = spe.putString("password", value).apply()
        get() = sp.getString("password", "") ?: ""
    fun logOut() {
        spe.clear().apply()
    }


}