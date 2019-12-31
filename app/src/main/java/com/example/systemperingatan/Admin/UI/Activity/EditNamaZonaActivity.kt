package com.example.systemperingatan.Admin.UI.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.activity_edit_area.buttonEditArea
import kotlinx.android.synthetic.main.activity_edit_area.toolbarArea
import kotlinx.android.synthetic.main.activity_edit_zona.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class EditNamaZonaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_zona)
        val intent = intent
        val data = intent.getParcelableExtra<DataItem>("editZona")
        Log.d("cekDataNumber", data.number)

        setSupportActionBar(toolbarArea)
        val actionBar = supportActionBar
        // Set toolbar title/app title
        actionBar?.title = "Edit Data"
        actionBar?.elevation = 4.0F

        editNamaZona.setText(data.message)
        editNoHp.setText(data.no_hp)
        edit_nama_pj.setText(data.nama_p_jawab)

        buttonEditArea.setOnClickListener {
            if (editNamaZona.text.isNullOrEmpty()) {
                editNamaZona.error = "Nama wajib diisi"
            } else {
                updateData(data.number.toString())
                update_data_zona_aman(data.number.toString())
                update_data_zona_terdekat_di_data_enter(data.number.toString())
                val i = Intent(this, ListDataAreaZonaActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                //   finish()
            }
        }
    }

    private fun updateData(number: String) {
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.edit + "/$number", { response ->
            Log.d("CLOG", "responh: $response")
            try {
                val jObj = JSONObject(response)
                val status1 = jObj.getString("status")
                Log.d("status post  = ", status1)
                if (status1.contains("200")) {
                    Toast.makeText(this, "Edit Name Complete", Toast.LENGTH_SHORT).show()
                } else {

                    val msg = jObj.getString("message")
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        }) {
            override fun getParams(): Map<String, String> {
                // Posting parameters to login url
                val params = HashMap<String, String>()
                params["message"] = editNamaZona.text.toString()
                params["no_hp"] = editNoHp.text.toString()
                params["nama_p_jawab"] = edit_nama_pj.text.toString()
                Log.d("testName= ", editNamaZona.text.toString())
                return params
            }
        }
        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }

    private fun update_data_zona_aman(number: String) {
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.edit_notif_zona + "/$number", { response ->
            Log.d("CLOG", "responh: $response")
            try {
                val jObj = JSONObject(response)
                val status1 = jObj.getString("status")
                Log.d("status post  = ", status1)
                if (status1.contains("200")) {
                    //          Toast.makeText(this, "Edit Name Complete", Toast.LENGTH_SHORT).show()
                } else {

                    val msg = jObj.getString("message")
                    //      Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        }) {
            override fun getParams(): Map<String, String> {
                // Posting parameters to login url
                val params = HashMap<String, String>()
                params["nama_zona"] = editNamaZona.text.toString()
                Log.d("testNameArea= ", editNamaZona.text.toString())
                return params
            }
        }
        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }


    private fun update_data_zona_terdekat_di_data_enter(number: String) {
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.edit_zona_di_notif_masuk + "/$number", { response ->
            Log.d("CLOG", "responh: $response")
            try {
                val jObj = JSONObject(response)
                val status1 = jObj.getString("status")
                Log.d("status post  = ", status1)
                if (status1.contains("200")) {
                    //          Toast.makeText(this, "Edit Name Complete", Toast.LENGTH_SHORT).show()
                } else {

                    val msg = jObj.getString("message")
                    //      Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

        }) {
            override fun getParams(): Map<String, String> {
                // Posting parameters to login url
                val params = HashMap<String, String>()
                params["nama_zona_terdekat"] = editNamaZona.text.toString()
                Log.d("testNameArea= ", editNamaZona.text.toString())
                return params
            }
        }
        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }
}
