package com.sukhayu.patient.ui.asha.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.entity.PatientEntity

class PatientListAdapter(
    private val onPatientSelected: (PatientEntity) -> Unit,
    private val onDeleteClick: (PatientEntity) -> Unit
) : RecyclerView.Adapter<PatientListAdapter.PatientViewHolder>() {

    private val items = mutableListOf<PatientEntity>()

    fun submitList(patients: List<PatientEntity>) {
        items.clear()
        items.addAll(patients)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvPatientName)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPatientPhone)
        private val tvGender: TextView = itemView.findViewById(R.id.tvPatientGender)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDeletePatient)

        fun bind(patient: PatientEntity) {

            tvName.text = patient.name
            tvPhone.text = patient.phone ?: "—"
            tvGender.text = patient.gender ?: "—"

            // When the row is clicked → open survey
            itemView.setOnClickListener {
                onPatientSelected(patient)
            }

            // When delete icon is clicked → deletion callback
            ivDelete.setOnClickListener {
                onDeleteClick(patient)
            }
        }
    }
}
