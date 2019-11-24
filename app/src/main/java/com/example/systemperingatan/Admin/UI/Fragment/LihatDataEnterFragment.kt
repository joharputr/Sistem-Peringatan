package com.example.systemperingatan.Admin.UI.Fragment

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
import com.example.systemperingatan.Admin.Adapter.DataUserEnterAdapter
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

              /*          Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
*/
                        val dataUser = DataUser(phone, waktu, area, id ,null,zona)
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