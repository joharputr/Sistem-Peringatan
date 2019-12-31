package com.example.systemperingatan.Admin.UI.Fragment

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.systemperingatan.API.Pojo.DataExitEnter.DataUser
import com.example.systemperingatan.API.Pojo.DataExitEnter.ResponseDataUser
import com.example.systemperingatan.Admin.Adapter.DataUserEnterAdapter
import com.example.systemperingatan.Admin.UI.Activity.search.SearchEnter
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.lihat_data_user.*
import retrofit2.Call
import retrofit2.Callback

class LihatDataEnterFragment : Fragment() {
    private var itemListData = ArrayList<DataUser>()
    val adapterArea = DataUserEnterAdapter(itemListData)


    override fun onResume() {
        super.onResume()
        itemListData.clear()


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lihat_data_user, container, false)
    }

    private fun addLevel() {

        val data = arrayOf("Jarak", "Waktu")

        val adapter = ArrayAdapter(context, R.layout.spinner_item_selected, data)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        sorting?.adapter = adapter
        sorting?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                val check = parent.getItemAtPosition(position).toString()
             //   Toast.makeText(context, check, Toast.LENGTH_SHORT).show()
                if ( parent.getItemAtPosition(position).toString() == "Jarak"){
                    reloadMapMarkers_jarak()
                }else{
                    reloadMapMarkers()
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

    }

    private fun initRecyclerView() {
        recyclerviewData.run {
            adapter = adapterArea
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        addLevel()
      //  reloadMapMarkers()
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val componentName = ComponentName(context!!, SearchEnter::class.java)

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


    fun reloadMapMarkers() {
        itemListData.clear()
        progressBar_circular_data.visibility = View.VISIBLE
        App.api.dataEnter().enqueue(object : Callback<ResponseDataUser> {
            override fun onResponse(call: Call<ResponseDataUser>, response: retrofit2.Response<ResponseDataUser>) {

                val data = response.body()
                progressBar_circular_data.visibility = View.GONE

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val id = data.data.get(i)?.id
                        val phone = data.data.get(i)?.phone
                        val area = data.data.get(i)?.namaArea
                        val waktu = data.data.get(i)?.waktu
                        val zona = data.data.get(i)?.namaZonaTerdekat
                        val level = data.data.get(i)?.level
                        val jarak = data.data.get(i)?.jarak

                        /* Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
          */
                        val dataUser = DataUser(phone, waktu, area, id, null, zona, level,jarak)
                        Log.d("dataUser = ", dataUser.toString())

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


    fun reloadMapMarkers_jarak() {
        itemListData.clear()
        progressBar_circular_data.visibility = View.VISIBLE
        App.api.dataEnter_jarak().enqueue(object : Callback<ResponseDataUser> {
            override fun onResponse(call: Call<ResponseDataUser>, response: retrofit2.Response<ResponseDataUser>) {

                val data = response.body()
                progressBar_circular_data.visibility = View.GONE

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val id = data.data.get(i)?.id
                        val phone = data.data.get(i)?.phone
                        val area = data.data.get(i)?.namaArea
                        val waktu = data.data.get(i)?.waktu
                        val zona = data.data.get(i)?.namaZonaTerdekat
                        val level = data.data.get(i)?.level
                        val jarak = data.data.get(i)?.jarak
                        /* Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
          */
                        val dataUser = DataUser(phone, waktu, area, id, null, zona, level,jarak)
                        Log.d("dataUser = ", dataUser.toString())

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