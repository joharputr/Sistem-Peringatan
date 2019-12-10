package com.example.systemperingatan.Admin.UI.Settings

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.systemperingatan.API.Pojo.DataExitEnter.DataUser
import com.example.systemperingatan.API.Pojo.DataExitEnter.ResponseDataUser
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SettingFragment : PreferenceFragmentCompat(), androidx.preference.Preference.OnPreferenceChangeListener {
    private val notifikasi = NotifikasiDataUser()
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences);

        val switchView = preferenceScreen.findPreference("key_notif") as SwitchPreferenceCompat
        switchView.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: androidx.preference.Preference?, newValue: Any?): Boolean {
        val key = preference?.key
        val isSet = newValue as Boolean

        if (key == "key_notif") {
            if (isSet) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d("SETTINGNOTIF", "true")
                    Search()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d("SETTINGNOTIF", "false")
                }
            }
        }
        return true
    }

    @SuppressLint("SimpleDateFormat")
    fun Search() {
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())

        App.api.getSearch_enter(currentDate).enqueue(object : Callback<ResponseDataUser> {
            override fun onResponse(call: Call<ResponseDataUser>, response: Response<ResponseDataUser>) {

                val data = response.body()

                Log.d("responseSearch = ", data.toString())
                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val id = data.data.get(i)?.id
                        val phone = data.data.get(i)?.phone
                        val nama_zona_terdekat = data.data.get(i)?.namaZonaTerdekat
                        val waktu = data.data.get(i)?.waktu
                        val area = data.data.get(i)?.namaArea

                        val dataUserAman = DataUser(phone, waktu, area, id, null, nama_zona_terdekat)

                        val dataArrayListEnter = ArrayList<DataUser>()
                        dataArrayListEnter.addAll(listOf(dataUserAman))
                        notifikasi.setRepeatingAlarm(context!!,dataUserAman)
                        Log.d("dataUserSearch = ", dataArrayListEnter.toString())
                    } else {
                        Log.d("nullCheck", "null")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseDataUser>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)

            }
        })
    }
}

