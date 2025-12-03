package com.sukhayu.patient.ui.supervisor

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.UpdateAshaRequest
import com.sukhayu.patient.data.remote.UpdateAshaResponse
import com.sukhayu.patient.utils.TokenManager
import com.sukhayu.utils.VoiceInputHelper
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AshaDetailActivity : AppCompatActivity() {

    private lateinit var tvAshaId: TextView
    private lateinit var tvAshaName: TextView
    private lateinit var tvAshaPhone: TextView
    private lateinit var tvVillage: TextView
    private lateinit var tvDistrict: TextView
    private lateinit var tvTaluka: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private lateinit var voiceHelper: VoiceInputHelper

    private var ashaId: String = ""
    private val TAG = "AshaDetailActivity"

    // Hierarchical data structure
    private val districtTalukaVillageData = mapOf(
        "Nagpur" to mapOf(
            "Nagpur Rural" to listOf("Kalmeshwar", "Mouda", "Parseoni", "Narkhed", "Katol"),
            "Nagpur Urban" to listOf("Nagpur City", "Kamptee", "Hingna", "Umred"),
            "Ramtek" to listOf("Ramtek", "Mansar", "Saoner"),
            "Umred" to listOf("Umred", "Bhiwapur", "Kuhi")
        ),
        "Thane" to mapOf(
            "Thane" to listOf("Thane", "Kalyan", "Dombivli", "Bhiwandi", "Ambernath"),
            "Kalyan" to listOf("Kalyan", "Ulhasnagar", "Shahad", "Ambivli"),
            "Bhiwandi" to listOf("Bhiwandi", "Wada", "Vasai", "Virar"),
            "Murbad" to listOf("Murbad", "Karjat", "Khopoli")
        ),
        "Raigad" to mapOf(
            "Alibag" to listOf("Alibag", "Mandwa", "Rewas", "Nagothane"),
            "Panvel" to listOf("Panvel", "Uran", "Karjat", "Khopoli"),
            "Pen" to listOf("Pen", "Roha", "Sudhagad"),
            "Murud" to listOf("Murud", "Shrivardhan", "Mhasla")
        )
    )

    private var selectedEditDistrict: String = ""
    private var selectedEditTaluka: String = ""
    private var selectedEditVillage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_detail)

        initViews()
        loadAshaData()

        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        btnEdit.setOnClickListener {
            showEditDialog()
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun initViews() {
        tvAshaId = findViewById(R.id.tvAshaId)
        tvAshaName = findViewById(R.id.tvAshaName)
        tvAshaPhone = findViewById(R.id.tvAshaPhone)
        tvVillage = findViewById(R.id.tvVillage)
        tvDistrict = findViewById(R.id.tvDistrict)
        tvTaluka = findViewById(R.id.tvTaluka)
        btnEdit = findViewById(R.id.btnEditAsha)
        btnDelete = findViewById(R.id.btnDeleteAsha)
    }

    private fun loadAshaData() {
        ashaId = intent.getStringExtra("ASHA_ID") ?: ""
        tvAshaId.text = "ID: $ashaId"
        tvAshaName.text = intent.getStringExtra("ASHA_NAME") ?: ""
        tvAshaPhone.text = intent.getStringExtra("ASHA_PHONE") ?: ""
        tvVillage.text = intent.getStringExtra("VILLAGE") ?: ""
        tvDistrict.text = intent.getStringExtra("DISTRICT") ?: ""
        tvTaluka.text = intent.getStringExtra("TALUKA") ?: ""
    }

    private fun showEditDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_asha, null)

        val etEditAshaName = dialogView.findViewById<TextInputEditText>(R.id.etEditAshaName)
        val spinnerEditDistrict = dialogView.findViewById<Spinner>(R.id.spinnerEditDistrict)
        val spinnerEditTaluka = dialogView.findViewById<Spinner>(R.id.spinnerEditTaluka)
        val spinnerEditVillage = dialogView.findViewById<Spinner>(R.id.spinnerEditVillage)

        // Pre-fill current values
        val currentName = intent.getStringExtra("ASHA_NAME") ?: ""
        val currentDistrict = intent.getStringExtra("DISTRICT") ?: ""
        val currentTaluka = intent.getStringExtra("TALUKA") ?: ""
        val currentVillage = intent.getStringExtra("VILLAGE") ?: ""

        etEditAshaName.setText(currentName)

        // Setup District Spinner
        setupEditDistrictSpinner(spinnerEditDistrict, spinnerEditTaluka, spinnerEditVillage, currentDistrict, currentTaluka, currentVillage)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit ASHA Details")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etEditAshaName.text.toString().trim()

                if (validateEditInputs(newName)) {
                    updateAshaDetails(newName, selectedEditDistrict, selectedEditTaluka, selectedEditVillage)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun setupEditDistrictSpinner(
        spinnerDistrict: Spinner,
        spinnerTaluka: Spinner,
        spinnerVillage: Spinner,
        currentDistrict: String,
        currentTaluka: String,
        currentVillage: String
    ) {
        val districts = districtTalukaVillageData.keys.toList()
        val districtAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, districts)
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistrict.adapter = districtAdapter

        // Set current district
        val districtPosition = districts.indexOf(currentDistrict)
        if (districtPosition >= 0) {
            spinnerDistrict.setSelection(districtPosition)
            selectedEditDistrict = currentDistrict
        }

        spinnerDistrict.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedEditDistrict = districts[position]
                setupEditTalukaSpinner(spinnerTaluka, spinnerVillage, selectedEditDistrict, currentTaluka, currentVillage)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedEditDistrict = ""
            }
        }
    }

    private fun setupEditTalukaSpinner(
        spinnerTaluka: Spinner,
        spinnerVillage: Spinner,
        district: String,
        currentTaluka: String,
        currentVillage: String
    ) {
        val talukas = districtTalukaVillageData[district]?.keys?.toList() ?: emptyList()
        val talukaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, talukas)
        talukaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTaluka.adapter = talukaAdapter
        spinnerTaluka.isEnabled = talukas.isNotEmpty()

        // Set current taluka if it exists in the new district
        val talukaPosition = talukas.indexOf(currentTaluka)
        if (talukaPosition >= 0) {
            spinnerTaluka.setSelection(talukaPosition)
            selectedEditTaluka = currentTaluka
        } else if (talukas.isNotEmpty()) {
            selectedEditTaluka = talukas[0]
        }

        spinnerTaluka.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedEditTaluka = talukas[position]
                setupEditVillageSpinner(spinnerVillage, district, selectedEditTaluka, currentVillage)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedEditTaluka = ""
            }
        }
    }

    private fun setupEditVillageSpinner(
        spinnerVillage: Spinner,
        district: String,
        taluka: String,
        currentVillage: String
    ) {
        val villages = districtTalukaVillageData[district]?.get(taluka) ?: emptyList()
        val villageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, villages)
        villageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerVillage.adapter = villageAdapter
        spinnerVillage.isEnabled = villages.isNotEmpty()

        // Set current village if it exists in the new taluka
        val villagePosition = villages.indexOf(currentVillage)
        if (villagePosition >= 0) {
            spinnerVillage.setSelection(villagePosition)
            selectedEditVillage = currentVillage
        } else if (villages.isNotEmpty()) {
            selectedEditVillage = villages[0]
        }

        spinnerVillage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedEditVillage = villages[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedEditVillage = ""
            }
        }
    }

    private fun validateEditInputs(name: String): Boolean {
        return when {
            name.isEmpty() -> {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                false
            }
            selectedEditDistrict.isEmpty() -> {
                Toast.makeText(this, "Please select a district", Toast.LENGTH_SHORT).show()
                false
            }
            selectedEditTaluka.isEmpty() -> {
                Toast.makeText(this, "Please select a taluka", Toast.LENGTH_SHORT).show()
                false
            }
            selectedEditVillage.isEmpty() -> {
                Toast.makeText(this, "Please select a village", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun updateAshaDetails(name: String, district: String, taluka: String, village: String) {
        val token = TokenManager.getToken()

        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = UpdateAshaRequest(
            asha_name = name.ifEmpty { null },
            asha_village = village.ifEmpty { null },
            asha_district = district.ifEmpty { null },
            asha_taluka = taluka.ifEmpty { null },
            supervisor_id = null
        )

        ApiClient.retrofit.updateAsha("Bearer $token", ashaId, updateRequest)
            .enqueue(object : Callback<UpdateAshaResponse> {
                override fun onResponse(call: Call<UpdateAshaResponse>, response: Response<UpdateAshaResponse>) {
                    if (response.isSuccessful) {
                        val updatedAsha = response.body()?.updatedAsha
                        if (updatedAsha != null) {
                            tvAshaName.text = updatedAsha.asha_name
                            tvVillage.text = updatedAsha.village
                            tvDistrict.text = updatedAsha.district
                            tvTaluka.text = updatedAsha.taluka
                            Toast.makeText(this@AshaDetailActivity, "Updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e(TAG, "Update failed: ${response.errorBody()?.string()}")
                        Toast.makeText(this@AshaDetailActivity, "Failed to update", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UpdateAshaResponse>, t: Throwable) {
                    Log.e(TAG, "Network error: ${t.message}", t)
                    Toast.makeText(this@AshaDetailActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDeleteConfirmationDialog() {
        val ashaName = intent.getStringExtra("ASHA_NAME") ?: "this ASHA worker"
        
        AlertDialog.Builder(this)
            .setTitle("Delete ASHA Account")
            .setMessage("Are you sure you want to permanently delete $ashaName's account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAshaAccount()
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteAshaAccount() {
        val token = TokenManager.getToken()

        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Deleting ASHA account: $ashaId")

        ApiClient.retrofit.deleteAsha("Bearer $token", ashaId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "ASHA account deleted successfully")
                        Toast.makeText(this@AshaDetailActivity, "ASHA account deleted successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close activity and return to previous screen
                    } else {
                        Log.e(TAG, "Delete failed: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(this@AshaDetailActivity, "Failed to delete account", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e(TAG, "Network error while deleting: ${t.message}", t)
                    Toast.makeText(this@AshaDetailActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
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
