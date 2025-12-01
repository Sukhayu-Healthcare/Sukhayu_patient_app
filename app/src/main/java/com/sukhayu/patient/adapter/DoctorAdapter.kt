package com.sukhayu.patient.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.databinding.ItemDoctorBinding
import com.sukhayu.patient.model.Doctor

class DoctorAdapter(
    private val onVideoCallClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    private var doctors = listOf<Doctor>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemDoctorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoctorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.bind(doctor)
        
        holder.binding.btnVideoCall.setOnClickListener {
            onVideoCallClick(doctor)
        }
    }

    override fun getItemCount(): Int {
        return doctors.size
    }

    fun submitList(newDoctors: List<Doctor>) {
        doctors = newDoctors
        notifyDataSetChanged()
    }

    class DoctorViewHolder(val binding: ItemDoctorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(doctor: Doctor) {
            binding.tvDoctorName.text = doctor.name
            binding.tvDoctorSpecialization.text = doctor.specialty ?: "General Physician"
            binding.tvDoctorRating.text = "⭐ ${doctor.rating ?: "N/A"}"
        }
    }
}