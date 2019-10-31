package com.example.systemperingatan.LoginRegister

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.App
import com.example.systemperingatan.User.UI.UserActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.systemperingatan.R.layout.activity_login)
        loginbtn.setOnClickListener {
            validateInput()
        }
    }

    override fun onResume() {
        super.onResume()
        checklogin()
    }

    private fun checklogin() {
        if (App.preferenceHelper.is_login == "1") {

            AlertDialog.Builder(this)
                    .setMessage("Anda Sudah login")
                    .setPositiveButton("DONE") { dialogInterface, i ->
                        startActivity(Intent(this, UserActivity::class.java))
                    }
                    .setCancelable(false)
                    .show()
        }
    }


    private fun validateInput() {
        if (loginpassword.text.isNullOrEmpty()) {
            loginpassword.error = getString(com.example.systemperingatan.R.string.error_required)
        } else if (loginhp.text.isNullOrEmpty()) {
            loginhp.error = getString(com.example.systemperingatan.R.string.error_required)
        } else {
            addUser()
        }
    }

    private fun addUser() {
        progressLogin.visibility = View.VISIBLE
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.LoginUser, { response ->
            Log.d("CLOG", "responh: $response")
            try {
                progressLogin.visibility = View.GONE
                val jObj = JSONObject(response)
                val status = jObj.getString("status")
                Log.d("dataStatus = ", status.toString())
                val data = jObj.get("data")
                Log.d("dataUSER = ", data.toString())
                if (status.contains("200")) {
                    Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show()

                    if (data != null) {
                        val jData = jObj.getJSONObject("data")
                        App.preferenceHelper.is_login = jData.getString("is_login")
                        val nama = jData.getString("nama")

                        Log.d("dataUSERHP = ", App.preferenceHelper.is_login.toString())
                        startActivity(Intent(this, UserActivity::class.java))
                    }

                } else {
                    val msg = jObj.getString("message")
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("errorcatch = ", e.toString())
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
                    Toast.makeText(applicationContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["hp"] = loginhp.text.toString()
                params["password"] = loginpassword.text.toString()
                return params
            }
        }

        // Adding request to request queue
        App.instance?.addToRequestQueue(strReq, tag_string_req)

    }
}
