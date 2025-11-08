package com.sukhayu.patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DoctorAdapter(
    private val list: List<Doctor>,
    private val onConsult: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDoctorName)
        val tvSpec: TextView = view.findViewById(R.id.tvSpecialty)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val btnConsult: Button = view.findViewById(R.id.btnConsult)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val doctor = list[position]
        holder.tvName.text = doctor.name
        holder.tvSpec.text = doctor.specialty
        holder.tvRating.text = "Rating: ${doctor.rating}"
        holder.btnConsult.setOnClickListener { onConsult(doctor) }
    }

    override fun getItemCount(): Int = list.size
}
