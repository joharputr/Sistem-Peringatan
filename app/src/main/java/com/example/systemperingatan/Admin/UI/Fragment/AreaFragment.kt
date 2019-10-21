package com.example.systemperingatan.Admin.UI.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.API.Pojo.Response
import com.example.systemperingatan.Admin.Adapter.ListDataAreaAdapter
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.activity_list_data_area.*
import kotlinx.android.synthetic.main.areafragment.*
import retrofit2.Call
import retrofit2.Callback

class AreaFragment : Fragment() {
    private var itemListArea = ArrayList<DataItem>()
    val adapterArea = ListDataAreaAdapter(itemListArea, this::onClick)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.areafragment, container, false)
    }

    private fun initRecyclerView() {
        recyclerviewArea.run {
            adapter = adapterArea
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun onClick(dataItem: DataItem) {

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        reloadMapMarkers()
    }

    fun reloadMapMarkers() {
        //   mMap!!.clear()
        progressBar_circular.visibility = View.VISIBLE
        App.api.allData().enqueue(object : Callback<Response> {
            override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {

                val data = response.body()
                progressBar_circular.visibility = View.GONE

                for (i in 0 until data!!.data!!.size) {
                    if (data.data != null && data.data.get(i)?.type == "circle") {
                        val number = data.data.get(i)?.number
                        val latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        val longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        val expires = java.lang.Long.parseLong(data.data.get(i)?.expires)
                        val radiusMeter = java.lang.Double.parseDouble(data.data.get(i)?.radius)
                        val message = data.data.get(i)?.message.toString()
                        Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()


                    } else if (data.data != null && data.data.get(i)?.type == "point") {
                        val numberPoint = data.data.get(i)?.number
                        val latitude = java.lang.Double.parseDouble(data.data.get(i)?.latitude)
                        val longitude = java.lang.Double.parseDouble(data.data.get(i)?.longitude)
                        val message = data.data.get(i)?.message.toString()

                        val dataFav1 = DataItem(numberPoint, null, null, latitude.toString(),
                                null, message, null, null, null, null, null)
                        Log.d("dataList = ", dataFav1.toString())
                        itemListArea.addAll(listOf(dataFav1))
                        initRecyclerView()

                    } else {
                        Toast.makeText(context, "errorGet = " + response.message(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Response>, t: Throwable) {
                Log.d("gagal", "gagal =" + t.localizedMessage)
                Toast.makeText(context, "gagal =" + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }
}