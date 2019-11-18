package com.example.systemperingatan.Admin.UI.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.systemperingatan.API.Pojo.DataExitEnter.DataItemExitEnter
import com.example.systemperingatan.API.Pojo.DataExitEnter.ResponseExitEnter
import com.example.systemperingatan.Admin.Adapter.DataUserExitAdapter
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.lihat_data_user.*
import retrofit2.Call
import retrofit2.Callback

class LihatDataExitFragment : Fragment() {
    private var itemListData = ArrayList<DataItemExitEnter>()
    val adapterArea = DataUserExitAdapter(itemListData)


    override fun onResume() {
        super.onResume()
        itemListData.clear()
        reloadMapMarkers()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lihat_data_user, container, false)
    }

    private fun initRecyclerView() {
        recyclerviewData.run {
            adapter = adapterArea
            layoutManager = LinearLayoutManager(context)
        }
    }

    fun reloadMapMarkers() {
        progressBar_circular_data.visibility = View.VISIBLE
        App.api.dataExit().enqueue(object : Callback<ResponseExitEnter> {
            override fun onResponse(call: Call<ResponseExitEnter>, response: retrofit2.Response<ResponseExitEnter>) {

                val data = response.body()
                progressBar_circular_data.visibility = View.GONE

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null) {
                        val id = data.data.get(i)?.id
                        val phone = data.data.get(i)?.phone
                        val area = data.data.get(i)?.namaArea
                        val waktu = data.data.get(i)?.waktu

                        Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()

                        val dataUser = DataItemExitEnter(phone, waktu, area, id, null)
                        Log.d("dataUserExit = ", dataUser.toString())

                        itemListData.addAll(listOf(dataUser))
                        adapterArea.notifyDataSetChanged()
                        initRecyclerView()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseExitEnter>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(context, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}