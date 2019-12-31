package com.example.systemperingatan.Admin.UI.Fragment

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.StringRequest
import com.example.systemperingatan.API.NetworkAPI
import com.example.systemperingatan.API.Pojo.DataExitEnter.DataUser
import com.example.systemperingatan.API.Pojo.DataExitEnter.ResponseDataUser
import com.example.systemperingatan.Admin.Adapter.DataUserExitAdapter
import com.example.systemperingatan.Admin.UI.Activity.search.SearchExit
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.lihat_data_user.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.listOf

class LihatDataExitFragment : Fragment() {
    private var itemListData = ArrayList<DataUser>()
    val adapterArea = DataUserExitAdapter(itemListData)


    override fun onResume() {
        super.onResume()
        itemListData.clear()
        reloadMapMarkers()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lihat_data_user, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sorting.visibility = View.GONE
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val componentName = ComponentName(context!!, SearchExit::class.java)

        searchMovie.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchMovie.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("querysub =", query)

                return false
            }
        })

        swipeSetup()
    }

    private fun swipeSetup() {
        swipeNotif.setOnRefreshListener {
            reloadMapMarkers()
            swipeNotif.isRefreshing = false
        }
    }

    private fun initRecyclerView() {
        recyclerviewData.run {
            adapter = adapterArea
            layoutManager = LinearLayoutManager(context)
        }
    }

    fun reloadMapMarkers() {
        itemListData.clear()
        progressBar_circular_data.visibility = View.VISIBLE
        App.api.dataExit().enqueue(object : Callback<ResponseDataUser> {
            override fun onResponse(call: Call<ResponseDataUser>, response: retrofit2.Response<ResponseDataUser>) {

                val data = response.body()
                progressBar_circular_data.visibility = View.GONE

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val id = data.data.get(i)?.id
                        val phone = data.data.get(i)?.phone
                        val area = data.data.get(i)?.namaArea
                        val waktu = data.data.get(i)?.waktu
                        val id_area_masuk = data.data.get(i)?.id
                        /*    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
    */
                          val dataUser = DataUser(phone, waktu, area, id, null, null, null, null,id_area_masuk)
                        Log.d("dataUserExit = ", dataUser.toString())

                        itemListData.addAll(listOf(dataUser))
                        adapterArea.notifyDataSetChanged()
                        initRecyclerView()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseDataUser>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(context, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }


}