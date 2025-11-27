package com.sukhayu.patient.ui.awareness

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.databinding.ActivityDiseaseOutbreakBinding

class DiseaseOutbreakActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiseaseOutbreakBinding
    private lateinit var adapter: AwarenessAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiseaseOutbreakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        adapter = AwarenessAdapter()
        binding.rvAwareness.layoutManager = LinearLayoutManager(this)
        binding.rvAwareness.adapter = adapter

        loadAwarenessItems()
    }

    private fun loadAwarenessItems() {

        // Dummy list (replace with DB later)
        val list = listOf(
            AwarenessItem("Dengue", 124, "Use mosquito repellent, remove standing water."),
            AwarenessItem("Influenza (Flu)", 87, "Get vaccinated, wash hands frequently."),
            AwarenessItem("COVID-19", 34, "Wear mask in crowds, maintain distance.")
        )

        adapter.submitList(list)

        // Show/Hide empty state and recycler
        if (list.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.rvAwareness.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.rvAwareness.visibility = View.VISIBLE
        }
    }

    // Data class for items
    private data class AwarenessItem(
        val title: String,
        val cases: Int,
        val precautions: String
    )

    // Adapter
    private class AwarenessAdapter :
        androidx.recyclerview.widget.RecyclerView.Adapter<AwarenessAdapter.VH>() {

        private val items = mutableListOf<AwarenessItem>()

        fun submitList(list: List<AwarenessItem>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(com.sukhayu.patient.R.layout.item_awareness, parent, false)
            return VH(view)
        }
    }
}
