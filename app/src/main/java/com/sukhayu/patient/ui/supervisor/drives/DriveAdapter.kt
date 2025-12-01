package com.sukhayu.patient.ui.supervisor.drives

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

data class DriveItem(
    val driveName: String,
    val date: String,
    val venue: String,
    val remark: String,
    val ashaName: String
)

class DriveAdapter(private val items: List<DriveItem>) : RecyclerView.Adapter<DriveAdapter.DriveViewHolder>() {

    inner class DriveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDriveName: TextView = itemView.findViewById(R.id.tvDriveName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvVenue: TextView = itemView.findViewById(R.id.tvVenue)
        private val tvAshaName: TextView = itemView.findViewById(R.id.tvAshaName)

        fun bind(item: DriveItem) {
            tvDriveName.text = item.driveName
            tvDate.text = "Date: ${item.date}"
            tvVenue.text = "Venue: ${item.venue}"
            tvAshaName.text = "ASHA: ${item.ashaName}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriveViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_drive, parent, false)
        return DriveViewHolder(view)
    }

    override fun onBindViewHolder(holder: DriveViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
