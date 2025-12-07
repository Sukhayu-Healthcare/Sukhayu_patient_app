package com.sukhayu.patient.ui.patient.appointment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

data class Doctor(
    val name: String,
    val phone: String,
    val specialization: String,
    val availableDays: String
)

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DoctorListAdapter
    private val doctors = mutableListOf<Doctor>()
    private val client = OkHttpClient()

    // Add references for the new buttons
    private lateinit var btnCommunityHealthOfficer: View
    private lateinit var btnMedicalOfficer: View

    // Track selected doctor type
    private var selectedType: String = "community_health_officer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        recyclerView = findViewById(R.id.rvDoctorsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DoctorListAdapter(doctors) { phoneNumber ->
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Find buttons
        btnCommunityHealthOfficer = findViewById(R.id.btnCommunityHealthOfficer)
        btnMedicalOfficer = findViewById(R.id.btnMedicalOfficer)

        // Set click listeners for doctor type selection
        btnCommunityHealthOfficer.setOnClickListener {
            selectedType = "community_health_officer"
            fetchDoctors(selectedType)
            highlightSelectedButton()
        }
        btnMedicalOfficer.setOnClickListener {
            selectedType = "medical_officer"
            fetchDoctors(selectedType)
            highlightSelectedButton()
        }

        // Initial fetch for default type
        fetchDoctors(selectedType)
        highlightSelectedButton()
    }

    // Highlight selected button (simple background change)
    private fun highlightSelectedButton() {
        btnCommunityHealthOfficer.isEnabled = selectedType != "community_health_officer"
        btnMedicalOfficer.isEnabled = selectedType != "medical_officer"
        btnCommunityHealthOfficer.alpha = if (selectedType == "community_health_officer") 1f else 0.5f
        btnMedicalOfficer.alpha = if (selectedType == "medical_officer") 1f else 0.5f
    }

    // Add doctorType param to fetchDoctors
    private fun fetchDoctors(doctorType: String) {
        val url = ApiClient.BASE_URL + "patient/doctors?type=$doctorType"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@BookAppointmentActivity, "Failed to load doctors", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { body ->
                    val json = body.string()
                    val jsonArray = JSONArray(json)
                    val doctorList = mutableListOf<Doctor>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        doctorList.add(
                            Doctor(
                                name = obj.optString("name"),
                                phone = obj.optString("phone"),
                                specialization = obj.optString("specialization"),
                                availableDays = obj.optString("available_days")
                            )
                        )
                    }
                    runOnUiThread {
                        doctors.clear()
                        doctors.addAll(doctorList)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}
