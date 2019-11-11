package com.example.systemperingatan.User.UI

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.activity_edit_user.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class EditUser : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)
        editNamaUser.setText(App.preferenceHelper.nama)
        editPasswordUser.setText(App.preferenceHelper.password)
        editNoHpUser.setText(App.preferenceHelper.hp)
        buttonEditUser.setOnClickListener {
            updateData(App.preferenceHelper.id)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, UserActivity::class.java))
    }


    private fun updateData(id: String) {
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.editUser + "/$id", { response ->
            Log.d("CLOG", "responh: $response")
            try {
                val jObj = JSONObject(response)
                val status1 = jObj.getString("status")
                Log.d("status post  = ", status1)
                if (status1.contains("200")) {
                    Toast.makeText(this, "Change Profile Complete", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, UserActivity::class.java))
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
                params["nama"] = editNamaUser.text.toString()
                params["password"] = editPasswordUser.text.toString()
                params["hp"] = editNoHpUser.text.toString()

                App.preferenceHelper.hp = editNoHpUser.text.toString()
                App.preferenceHelper.nama = editNamaUser.text.toString()
                App.preferenceHelper.password = editPasswordUser.text.toString()
                Log.d("testName= ", editNamaUser.text.toString() + "passsword = " + editPasswordUser.text.toString() + "hp = "
                        + editNoHpUser.text.toString())
                return params
            }
        }
        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }
}
