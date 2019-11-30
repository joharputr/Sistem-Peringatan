package com.example.systemperingatan.Admin.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.item_view.view.*



class ListDataAreaZonaAdapter(private val itemList: ArrayList<DataItem>,
                              private val onLongClick: (dataitem: DataItem) -> Unit) :
        RecyclerView.Adapter<ListDataAreaZonaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            return@setOnLongClickListener true
        }

    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        fun bind(item: DataItem) = itemView.apply {
            area.text = item.message
            longitude.text = item.longitude
            latitude.text = item.latitude
            Glide.with(image)
                    .load(R.drawable.location_item)
                    .into(image)
        }
    }

}