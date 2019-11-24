package com.example.systemperingatan.Admin.UI.Fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
import com.example.systemperingatan.Admin.UI.Activity.EditNamaAreaZonaActivity
import com.example.systemperingatan.Admin.UI.Activity.EditRadiusActivity
import com.example.systemperingatan.App
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.areafragment.*
import retrofit2.Call
import retrofit2.Callback

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AreaFragment : Fragment() {
    private var itemListArea = ArrayList<DataItem>()
    val adapterArea = ListDataAreaAdapter(itemListArea, this::onClick, this::onLongClick)

    //long click
    private fun onLongClick(dataItem: DataItem) {
        if (App.preferenceHelper.tipe == "admin") {
            val options: Array<String> = arrayOf("Edit Nama", "Edit Lokasi")
            AlertDialog.Builder(context)
                    // whcih = index dar pilihan
                    .setItems(options) { dialog, which ->
                        when (which) {
                            0 -> {
                                //edit
                                editArea(dataItem)

                            }
                            1 -> {
                                editPosition(dataItem)

                            }
                        }//menghilangkan dialog
                        dialog.dismiss()
                    }
                    .show()
        }

    }

    private fun editArea(dataItem: DataItem) {
        val intent = Intent(context, EditNamaAreaZonaActivity::class.java)
        intent.putExtra("editArea", dataItem)
        startActivity(intent)
    }

    private fun editPosition(dataItem: DataItem) {
        val intent = Intent(context, EditRadiusActivity::class.java)
        intent.putExtra("editRadius", dataItem)
        startActivity(intent)
    }


   override fun onResume() {
        super.onResume()
       Log.d("testFragment","test")
        itemListArea.clear()
        adapterArea.notifyDataSetChanged()
        reloadMapMarkers()
    }

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
        Log.d("testdata", dataItem.number)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        swipeArea.setOnRefreshListener {
            reloadMapMarkers()
            swipeArea.isRefreshing = false
        }
    }

    fun reloadMapMarkers() {
        //   mMap!!.clear()
        itemListArea.clear()
        adapterArea.notifyDataSetChanged()
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
                   //     Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()

                        val dataFav1 = DataItem(number, null, null, latitude.toString(),
                                null, message, null, longitude.toString(), null, null, null)
                        Log.d("dataListArea = ", dataFav1.toString())

                        itemListArea.addAll(listOf(dataFav1))
                        adapterArea.notifyDataSetChanged()
                        initRecyclerView()

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