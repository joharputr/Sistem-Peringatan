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
        //delete All shared preference
        App.preferenceHelper.clearAll()
        loginbtn.setOnClickListener {
            validateInput()
        }

    /*    buatAkun.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }*/

    }

    override fun onResume() {
        super.onResume()
        checklogin()
        Log.d("dataUSERHP = ", App.preferenceHelper.is_login.toString())

    }

    private fun checklogin() {
        if (App.preferenceHelper.tipe == "admin") {

            AlertDialog.Builder(this)
                    .setMessage("Anda Sudah login")
                    .setPositiveButton("DONE") { dialogInterface, i ->
                        startActivity(Intent(this, UserActivity::class.java))
                    }
                    .setCancelable(false)
                    .show()
        }
    }

  /*  override fun onBackPressed() {
      //  super.onBackPressed()
        Toast.makeText(this, "Back press disabled!", Toast.LENGTH_SHORT).show();
    }*/


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
                        App.preferenceHelper.id= jData.getString("id")
                        App.preferenceHelper.nama = jData.getString("nama")
                        App.preferenceHelper.tipe = jData.getString("tipe")
                        App.preferenceHelper.hp = jData.getString("hp")
                        App.preferenceHelper.password = jData.getString("password")

                        Log.d("dataUSERHP = ", "login = " + App.preferenceHelper.is_login + "" +
                                " id = " + App.preferenceHelper.id +
                                " nama = " + App.preferenceHelper.nama +
                                " tipe = " + App.preferenceHelper.tipe +
                                " hp = " + App.preferenceHelper.hp +
                                " password = " + App.preferenceHelper.password)
                        val intent = Intent(this, UserActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
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
