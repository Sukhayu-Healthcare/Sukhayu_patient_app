package com.sukhayu.patient.ui.supervisor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.AshaWorker

class AshaAdapter(private var ashaList: List<AshaWorker>) : RecyclerView.Adapter<AshaAdapter.AshaViewHolder>() {

    class AshaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAshaName: TextView = view.findViewById(R.id.tvAshaName)
        val tvAshaPhone: TextView = view.findViewById(R.id.tvAshaPhone)
        val tvAshaEmail: TextView = view.findViewById(R.id.tvAshaEmail)
        val tvAshaArea: TextView = view.findViewById(R.id.tvAshaArea)
        val tvAshaStatus: TextView = view.findViewById(R.id.tvAshaStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AshaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_asha_card, parent, false)
        return AshaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AshaViewHolder, position: Int) {
        val asha = ashaList[position]
        holder.tvAshaName.text = asha.asha_name
        holder.tvAshaPhone.text = "Phone: ${asha.asha_phone}"
        holder.tvAshaEmail.text = "ID: ${asha.asha_id}"
        holder.tvAshaArea.text = "Village: ${asha.village} | District: ${asha.district}"
        holder.tvAshaStatus.text = "Active"
        holder.tvAshaStatus.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
    }

    override fun getItemCount() = ashaList.size

    fun updateData(newList: List<AshaWorker>) {
        ashaList = newList
        notifyDataSetChanged()
    }
}
