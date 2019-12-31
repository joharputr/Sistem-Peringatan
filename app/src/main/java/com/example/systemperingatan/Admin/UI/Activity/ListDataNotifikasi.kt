package com.example.systemperingatan.Admin.UI.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataExitEnter.ResponseDataUser
import com.example.systemperingatan.Admin.UI.pagerDataUser
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.activity_list_data_area.toolbarListArea
import kotlinx.android.synthetic.main.activity_list_data_area_exitenter.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.util.*

class ListDataNotifikasi : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_data_area_exitenter)

        viewpager_main_exit_enter.adapter = pagerDataUser(supportFragmentManager, baseContext)
        tabs_main_exit_enter.setupWithViewPager(viewpager_main_exit_enter)
        setToolbar()
        check_deleted_enter()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun check_deleted_enter() {

        App.api.dataExit().enqueue(object : Callback<ResponseDataUser> {
            override fun onResponse(call: Call<ResponseDataUser>, response: retrofit2.Response<ResponseDataUser>) {

                val data = response.body()

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val id_area_masuk = data.data.get(i)?.id_area_keluar
                        Log.d("TESTDATAKELUAR = ", id_area_masuk)
                        delete_data_enter_when_go_to_exit_area(id_area_masuk!!)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseDataUser>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@ListDataNotifikasi, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun delete_data_enter_when_go_to_exit_area(number: String) {
        val tag_string_req = "req_postdata"
        Log.d("CLOG", "deleteId:$number")
        val strReq = object : StringRequest(Method.DELETE,
                NetworkAPI.delete_data_enter_when_go_to_exit_area + "/$number", { response ->
            Log.d("CLOG", "responh: $response")
            try {
                val jObj = JSONObject(response)
                val status1 = jObj.getString("status")
                Log.d("status post  = ", status1)
                if (status1.contains("200")) {
                    //          Toast.makeText(this, "id = $number Geofence Remove!", Toast.LENGTH_SHORT).show()
                } else {
                    val msg = jObj.getString("message")
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("error catch = ", e.toString())
            }

        }, { error ->
            Log.d("CLOG", "verespon: ${error.localizedMessage}")
            val json: String?
            val response = error.networkResponse
            if (response != null && response.data != null) {
                json = String(response.data)
                val jObj: JSONObject?
                try {
                    jObj = JSONObject(json)
                    val msg = jObj.getString("message")
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        }) {
            override fun getParams(): Map<String, String> {
                // Posting parameters to login url
                val params = HashMap<String, String>()
                //      params["distance"] = distance.toString()
                return params
            }

        }

        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }

    private fun setToolbar() {
        setSupportActionBar(toolbarListArea)
        val actionBar = supportActionBar
        actionBar?.title = "List Data Notifikasi"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == android.R.id.home) {
            //   finish()
            startActivity(Intent(this, MapsAdminActivity::class.java))

        }
        return super.onOptionsItemSelected(item)
    }
}