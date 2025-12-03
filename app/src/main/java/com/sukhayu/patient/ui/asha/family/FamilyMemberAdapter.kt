package com.sukhayu.patient.ui.asha.family

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.entity.PatientEntity

class FamilyMemberAdapter(
    private val onConductSurvey: (PatientEntity) -> Unit
) : RecyclerView.Adapter<FamilyMemberAdapter.MemberViewHolder>() {

    private val items = mutableListOf<PatientEntity>()

    fun submitList(patients: List<PatientEntity>) {
        items.clear()
        items.addAll(patients)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_family_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvMemberName)
        private val tvGender: TextView = itemView.findViewById(R.id.tvMemberGender)
        private val tvAge: TextView = itemView.findViewById(R.id.tvMemberAge)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvMemberPhone)
        private val btnSurvey: Button = itemView.findViewById(R.id.btnConductSurvey)

        fun bind(patient: PatientEntity) {
            tvName.text = patient.name
            tvGender.text = "Gender: ${patient.gender ?: "N/A"}"

            // Calculate age from patient.age field (direct value) or show N/A
            val ageText = if (patient.age != null && patient.age > 0) {
                "Age: ${patient.age} years"
            } else {
                "Age: N/A"
            }
            tvAge.text = ageText

            tvPhone.text = "Phone: ${patient.phone ?: "N/A"}"

            btnSurvey.setOnClickListener {
                onConductSurvey(patient)
            }
        }
    }
}

