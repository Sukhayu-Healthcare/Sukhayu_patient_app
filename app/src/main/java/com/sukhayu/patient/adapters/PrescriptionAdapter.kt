package com.sukhayu.patient.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.TtsHelper

// Create a simple data class for Prescription
data class Prescription(
    val medicineName: String,
    val dosage: String,
    val frequency: String
)

class PrescriptionAdapter(
    private val prescriptions: List<Prescription>,
    private val ttsHelper: TtsHelper
) : RecyclerView.Adapter<PrescriptionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewMedicineName: TextView = itemView.findViewById(R.id.textViewMedicineName)
        val textViewDosage: TextView = itemView.findViewById(R.id.textViewDosage)
        val textViewFrequency: TextView = itemView.findViewById(R.id.textViewFrequency)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prescription, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prescription = prescriptions[position]
        holder.textViewMedicineName.text = prescription.medicineName
        holder.textViewDosage.text = prescription.dosage
        holder.textViewFrequency.text = prescription.frequency

        // Add TTS support for prescription items
        holder.textViewMedicineName.setOnLongClickListener {
            ttsHelper.speak("Medicine: ${prescription.medicineName}")
            true
        }

        holder.textViewDosage.setOnLongClickListener {
            ttsHelper.speak("Dosage: ${prescription.dosage}")
            true
        }

        holder.textViewFrequency.setOnLongClickListener {
            ttsHelper.speak("Frequency: ${prescription.frequency}")
            true
        }

        holder.itemView.setOnLongClickListener {
            val fullText = "Medicine ${prescription.medicineName}, Dosage ${prescription.dosage}, ${prescription.frequency}"
            ttsHelper.speak(fullText)
            true
        }

        // ...existing click logic...
    }

    override fun getItemCount(): Int {
        return prescriptions.size
    }
}