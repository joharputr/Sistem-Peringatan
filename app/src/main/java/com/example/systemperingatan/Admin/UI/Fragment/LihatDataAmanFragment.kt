package com.example.systemperingatan.Admin.UI.Fragment

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.systemperingatan.API.Pojo.DataExitEnter.DataUser
import com.example.systemperingatan.API.Pojo.DataExitEnter.ResponseDataUser
import com.example.systemperingatan.Admin.Adapter.DataUserAmanAdapter
import com.example.systemperingatan.Admin.UI.Activity.search.SearchDataUser
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.lihat_data_user.*
import retrofit2.Call
import retrofit2.Callback

class LihatDataAmanFragment : Fragment() {
    private var itemListData = ArrayList<DataUser>()
    val adapterArea = DataUserAmanAdapter(itemListData)

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
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val componentName = ComponentName(context!!, SearchDataUser::class.java)

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
    }

    private fun initRecyclerView() {
        recyclerviewData.run {
            adapter = adapterArea
            layoutManager = LinearLayoutManager(context)
        }
    }

    fun reloadMapMarkers() {
        progressBar_circular_data.visibility = View.VISIBLE
        App.api.dataAman().enqueue(object : Callback<ResponseDataUser> {
            override fun onResponse(call: Call<ResponseDataUser>, response: retrofit2.Response<ResponseDataUser>) {

                val data = response.body()
                progressBar_circular_data.visibility = View.GONE

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val id = data.data.get(i)?.id
                        val phone = data.data.get(i)?.phone
                        val nama_zona = data.data.get(i)?.nama_zona
                        val waktu = data.data.get(i)?.waktu

                        /*    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
    */
                        val dataUserAman = DataUser(phone, waktu, null, id, nama_zona)
                        Log.d("dataUserExit = ", dataUserAman.toString())

                        itemListData.addAll(listOf(dataUserAman))
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