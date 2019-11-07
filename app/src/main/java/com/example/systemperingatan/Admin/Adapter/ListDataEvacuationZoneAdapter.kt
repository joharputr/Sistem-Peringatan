package com.example.systemperingatan.Admin.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.systemperingatan.API.Pojo.DataItem
import com.example.systemperingatan.R
import kotlinx.android.synthetic.main.item_view.view.area
import kotlinx.android.synthetic.main.item_view_evacuation_zone.view.*


class ListDataEvacuationZoneAdapter(private val itemList: ArrayList<DataItem>,
                                    private val onClik: (DataItem) -> Unit) :
        RecyclerView.Adapter<ListDataEvacuationZoneAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_view_evacuation_zone, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onClik(item)
        }
    }

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        fun bind(item: DataItem) = itemView.apply {
            area.text = item.message
            address.text = item.address
            distance.text = item.distance + " Meter"
        }
    }

}