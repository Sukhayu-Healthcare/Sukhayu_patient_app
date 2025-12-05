package com.sukhayu.patient.ui.awareness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.databinding.ActivityDiseaseOutbreakBinding
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.patient.utils.HeaderUtils

class DiseaseOutbreakActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiseaseOutbreakBinding
    private lateinit var adapter: AwarenessAdapter
    private lateinit var voiceHelper: VoiceInputHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiseaseOutbreakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        // Setup RecyclerView
        adapter = AwarenessAdapter()
        binding.rvAwareness.layoutManager = LinearLayoutManager(this)
        binding.rvAwareness.adapter = adapter

        loadAwarenessItems()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun initViews() {
        // Views are now initialized through binding
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

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
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

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int = items.size

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: android.widget.TextView? = view.findViewById(com.sukhayu.patient.R.id.tvAwarenessTitle)
            val tvDesc: android.widget.TextView? = view.findViewById(com.sukhayu.patient.R.id.tvAwarenessDescription)

            init {
                // Use safe calls to prevent crashes
                if (tvTitle == null || tvDesc == null) {
                    android.util.Log.e("DiseaseOutbreak", "Layout missing required views")
                }
            }

            fun bind(item: AwarenessItem) {
                tvTitle?.text = item.title
                tvDesc?.text = "Cases: ${item.cases}, Precautions: ${item.precautions}"
            }
        }
    }
}
