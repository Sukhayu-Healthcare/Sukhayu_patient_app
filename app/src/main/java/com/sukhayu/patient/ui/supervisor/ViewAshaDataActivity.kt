package com.sukhayu.patient.ui.supervisor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.AshaWorker
import com.sukhayu.patient.data.repository.SupervisorRepository
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.VoiceInputHelper
import com.sukhayu.patient.utils.HeaderUtils
import kotlinx.coroutines.launch

class ViewAshaDataActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ashaAdapter: AshaAdapter
    private lateinit var etSearchAsha: EditText
    private lateinit var tvAshaCount: TextView
    private var ashaListFull: List<AshaWorker> = emptyList() // Keep full list for filtering
    private val TAG = "ViewAshaDataActivity"
    private lateinit var voiceHelper: VoiceInputHelper
    private lateinit var repository: SupervisorRepository

    // new: launcher to open detail and receive result
    private lateinit var ashaDetailLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_asha_data)
        HeaderUtils.setupRoleInHeader(this)
        repository = SupervisorRepository(this)

        recyclerView = findViewById(R.id.recyclerViewAsha)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        etSearchAsha = findViewById(R.id.etSearchAsha)
        tvAshaCount = findViewById(R.id.tvAshaCount)

        // register launcher before using
        ashaDetailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data?.getBooleanExtra("ASHA_UPDATED", false) == true) {
                    // refresh list after edits
                    fetchAshaList()
                    Toast.makeText(this, "ASHA profile updated", Toast.LENGTH_SHORT).show()
                }
            }
        }

        ashaAdapter = AshaAdapter(emptyList()) { ashaWorker ->
            navigateToAshaDetails(ashaWorker)
        }
        recyclerView.adapter = ashaAdapter

        setupSearchFilter()
        fetchAshaList()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun setupSearchFilter() {
        etSearchAsha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAshaList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterAshaList(query: String) {
        val filteredList = if (query.isEmpty()) {
            ashaListFull
        } else {
            ashaListFull.filter { asha ->
                asha.asha_name.contains(query, ignoreCase = true) ||
                asha.asha_id.contains(query, ignoreCase = true) ||
                asha.asha_phone.contains(query, ignoreCase = false)
            }
        }
        
        ashaAdapter.updateData(filteredList)
        updateAshaCount(filteredList.size, ashaListFull.size)
        
        if (filteredList.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(this, "No ASHA workers found matching '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAshaCount(displayedCount: Int, totalCount: Int) {
        tvAshaCount.text = if (displayedCount == totalCount) {
            "Total ASHA Workers: $totalCount"
        } else {
            "Showing $displayedCount of $totalCount ASHA Workers"
        }
    }

    private fun navigateToAshaDetails(ashaWorker: AshaWorker) {
        val intent = Intent(this, AshaDetailActivity::class.java).apply {
            putExtra("ASHA_ID", ashaWorker.asha_id)
            putExtra("ASHA_NAME", ashaWorker.asha_name)
            putExtra("ASHA_PHONE", ashaWorker.asha_phone)
            putExtra("VILLAGE", ashaWorker.village)
            putExtra("DISTRICT", ashaWorker.district)
            putExtra("TALUKA", ashaWorker.taluka)
            putExtra("PROFILE_PIC", ashaWorker.profile_pic)
        }
        // use launcher to receive possible "edited" result
        ashaDetailLauncher.launch(intent)
    }

    private fun fetchAshaList() {
        val token = TokenManager.getToken()
        
        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Auth token not found")
            return
        }

        Log.d(TAG, "Fetching ASHA list for supervisor")

        lifecycleScope.launch {
            val result = repository.getAshaWorkers()
            
            result.onSuccess { ashaWorkers ->
                Log.d(TAG, "Retrieved ${ashaWorkers.size} ASHA workers")
                
                ashaListFull = ashaWorkers
                ashaAdapter.updateData(ashaWorkers)
                updateAshaCount(ashaWorkers.size, ashaWorkers.size)
                
                if (ashaWorkers.isEmpty()) {
                    Toast.makeText(this@ViewAshaDataActivity, "No ASHA workers registered yet", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ViewAshaDataActivity, "Loaded ${ashaWorkers.size} ASHA workers", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { error ->
                Log.e(TAG, "Error: ${error.message}", error)
                Toast.makeText(this@ViewAshaDataActivity, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
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
}
