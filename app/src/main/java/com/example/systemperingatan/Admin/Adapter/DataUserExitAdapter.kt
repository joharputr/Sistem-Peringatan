package com.example.systemperingatan.Admin.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.systemperingatan.API.Pojo.DataExitEnter.DataItemExitEnter
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.item_view_enter.view.*
import kotlinx.android.synthetic.main.item_view_enter.view.area_data
import kotlinx.android.synthetic.main.item_view_enter.view.phone
import kotlinx.android.synthetic.main.item_view_enter.view.waktu
import kotlinx.android.synthetic.main.item_view_exit.view.*

class DataUserExitAdapter(private val data: ArrayList<DataItemExitEnter>) : RecyclerView.Adapter<DataUserExitAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: DataItemExitEnter) = itemView.apply {
            phone.text = item.phone
            area_data_exit.text = item.namaArea
            waktu_exit.text = item.waktu
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_view_exit, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }
}