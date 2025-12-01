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
        val textViewDoctorName: TextView = itemView.findViewById(R.id.text_view_doctor_name)
        val textViewDate: TextView = itemView.findViewById(R.id.text_view_date)
        val textViewTime: TextView = itemView.findViewById(R.id.text_view_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.textViewDoctorName.text = appointment.doctorName
        holder.textViewDate.text = appointment.date
        holder.textViewTime.text = appointment.time

        // Add TTS support for appointment items
        holder.textViewDoctorName.setOnLongClickListener {
            ttsHelper.speak("Doctor: ${appointment.doctorName}")
            true
        }

        holder.textViewDate.setOnLongClickListener {
            ttsHelper.speak("Date: ${appointment.date}")
            true
        }

        holder.textViewTime.setOnLongClickListener {
            ttsHelper.speak("Time: ${appointment.time}")
            true
        }

        holder.itemView.setOnLongClickListener {
            val fullText = "Appointment with Doctor ${appointment.doctorName} on ${appointment.date} at ${appointment.time}"
            ttsHelper.speak(fullText)
            true
        }

        // ...existing click logic...
    }

    override fun getItemCount(): Int {
        return appointments.size
    }
}