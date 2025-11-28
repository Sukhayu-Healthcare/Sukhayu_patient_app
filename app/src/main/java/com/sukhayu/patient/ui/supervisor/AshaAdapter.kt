package com.sukhayu.patient.ui.supervisor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.AshaWorker

class AshaAdapter(private var ashaWorkers: List<AshaWorker>) : RecyclerView.Adapter<AshaAdapter.AshaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AshaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_asha_worker, parent, false)
        return AshaViewHolder(view)
    }

    override fun onBindViewHolder(holder: AshaViewHolder, position: Int) {
        holder.bind(ashaWorkers[position])
    }

    override fun getItemCount(): Int = ashaWorkers.size

    fun updateData(newData: List<AshaWorker>) {
        ashaWorkers = newData
        notifyDataSetChanged()
    }

    inner class AshaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        private val tvVillage: TextView = itemView.findViewById(R.id.tvVillage)
        private val tvDistrict: TextView = itemView.findViewById(R.id.tvDistrict)
        private val tvTaluka: TextView = itemView.findViewById(R.id.tvTaluka)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)

        fun bind(asha: AshaWorker) {
            tvName.text = asha.name
            tvPhone.text = "📱 ${asha.phone}"
            tvVillage.text = "Village: ${asha.village}"
            tvDistrict.text = "District: ${asha.district}"
            tvTaluka.text = "Taluka: ${asha.taluka}"
            tvRole.text = "Role: ${asha.role}"
        }
    }
}
