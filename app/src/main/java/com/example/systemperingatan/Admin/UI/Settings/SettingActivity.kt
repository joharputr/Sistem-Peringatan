package com.example.systemperingatan.Admin.UI.Settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.systemperingatan.R

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        if (savedInstanceState == null) {
            val preferenceFragment = SettingFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, preferenceFragment)
                    .commit()
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}