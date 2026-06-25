package com.sukhayu.patient.ui.supervisor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.sukhayu.patient.R
import com.sukhayu.patient.utils.HeaderUtils
import android.view.View
import android.widget.AdapterView
import com.sukhayu.patient.utils.TtsHelper
import com.sukhayu.patient.utils.ViewTtsHelper
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sukhayu.patient.utils.LocalizableActivity
import com.sukhayu.utils.VoiceInputHelper

class AshaDetailActivity : LocalizableActivity() {

    private lateinit var ttsHelper: TtsHelper

    private lateinit var voiceHelper: VoiceInputHelper
    // Hierarchical data structure for dropdowns
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asha_detail)

        HeaderUtils.setupRoleInHeader(this)

        // Initialize TTS
        ttsHelper = TtsHelper(this)

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val currentLang = prefs.getString("My_Lang", "en") ?: "en"

        ttsHelper.setLanguage(currentLang)

        // Enable TTS on all TextViews and Buttons
        ViewTtsHelper.attachToAllTextViews(
            findViewById(android.R.id.content),
            ttsHelper
        )

        // Voice input setup
        requestAudioPermission()
        voiceHelper = VoiceInputHelper(this)
        VoiceInputHelper.attachToAllEditTexts(this)

        // bind views
        val tvAshaId = findViewById<TextView>(R.id.tvAshaId)
        val tvAshaName = findViewById<TextView>(R.id.tvAshaName)
        val tvAshaPhone = findViewById<TextView>(R.id.tvAshaPhone)
        val tvVillage = findViewById<TextView>(R.id.tvVillage)
        val tvDistrict = findViewById<TextView>(R.id.tvDistrict)
        val tvTaluka = findViewById<TextView>(R.id.tvTaluka)
        val btnEditAsha = findViewById<Button>(R.id.btnEditAsha)
        val btnDeleteAsha = findViewById<Button>(R.id.btnDeleteAsha)

        // populate from intent extras
        val ashaId = intent.getStringExtra("ASHA_ID") ?: ""
        tvAshaId.text = "ID: $ashaId"
        tvAshaName.text = intent.getStringExtra("ASHA_NAME") ?: ""
        tvAshaPhone.text = intent.getStringExtra("ASHA_PHONE") ?: ""
        tvVillage.text = intent.getStringExtra("VILLAGE") ?: ""
        tvDistrict.text = intent.getStringExtra("DISTRICT") ?: ""
        tvTaluka.text = intent.getStringExtra("TALUKA") ?: ""

        btnEditAsha.setOnClickListener {
            // inflate dialog layout
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_asha, null)

            val etName = dialogView.findViewById<TextInputEditText>(R.id.etEditAshaName)
            val etState = dialogView.findViewById<EditText>(R.id.etEditState)
            val spinnerDistrict = dialogView.findViewById<Spinner>(R.id.spinnerEditDistrict)
            val spinnerTaluka = dialogView.findViewById<Spinner>(R.id.spinnerEditTaluka)
            val spinnerVillage = dialogView.findViewById<Spinner>(R.id.spinnerEditVillage)

            // pre-fill values
            etName?.setText(tvAshaName.text)
            etState?.setText(intent.getStringExtra("STATE") ?: etState?.text?.toString() ?: "Maharashtra")

            val currentDistrict = intent.getStringExtra("DISTRICT") ?: ""
            val currentTaluka = intent.getStringExtra("TALUKA") ?: ""
            val currentVillage = intent.getStringExtra("VILLAGE") ?: ""

            // Prepare district list
            val districts = districtTalukaVillageData.keys.toList()
            val districtAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, districts)
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDistrict.adapter = districtAdapter

            // Set district selection
            val districtIndex = districts.indexOfFirst { it.equals(currentDistrict, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
            spinnerDistrict.setSelection(districtIndex)

            // Helper to update village spinner
            fun updateVillageSpinner(selectedDistrict: String, selectedTaluka: String?, selectedVillage: String? = null) {
                val villageList = selectedTaluka?.let { districtTalukaVillageData[selectedDistrict]?.get(it) } ?: emptyList()
                val villageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, villageList)
                villageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerVillage.adapter = villageAdapter
                val villageIndex = selectedVillage?.let { villageList.indexOfFirst { v -> v.equals(it, ignoreCase = true) } }?.takeIf { it >= 0 } ?: 0
                spinnerVillage.setSelection(villageIndex)
            }

            // Helper to update taluka spinner
            fun updateTalukaSpinner(selectedDistrict: String, selectedTaluka: String? = null) {
                val talukaList = districtTalukaVillageData[selectedDistrict]?.keys?.toList() ?: emptyList()
                val talukaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, talukaList)
                talukaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTaluka.adapter = talukaAdapter
                val talukaIndex = selectedTaluka?.let { talukaList.indexOfFirst { t -> t.equals(it, ignoreCase = true) } }?.takeIf { it >= 0 } ?: 0
                spinnerTaluka.setSelection(talukaIndex)
                updateVillageSpinner(selectedDistrict, talukaList.getOrNull(talukaIndex), currentVillage)
            }

            // Initial taluka/village population
            val selectedDistrict = districts.getOrNull(districtIndex) ?: districts.first()
            updateTalukaSpinner(selectedDistrict, currentTaluka)

            // Cascading listeners
            spinnerDistrict.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                    val selectedDistrict = districts[position]
                    updateTalukaSpinner(selectedDistrict)
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            })

            spinnerTaluka.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                    val selectedDistrict = spinnerDistrict.selectedItem?.toString() ?: ""
                    val talukaList = districtTalukaVillageData[selectedDistrict]?.keys?.toList() ?: emptyList()
                    val selectedTaluka = talukaList.getOrNull(position)
                    updateVillageSpinner(selectedDistrict, selectedTaluka)
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            })

            // build and show dialog
            AlertDialog.Builder(this)
                .setTitle("Edit ASHA")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val newName = etName?.text?.toString()?.trim() ?: ""
                    val newDistrict = spinnerDistrict.selectedItem?.toString() ?: ""
                    val newTaluka = spinnerTaluka.selectedItem?.toString() ?: ""
                    val newVillage = spinnerVillage.selectedItem?.toString() ?: ""

                    // prepare result to notify caller (ViewAshaDataActivity) to refresh
                    val resultIntent = Intent().apply {
                        putExtra("ASHA_UPDATED", true)
                        putExtra("ASHA_ID", ashaId)
                        putExtra("ASHA_NAME", newName)
                        putExtra("ASHA_PHONE", tvAshaPhone.text.toString())
                        putExtra("DISTRICT", newDistrict)
                        putExtra("TALUKA", newTaluka)
                        putExtra("VILLAGE", newVillage)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        supportActionBar?.apply {
            title = "ASHA Details"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun requestAudioPermission() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                200
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
