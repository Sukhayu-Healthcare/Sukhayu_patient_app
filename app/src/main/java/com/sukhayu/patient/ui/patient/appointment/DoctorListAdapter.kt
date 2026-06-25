package com.sukhayu.patient.ui.patient.appointment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

import com.sukhayu.patient.data.remote.Doctor

class DoctorListAdapter(
    private val doctors: List<Doctor>,
    private val onDoctorClick: (phoneNumber: String) -> Unit
) : RecyclerView.Adapter<DoctorListAdapter.DoctorViewHolder>() {

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.cardDoctor)
        val tvName: TextView = itemView.findViewById(R.id.tvDoctorName)
        val tvSpecialization: TextView = itemView.findViewById(R.id.tvDoctorSpecialization)
        val tvDays: TextView = itemView.findViewById(R.id.tvDoctorDays)
        val tvPhone: TextView = itemView.findViewById(R.id.tvDoctorPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.tvName.text = doctor.doc_name
        holder.tvSpecialization.text = doctor.doc_speciality ?: "General"
        holder.tvDays.text = doctor.hospital_taluka ?: "N/A"
        holder.tvPhone.text = doctor.doc_phone ?: "N/A"
        holder.card.setOnClickListener {
            onDoctorClick(doctor.doc_phone ?: "")
        }
    }

    override fun getItemCount(): Int = doctors.size
}
