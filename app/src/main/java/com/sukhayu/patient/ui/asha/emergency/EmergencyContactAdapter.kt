package com.sukhayu.patient.ui.asha.emergency

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

class EmergencyContactAdapter(
    private val contacts: List<EmergencyContact>,
    private val onContactClick: (String) -> Unit
) : RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContactName: TextView = itemView.findViewById(R.id.tvContactName)
        private val tvContactNumber: TextView = itemView.findViewById(R.id.tvContactNumber)
        private val tvContactDescription: TextView = itemView.findViewById(R.id.tvContactDescription)
        private val ivPhone: ImageView = itemView.findViewById(R.id.ivPhone)

        fun bind(contact: EmergencyContact) {
            tvContactName.text = contact.name
            tvContactNumber.text = contact.number
            tvContactDescription.text = contact.description

            itemView.setOnClickListener {
                onContactClick(contact.number)
            }
        }
    }
}

