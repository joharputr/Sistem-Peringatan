package com.example.systemperingatan.Admin.UI.Activity.search

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.systemperingatan.API.Pojo.DataExitEnter.DataUser
import com.example.systemperingatan.API.Pojo.DataExitEnter.ResponseDataUser
import com.example.systemperingatan.Admin.Adapter.DataUserEnterAdapter
import com.example.systemperingatan.Admin.Adapter.DataUserExitAdapter
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.searchlayout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchExit : AppCompatActivity() {
    private var itemListData = ArrayList<DataUser>()
    val adapterArea = DataUserExitAdapter(itemListData)
    private var search: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.searchlayout)

        handleIntent(intent)
        initRecyclerView()
        Search()

        setSupportActionBar(searchToolbar)
        val actionBar = supportActionBar
        actionBar?.title = "data keluar = "+search

    }

    private fun initRecyclerView() {
        searchRecycler.run {
            adapter = adapterArea
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        handleIntent(intent!!)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            search = intent.getStringExtra(SearchManager.QUERY)
            Log.d("SearcghData= ", search)
        }
    }

    fun Search() {
        itemListData.clear()
        pbsearch.visibility = View.VISIBLE
        App.api.getSearch_exit(search).enqueue(object : Callback<ResponseDataUser> {
            override fun onResponse(call: Call<ResponseDataUser>, response: Response<ResponseDataUser>) {

                val data = response.body()
                pbsearch.visibility = View.GONE
                Log.d("responseSearch = ", data.toString())
                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val id = data.data.get(i)?.id
                        val phone = data.data.get(i)?.phone
                        val area = data.data.get(i)?.namaArea
                        val waktu = data.data.get(i)?.waktu

                        /*    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
    */
                        val dataUser = DataUser(phone, waktu, area, id, null,null)
                        Log.d("dataUserExit = ", dataUser.toString())

                        itemListData.addAll(listOf(dataUser))
                        adapterArea.notifyDataSetChanged()
                        initRecyclerView()

                    }else {
                        Log.d("nullCheck","null")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseDataUser>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(this@SearchExit, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}