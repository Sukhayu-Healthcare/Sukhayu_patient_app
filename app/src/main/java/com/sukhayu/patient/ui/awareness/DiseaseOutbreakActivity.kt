package com.sukhayu.patient.ui.awareness

import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.databinding.ActivityDiseaseOutbreakBinding

class DiseaseOutbreakActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiseaseOutbreakBinding
    private lateinit var adapter: AwarenessAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiseaseOutbreakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AwarenessAdapter()
        binding.rvAwareness.layoutManager = LinearLayoutManager(this)
        binding.rvAwareness.adapter = adapter

        loadAwarenessItems()
    }

    private fun loadAwarenessItems() {
        val list = listOf(
            AwarenessItem("Dengue", 124, "Use mosquito repellent, remove standing water."),
            AwarenessItem("Influenza (Flu)", 87, "Get vaccinated, wash hands frequently."),
            AwarenessItem("COVID-19", 34, "Wear mask in crowds, maintain distance.")
        )

        adapter.submitList(list)

        if (list.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.rvAwareness.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.rvAwareness.visibility = View.VISIBLE
        }
    }

    // ------------ data model ------------
    private data class AwarenessItem(
        val title: String,
        val cases: Int,
        val precautions: String
    )

    // ------------ adapter ------------
    private class AwarenessAdapter :
        RecyclerView.Adapter<AwarenessAdapter.VH>() {

        private val items = mutableListOf<AwarenessItem>()

        fun submitList(list: List<AwarenessItem>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_awareness, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvCases: TextView = itemView.findViewById(R.id.tvCases)
            private val tvPrecautions: TextView = itemView.findViewById(R.id.tvPrecautions)

            fun bind(item: AwarenessItem) {
                tvTitle.text = item.title
                tvCases.text = "Cases: ${item.cases}"
                tvPrecautions.text = item.precautions
            }
        }
    }
}
