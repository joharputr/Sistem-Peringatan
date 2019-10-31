package com.example.systemperingatan.LoginRegister

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.activity_register.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class Register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        buttonRegister.setOnClickListener {
            validateInput()
        }
    }

    private fun validateInput() {
        if (registernama.text.isNullOrEmpty()) {
            registernama.error = getString(R.string.error_required)
        } else if (registerpassword.text.isNullOrEmpty()) {
            registerpassword.error = getString(R.string.error_required)
        } else if (registerhp.text.isNullOrEmpty()) {
            registerhp.error = getString(R.string.error_required)
        }else{
            addUser()
        }
    }

    private fun addUser() {
        progressRegister.visibility = View.VISIBLE
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.RegisterUser, { response ->
            Log.d("CLOG", "responh: $response")
            try {
                progressRegister.visibility = View.GONE
                val jObj = JSONObject(response)
                val status1 = jObj.getString("status")
                Log.d("statuspost  = ", status1)
                if (status1.contains("200")) {
                    Toast.makeText(this, "User Added!", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(applicationContext, error.localizedMessage, Toast.LENGTH_SHORT).show()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["nama"] = registernama.text.toString()
                params["hp"] = registerhp.text.toString()
                params["password"] = registerpassword.text.toString()
                return params
            }
        }

        // Adding request to request queue
        App.instance?.addToRequestQueue(strReq, tag_string_req)

    }
}
