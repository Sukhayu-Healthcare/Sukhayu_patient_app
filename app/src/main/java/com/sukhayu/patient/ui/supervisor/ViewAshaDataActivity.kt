package com.sukhayu.patient.ui.supervisor

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.AshaListResponse
import com.sukhayu.patient.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewAshaDataActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ashaAdapter: AshaAdapter
    private val TAG = "ViewAshaDataActivity"
    private val SUPERVISOR_ID = "SUP001" // Fixed supervisor ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_asha_data)

        recyclerView = findViewById(R.id.recyclerViewAsha)
        recyclerView.layoutManager = LinearLayoutManager(this)

        ashaAdapter = AshaAdapter(emptyList())
        recyclerView.adapter = ashaAdapter

        fetchAshaList()
    }

    private fun fetchAshaList() {
        val token = TokenManager.getToken()
        
        if (token.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Auth token not found")
            return
        }

        Log.d(TAG, "Fetching ASHA list for supervisor: $SUPERVISOR_ID with Bearer token")

        ApiClient.retrofit.getAshaList("Bearer $token", SUPERVISOR_ID).enqueue(object : Callback<AshaListResponse> {
            override fun onResponse(call: Call<AshaListResponse>, response: Response<AshaListResponse>) {
                if (response.isSuccessful) {
                    val ashaWorkers = response.body()?.ashaWorkers ?: emptyList()
                    Log.d(TAG, "Retrieved ${ashaWorkers.size} ASHA workers")
                    ashaAdapter.updateData(ashaWorkers)
                    
                    if (ashaWorkers.isEmpty()) {
                        Toast.makeText(this@ViewAshaDataActivity, "No ASHA workers registered yet", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ViewAshaDataActivity, "Loaded ${ashaWorkers.size} ASHA workers", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Failed to fetch: ${response.code()} - ${response.message()}")
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error body: $errorBody")
                    Toast.makeText(this@ViewAshaDataActivity, "Failed to load data: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AshaListResponse>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@ViewAshaDataActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
