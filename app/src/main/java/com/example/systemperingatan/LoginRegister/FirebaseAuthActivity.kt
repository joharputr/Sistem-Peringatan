package com.example.systemperingatan.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import com.example.systemperingatan.User.UI.UserActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_edit_area.*
import kotlinx.android.synthetic.main.activity_firebase_auth.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class FirebaseAuthActivity : AppCompatActivity() {
    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    var verificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_auth)
        mAuth = FirebaseAuth.getInstance()
        App.preferenceHelper.clearAll()

        btnaskcode.setOnClickListener {
            if (editPhone.text.isNullOrEmpty()) {
                editPhone.error = getString(R.string.error_nohp_fb)
            } else {
                progressbar.visibility = View.VISIBLE
                verify()
            }
        }

        btnVerivy.setOnClickListener {
            if (editCodeVerification.text.isNullOrEmpty())
                editCodeVerification.error = "Wajib mengisi kode verifikasi"
            else {
                authenticate()
            }
        }

        //setting toolbar
        setSupportActionBar(findViewById(R.id.toolbarFirebase))
        //home navigation

        val actionBar = supportActionBar
        actionBar?.title = "Login"
    }

    //setting menu in action bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_firebase, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.login_admin -> {
            startActivity(Intent(this, Login::class.java))
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun verify() {
        verificationCallbacks()
        val phnNo = editPhone.text.toString()
        App.preferenceHelper.phonefb = phnNo
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phnNo,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        )

    }

    private fun verificationCallbacks() {
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                progressbar.visibility = View.GONE
                Log.d("onVerificatsucces = ", credential.smsCode.toString())
               // Toast.makeText(applicationContext, "Instant Verifikasi Sukses", Toast.LENGTH_SHORT).show()
                signIn(credential)

            }

            override fun onVerificationFailed(error: FirebaseException) {
                progressbar.visibility = View.GONE
                Log.d("onVerificationFailed = ", error.toString())
                Toast.makeText(applicationContext, "masukan nomor telepon sesuai format", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verfication: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verfication, p1)
                verificationId = verfication
                Toast.makeText(applicationContext, "Kode Dikirimkan", Toast.LENGTH_SHORT).show()
                progressbar.visibility = View.GONE
            }
        }
    }

    private fun signIn(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        Log.d("cekNomor = ", "" + task.result?.user)
                        val phnNo = editPhone.text.toString()
                        cek_user_firebase(phnNo)

                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w("", "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            progressbar.visibility = View.GONE
                            Toast.makeText(applicationContext, "Kode Verifikasi salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
    }

    private fun authenticate() {

        val verifiNo = editCodeVerification.text.toString()

        try {
            progressbar.visibility = View.VISIBLE
            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, verifiNo)
            signIn(credential)
        } catch (e: Exception) {
            progressbar.visibility = View.GONE
            editCodeVerification.error = "Kode Verifikasi salah"
        }
    }

    private fun cek_user_firebase(number: String) {
        val tag_string_req = "req_postdata"
        val strReq = object : StringRequest(Method.POST,
                NetworkAPI.cek_firebase_user, { response ->
            Log.d("CLOG", "responh: $response")
            try {
                progressbar.visibility = View.GONE
                val jObj = JSONObject(response)
                val status = jObj.getString("status")
                Log.d("status post  = ", status)
                if (status.contains("200")) {
                    //login sukses
                    Toast.makeText(applicationContext, "success sign in", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, UserActivity::class.java))
                    App.preferenceHelper.is_login = "1"
                } else if (status.contains("403")) {
                    //user already login
                    val msg = jObj.getString("message")
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                } else {
                    //register
                    val msg = jObj.getString("message")
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, UserActivity::class.java))
                    App.preferenceHelper.is_login = "1"
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
                params["phone"] = number

                return params
            }
        }
        App.instance?.addToRequestQueue(strReq, tag_string_req)
    }
}
