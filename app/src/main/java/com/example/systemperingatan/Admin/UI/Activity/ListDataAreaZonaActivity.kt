package com.example.systemperingatan.Admin.UI.Activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.systemperingatan.Admin.UI.pager
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.activity_list_data_area.*

class ListDataAreaZonaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_data_area)

        viewpager_main.adapter = pager(supportFragmentManager, baseContext)
        tabs_main.setupWithViewPager(viewpager_main)
        setToolbar()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        /*   if (App.preferenceHelper.tipe == "admin")
               startActivity(Intent(this, MapsAdminActivity::class.java))
           else
               startActivity(Intent(this, UserActivity::class.java))*/
        finish()
    }

    private fun setToolbar() {
        setSupportActionBar(toolbarListArea)
        val actionBar = supportActionBar
        actionBar?.title = "List Area dan Zona Evakuasi"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == android.R.id.home) {
            /* if (App.preferenceHelper.tipe == "admin")
                 startActivity(Intent(this, MapsAdminActivity::class.java))
             else
                 startActivity(Intent(this, UserActivity::class.java))

 */
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}

