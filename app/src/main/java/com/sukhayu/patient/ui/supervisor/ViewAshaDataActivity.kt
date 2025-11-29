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

        Log.d(TAG, "Fetching ASHA list for supervisor")
        Log.d(TAG, "Token: Bearer ${token.take(20)}...")

        val call = ApiClient.retrofit.getAshaList("Bearer $token")
        Log.d(TAG, "Full request URL: ${call.request().url}")
        Log.d(TAG, "Request method: ${call.request().method}")
        
        call.enqueue(object : Callback<AshaListResponse> {
            override fun onResponse(call: Call<AshaListResponse>, response: Response<AshaListResponse>) {
                Log.d(TAG, "Response code: ${response.code()}")
                Log.d(TAG, "Response message: ${response.message()}")
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d(TAG, "Response body: $responseBody")
                    
                    val ashaWorkers = responseBody?.ashas ?: emptyList()
                    Log.d(TAG, "Retrieved ${ashaWorkers.size} ASHA workers")
                    ashaAdapter.updateData(ashaWorkers)
                    
                    if (ashaWorkers.isEmpty()) {
                        Toast.makeText(this@ViewAshaDataActivity, "No ASHA workers registered yet", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ViewAshaDataActivity, "Loaded ${ashaWorkers.size} ASHA workers", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to fetch: ${response.code()}")
                    Log.e(TAG, "Error message: ${response.message()}")
                    Log.e(TAG, "Error body: $errorBody")
                    
                    val errorMessage = when (response.code()) {
                        401 -> "Unauthorized. Please login again."
                        403 -> "Access forbidden. Only supervisors can view ASHA workers."
                        404 -> "Supervisor profile not found."
                        500 -> "Server error. Please try again later."
                        else -> "Failed to load data: ${response.message()}"
                    }
                    Toast.makeText(this@ViewAshaDataActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AshaListResponse>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@ViewAshaDataActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
