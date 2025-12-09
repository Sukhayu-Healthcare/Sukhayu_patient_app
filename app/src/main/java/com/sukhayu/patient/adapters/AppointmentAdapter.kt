package com.sukhayu.patient.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper

// Create a simple data class for Appointment
data class Appointment(
    val doctorName: String,
    val date: String,
    val time: String
)

class AppointmentAdapter(
    private val appointments: List<Appointment>,
    private val ttsHelper: TtsHelper
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDoctorName: TextView = itemView.findViewById(R.id.tvDoctorName)
        val tvAppointmentDate: TextView = itemView.findViewById(R.id.tvAppointmentDate)
        val tvAppointmentTime: TextView = itemView.findViewById(R.id.tvAppointmentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.apply {
            tvDoctorName.text = appointment.doctorName
            tvAppointmentDate.text = appointment.date
            tvAppointmentTime.text = appointment.time ?: "Not specified"

            // Add TTS support for appointment items
            tvDoctorName.setOnLongClickListener {
                ttsHelper.speak("Doctor: ${appointment.doctorName}")
                true
            }

            tvAppointmentDate.setOnLongClickListener {
                ttsHelper.speak("Date: ${appointment.date}")
                true
            }

            tvAppointmentTime.setOnLongClickListener {
                ttsHelper.speak("Time: ${appointment.time}")
                true
            }

            itemView.setOnLongClickListener {
                val fullText = "Appointment with Doctor ${appointment.doctorName} on ${appointment.date} at ${appointment.time}"
                ttsHelper.speak(fullText)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return appointments.size
    }
}