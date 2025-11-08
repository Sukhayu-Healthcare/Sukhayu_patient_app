package com.sukhayu.patient.ui.awareness

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.databinding.ActivityDiseaseOutbreakBinding
// import com.sukhayu.patient.model.AwarenessItem  // Commented if model not ready

class DiseaseOutbreakActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiseaseOutbreakBinding
    // private lateinit var adapter: AwarenessAdapter // Commented for now

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiseaseOutbreakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // adapter = AwarenessAdapter()
        // binding.rvAwareness.layoutManager = LinearLayoutManager(this)
        // binding.rvAwareness.adapter = adapter

        // Temporarily disable data loading
        // loadAwarenessItems()
    }

    /*
    private fun loadAwarenessItems() {
        val list = listOf(
            AwarenessItem("Dengue Alert", "Use mosquito repellent and avoid standing water."),
            AwarenessItem("Flu Season Update", "Get vaccinated and wear masks in crowded areas.")
        )
        adapter.submitList(list)
    }
    */
}
