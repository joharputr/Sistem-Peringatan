package com.example.systemperingatan.Admin.UI.Activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.systemperingatan.Admin.UI.pager
import com.example.systemperingatan.Admin.UI.pagerDataUser
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.activity_list_data_area.toolbarListArea
import kotlinx.android.synthetic.main.activity_list_data_area_exitenter.*

class ListDataExitEnter : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_data_area_exitenter)

        viewpager_main_exit_enter.adapter = pagerDataUser(supportFragmentManager, baseContext)
        tabs_main_exit_enter.setupWithViewPager(viewpager_main_exit_enter)
        setToolbar()
    }

    override fun onBackPressed() {
        super.onBackPressed()
       finish()
    }

    private fun setToolbar() {
        setSupportActionBar(toolbarListArea)
        val actionBar = supportActionBar
        actionBar?.title = "List Data Masuk dan Keluar"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == android.R.id.home) {
            //   finish()
            startActivity(Intent(this, MapsAdminActivity::class.java))

        }
        return super.onOptionsItemSelected(item)
    }
}