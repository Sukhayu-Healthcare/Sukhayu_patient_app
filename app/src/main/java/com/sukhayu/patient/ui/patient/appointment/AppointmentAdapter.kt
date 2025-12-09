package com.sukhayu.patient.ui.patient.appointment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.entity.AppointmentEntity
import com.sukhayu.patient.databinding.ItemAppointmentBinding

class AppointmentAdapter(
    private val appointments: List<AppointmentEntity>,
    private val onDeleteClick: (AppointmentEntity) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    inner class AppointmentViewHolder(private val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: AppointmentEntity) {
            binding.apply {
                tvDoctorName.text = appointment.doctor_name
                tvDoctorPhone.text = appointment.doctor_phone
                tvDoctorType.text = when (appointment.doctor_type) {
                    "community_health_officer" -> "CHO"
                    "medical_officer" -> "MO"
                    else -> appointment.doctor_type
                }

                tvAppointmentDate.text = appointment.appointment_date
                tvAppointmentTime.text = appointment.appointment_time ?: "Not specified"

                tvNotes.text = appointment.notes ?: "No notes"

                // Sync status color
                when (appointment.sync_status) {
                    "synced" -> tvSyncStatus.setTextColor(root.context.getColor(R.color.color_success))
                    "failed" -> tvSyncStatus.setTextColor(root.context.getColor(R.color.color_error))
                    else -> tvSyncStatus.setTextColor(root.context.getColor(R.color.color_warning))
                }
                tvSyncStatus.text = appointment.sync_status?.uppercase() ?: "PENDING"

                btnDelete.setOnClickListener {
                    onDeleteClick(appointment)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount() = appointments.size
}
