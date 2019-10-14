package com.example.systemperingatan

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.systemperingatan.Admin.MapsAdminFragment

class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        setFragment(MapsAdminFragment.newInstance, "sdsd")
    }

    private fun setFragment(fragment: Fragment, title: String) {
        this.title = title
        supportFragmentManager.beginTransaction()
                .replace(R.id.container1, fragment)
                .commit()
        Log.d("fragmentTag = ",fragment.toString())
    }
}
