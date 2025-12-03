package com.sukhayu.patient.ui.asha.family

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.local.AshaLocalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FamilyListActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var rvFamilies: RecyclerView
    private lateinit var adapter: FamilyListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family_list)

        // Set up toolbar
        supportActionBar?.apply {
            title = "Family List"
            setDisplayHomeAsUpEnabled(true)
        }

        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        rvFamilies = findViewById(R.id.rvFamilies)

        adapter = FamilyListAdapter { supremeId ->
            // Open MemberListActivity
            val intent = android.content.Intent(this, MemberListActivity::class.java)
            intent.putExtra("SUPREME_ID", supremeId)
            startActivity(intent)
        }

        rvFamilies.layoutManager = LinearLayoutManager(this)
        rvFamilies.adapter = adapter

        loadFamilies()
    }

    private fun loadFamilies() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE

            val families = withContext(Dispatchers.IO) {
                val dao = AshaLocalDatabase.getInstance(applicationContext).patientDao()
                val allPatients = dao.getAllPatients()

                // Group patients by supremeId
                val grouped = allPatients.groupBy { it.supremeId ?: it.id }

                // Convert to FamilyGroup objects
                grouped.map { (supremeId, members) ->
                    // Find family head (patient whose id equals supremeId)
                    val head = members.find { it.id == supremeId } ?: members.first()

                    // Extract surname from head's name (last word)
                    val surname = head.name.trim().split(" ").lastOrNull() ?: "Unknown"

                    FamilyGroup(
                        supremeId = supremeId,
                        familySurname = surname,
                        familyHead = head.name,
                        phone = head.phone ?: "N/A",
                        memberCount = members.size
                    )
                }.sortedBy { it.familySurname }
            }

            progressBar.visibility = View.GONE
            adapter.submitList(families)
            tvEmpty.visibility = if (families.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class FamilyGroup(
    val supremeId: String,
    val familySurname: String,
    val familyHead: String,
    val phone: String,
    val memberCount: Int
)

