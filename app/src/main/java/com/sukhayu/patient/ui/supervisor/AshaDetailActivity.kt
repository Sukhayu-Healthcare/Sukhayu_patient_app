package com.sukhayu.patient.ui.supervisor

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    private lateinit var voiceHelper: VoiceInputHelper

    private var ashaId: String = ""
    private val TAG = "AshaDetailActivity"

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
    }

    private fun initViews() {
        tvAshaId = findViewById(R.id.tvAshaId)
        tvAshaName = findViewById(R.id.tvAshaName)
        tvAshaPhone = findViewById(R.id.tvAshaPhone)
        tvVillage = findViewById(R.id.tvVillage)
        tvDistrict = findViewById(R.id.tvDistrict)
        tvTaluka = findViewById(R.id.tvTaluka)
        btnEdit = findViewById(R.id.btnEditAsha)
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_asha, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEditAshaName)
        val etVillage = dialogView.findViewById<TextInputEditText>(R.id.etEditVillage)
        val etDistrict = dialogView.findViewById<TextInputEditText>(R.id.etEditDistrict)
        val etTaluka = dialogView.findViewById<TextInputEditText>(R.id.etEditTaluka)

        etName.setText(tvAshaName.text)
        etVillage.setText(tvVillage.text)
        etDistrict.setText(tvDistrict.text)
        etTaluka.setText(tvTaluka.text)

        AlertDialog.Builder(this)
            .setTitle("Edit ASHA Details")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                updateAshaProfile(
                    etName.text.toString(),
                    etVillage.text.toString(),
                    etDistrict.text.toString(),
                    etTaluka.text.toString()
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateAshaProfile(name: String, village: String, district: String, taluka: String) {
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
