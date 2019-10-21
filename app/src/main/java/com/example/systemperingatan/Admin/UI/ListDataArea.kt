package com.example.systemperingatan.Admin.UI

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.API.Pojo.Response
import com.example.systemperingatan.Admin.Adapter.ListDataAreaAdapter
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.activity_list_data_area.*
import retrofit2.Call
import retrofit2.Callback

class ListDataArea : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_data_area)

        viewpager_main.adapter = pager(supportFragmentManager, baseContext)
        tabs_main.setupWithViewPager(viewpager_main)
        setToolbar()
    }

    private fun setToolbar() {
        setSupportActionBar(toolbarListArea)
        val actionBar = supportActionBar
        actionBar?.title = "List Area dan Zona Evakuasi"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}

