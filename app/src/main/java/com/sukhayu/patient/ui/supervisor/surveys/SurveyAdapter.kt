package com.sukhayu.patient.ui.supervisor.surveys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

data class SurveyItem(
    val surveyName: String,
    val date: String,
    val type: String,
    val status: String,
    val ashaName: String,
    val patientCount: Int
)

class SurveyAdapter(private val items: List<SurveyItem>) : RecyclerView.Adapter<SurveyAdapter.SurveyViewHolder>() {

    inner class SurveyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSurveyName: TextView = itemView.findViewById(R.id.tvSurveyName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvAshaName: TextView = itemView.findViewById(R.id.tvAshaName)
        private val tvPatientCount: TextView = itemView.findViewById(R.id.tvPatientCount)

        fun bind(item: SurveyItem) {
            tvSurveyName.text = item.surveyName
            tvDate.text = "Date: ${item.date}"
            tvType.text = "Type: ${item.type}"
            tvStatus.text = "Status: ${item.status}"
            tvAshaName.text = "ASHA: ${item.ashaName}"
            tvPatientCount.text = if (item.patientCount > 0) "Patients: ${item.patientCount}" else "Pending"

            val statusColor = when (item.status) {
                "Completed" -> itemView.context.getColor(android.R.color.holo_green_light)
                "Ongoing" -> itemView.context.getColor(android.R.color.holo_blue_light)
                else -> itemView.context.getColor(android.R.color.holo_orange_light)
            }
            tvStatus.setTextColor(statusColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_survey, parent, false)
        return SurveyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
