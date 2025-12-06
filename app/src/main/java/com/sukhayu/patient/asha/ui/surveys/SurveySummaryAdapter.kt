package com.sukhayu.patient.asha.ui.surveys

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

class SurveySummaryAdapter(
    private val onItemClick: (SurveySummaryUiModel) -> Unit
) : ListAdapter<SurveySummaryUiModel, SurveySummaryAdapter.SurveyViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<SurveySummaryUiModel>() {
        override fun areItemsTheSame(
            oldItem: SurveySummaryUiModel,
            newItem: SurveySummaryUiModel
        ): Boolean {
            // For now, compare by all fields; later we can add an ID field.
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: SurveySummaryUiModel,
            newItem: SurveySummaryUiModel
        ): Boolean = oldItem == newItem
    }

    inner class SurveyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPatientName: TextView = itemView.findViewById(R.id.tv_patient_name)
        private val tvSurveyType: TextView = itemView.findViewById(R.id.tv_survey_type)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_survey_date)
        private val tvVillage: TextView = itemView.findViewById(R.id.tv_village)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val ivSyncStatus: ImageView = itemView.findViewById(R.id.iv_sync_status)

        fun bind(item: SurveySummaryUiModel) {
            tvPatientName.text = item.patientName
            tvSurveyType.text = item.surveyType
            tvDate.text = item.date
            tvVillage.text = item.village ?: "-"
            tvStatus.text = item.status

            // Simple sync icon logic
            if (item.isSynced) {
                ivSyncStatus.setImageResource(R.drawable.ic_cloud_done) // you can change
                ivSyncStatus.contentDescription = "Synced"
            } else {
                ivSyncStatus.setImageResource(R.drawable.ic_cloud_off) // change as needed
                ivSyncStatus.contentDescription = "Pending sync"
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_survey_summary, parent, false)
        return SurveyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
