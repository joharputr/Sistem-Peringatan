package com.example.systemperingatan.Admin.UI.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.activity_edit_area.*
import kotlinx.android.synthetic.main.spinner_item_selected.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class EditNamaAreaActivity : AppCompatActivity() {
    var data = DataItem()
    var ceklevel: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_area)
        val intent = intent
        data = intent.getParcelableExtra("editArea")
        Log.d("cekDataNumber", data.number)

        setSupportActionBar(toolbarArea)
        val actionBar = supportActionBar
        // Set toolbar title/app title
        actionBar?.title = "Edit Data"
        actionBar?.elevation = 4.0F

        editNama.setText(data.message)
        val dataLevel = arrayOf("Pilih Level", "Normal", "Waspada", "Siaga", "Awal")

        val adapter = ArrayAdapter(this, R.layout.spinner_item_selected, dataLevel)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        spinnerEdit?.adapter = adapter
        spinnerEdit?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                text1.text = data.level
                if (parent.getItemAtPosition(position) == "Pilih Level") {
                    ceklevel = data.level
                } else {
                    ceklevel = parent.getItemAtPosition(position).toString()
                }
                Log.d("reminderlevel  = ", ceklevel)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        buttonEditArea.setOnClickListener {
            if (editNama.text.isNullOrEmpty()) {
                editNama.error = "Nama wajib diisi"
            } else {
                updateData(data.number.toString())
                update_nama_area_masuk(data.number.toString())
                update_nama_area_keluar(data.number.toString())

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
                params["message"] = editNama.text.toString()
                params["level"] = ceklevel.toString()
                Log.d("testName= ", editNama.text.toString())
                return params
            }
        }
        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }


    private fun update_nama_area_masuk(number: String) {
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.edit_notif_masuk + "/$number", { response ->
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
                params["area"] = editNama.text.toString()
                Log.d("testNameArea= ", editNama.text.toString())
                return params
            }
        }
        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }

    private fun update_nama_area_keluar(number: String) {
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.edit_notif_keluar + "/$number", { response ->
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
                params["area"] = editNama.text.toString()
                Log.d("testNameArea= ", editNama.text.toString())
                return params
            }
        }
        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }
}
