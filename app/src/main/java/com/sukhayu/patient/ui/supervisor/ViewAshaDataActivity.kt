package com.sukhayu.patient.ui.supervisor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
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
    private lateinit var tvPageInfo: TextView
    private lateinit var btnPrevPage: Button
    private lateinit var btnNextPage: Button

    // Keep list for current page (filtering is per-page now)
    private var ashaListFull: List<AshaWorker> = emptyList()

    private val TAG = "ViewAshaDataActivity"
    private lateinit var voiceHelper: VoiceInputHelper
    private lateinit var repository: SupervisorRepository

    // Pagination state
    private var currentPage: Int = 1
    private var totalPages: Int = 1
    private val pageSize: Int = 5 // must match backend default, or change both sides
    private var totalAshaCount: Int = 0

    // launcher to open detail and receive result
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
        tvPageInfo = findViewById(R.id.tvPageInfo)
        btnPrevPage = findViewById(R.id.btnPrevPage)
        btnNextPage = findViewById(R.id.btnNextPage)

        // register launcher before using
        ashaDetailLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data?.getBooleanExtra("ASHA_UPDATED", false) == true) {
                        // refresh current page after edits
                        fetchAshaList(currentPage)
                        Toast.makeText(this, "ASHA profile updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        ashaAdapter = AshaAdapter(emptyList()) { ashaWorker ->
            navigateToAshaDetails(ashaWorker)
        }
        recyclerView.adapter = ashaAdapter

        setupSearchFilter()
        setupPaginationButtons()

        fetchAshaList(1)

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)
    }

    private fun setupSearchFilter() {
        etSearchAsha.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterAshaList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupPaginationButtons() {
        btnPrevPage.setOnClickListener {
            if (currentPage > 1) {
                fetchAshaList(currentPage - 1)
            }
        }

        btnNextPage.setOnClickListener {
            if (currentPage < totalPages) {
                fetchAshaList(currentPage + 1)
            }
        }

        updatePaginationUi()
    }

    private fun updatePaginationUi() {
        tvPageInfo.text = "Page $currentPage of $totalPages"
        btnPrevPage.isEnabled = currentPage > 1
        btnNextPage.isEnabled = currentPage < totalPages
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
        updateAshaCount(filteredList.size)

        if (filteredList.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(
                this,
                "No ASHA workers found matching '$query'",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateAshaCount(displayedCount: Int) {
        tvAshaCount.text = if (displayedCount == totalAshaCount || totalAshaCount == 0) {
            "Total ASHA Workers: $totalAshaCount"
        } else {
            "Showing $displayedCount of $totalAshaCount ASHA Workers"
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
        ashaDetailLauncher.launch(intent)
    }

    private fun fetchAshaList(page: Int = 1) {
        val token = TokenManager.getToken()

        if (token.isEmpty()) {
            Toast.makeText(
                this,
                "Session expired. Please login again.",
                Toast.LENGTH_LONG
            ).show()
            Log.e(TAG, "Auth token not found")
            return
        }

        Log.d(TAG, "Fetching ASHA list for supervisor. Page = $page")

        lifecycleScope.launch {
            val result = repository.getAshaWorkers(page, pageSize)

            result.onSuccess { response ->
                Log.d(
                    TAG,
                    "Retrieved page ${response.page}/${response.totalPages}, count=${response.ashas.size}, total=${response.total}"
                )

                currentPage = response.page
                totalPages = if (response.totalPages <= 0) 1 else response.totalPages
                totalAshaCount = response.total

                ashaListFull = response.ashas
                ashaAdapter.updateData(response.ashas)
                updateAshaCount(response.ashas.size)
                updatePaginationUi()

                if (response.ashas.isEmpty()) {
                    Toast.makeText(
                        this@ViewAshaDataActivity,
                        "No ASHA workers found on this page",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ViewAshaDataActivity,
                        "Loaded ${response.ashas.size} ASHA workers",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.onFailure { error ->
                Log.e(TAG, "Error: ${error.message}", error)
                Toast.makeText(
                    this@ViewAshaDataActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceHelper.destroy()
    }
}
