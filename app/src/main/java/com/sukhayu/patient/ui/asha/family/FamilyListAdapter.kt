package com.sukhayu.patient.ui.asha.family

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R

class FamilyListAdapter(
    private val onViewMembersClick: (String) -> Unit
) : RecyclerView.Adapter<FamilyListAdapter.FamilyViewHolder>() {

    private val items = mutableListOf<FamilyGroup>()

    fun submitList(families: List<FamilyGroup>) {
        items.clear()
        items.addAll(families)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamilyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_family, parent, false)
        return FamilyViewHolder(view)
    }

    override fun onBindViewHolder(holder: FamilyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class FamilyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFamilySurname: TextView = itemView.findViewById(R.id.tvFamilySurname)
        private val tvFamilyHead: TextView = itemView.findViewById(R.id.tvFamilyHead)
        private val tvFamilyPhone: TextView = itemView.findViewById(R.id.tvFamilyPhone)
        private val tvMemberCount: TextView = itemView.findViewById(R.id.tvMemberCount)
        private val btnViewMembers: Button = itemView.findViewById(R.id.btnViewMembers)

        fun bind(family: FamilyGroup) {
            tvFamilySurname.text = "${family.familySurname} Family"
            tvFamilyHead.text = "Head: ${family.familyHead}"
            tvFamilyPhone.text = "Phone: ${family.phone}"
            tvMemberCount.text = "${family.memberCount} member${if (family.memberCount > 1) "s" else ""}"

            btnViewMembers.setOnClickListener {
                onViewMembersClick(family.supremeId)
            }
        }
    }
}

