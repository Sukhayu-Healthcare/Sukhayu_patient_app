package com.sukhayu.patient.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.model.Doctor

class DoctorAdapter(
    private val list: List<Doctor>,
    private val onVideoCall: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDoctorName)
        val tvSpec: TextView = view.findViewById(R.id.tvDoctorSpecialization)
        val tvRating: TextView = view.findViewById(R.id.tvDoctorRating)
        val btnConsult: Button = view.findViewById(R.id.btnConsult)
        val btnVideoCall: Button = view.findViewById(R.id.btnVideoCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val doctor = list[position]
        holder.tvName.text = doctor.name
        holder.tvSpec.text = doctor.specialty ?: "General Physician"
        holder.tvRating.text = "⭐ ${doctor.rating ?: "N/A"}"
        
        // Video Call button
        holder.btnVideoCall.setOnClickListener { 
            onVideoCall(doctor) 
        }
        
        // Consult button (for regular consultation)
        holder.btnConsult.setOnClickListener { 
            // Handle regular consultation
        }
    }

    override fun getItemCount(): Int = list.size
}
