package com.example.systemperingatan.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import com.example.systemperingatan.User.UI.UserActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_firebase_auth.*
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
            progressbar.visibility = View.VISIBLE
            verify()
        }

        btnVerivy.setOnClickListener {
            progressbar.visibility = View.VISIBLE
            authenticate()
        }

        //setting toolbar
        setSupportActionBar(findViewById(R.id.toolbarFirebase))
        //home navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val actionBar = supportActionBar
        actionBar?.title = "Login Firebase"
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
                signIn(credential)
            }

            override fun onVerificationFailed(error: FirebaseException) {
                progressbar.visibility = View.GONE
                Log.d("onVerificationFailed = ", error.toString())
                Toast.makeText(applicationContext, "" + error, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verfication: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verfication, p1)
                verificationId = verfication
                progressbar.visibility = View.GONE
            }
        }
    }

    private fun signIn(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener { task: Task<AuthResult> ->
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "success sign in", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, UserActivity::class.java))
                        App.preferenceHelper.is_login = "1"
                    }
                }
    }

    private fun authenticate() {

        val verifiNo = editCodeVerification.text.toString()

        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, verifiNo)

        signIn(credential)

    }
}
