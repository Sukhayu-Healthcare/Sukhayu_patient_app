package com.sukhayu.patient.ui.supervisor

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhayu.patient.R
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.AshaListResponse
import com.sukhayu.patient.data.remote.AshaWorker
import com.sukhayu.patient.utils.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewAshaDataActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ashaAdapter: AshaAdapter
    private lateinit var etSearchAsha: EditText
    private var ashaListFull: List<AshaWorker> = emptyList() // Keep full list for filtering
    private val TAG = "ViewAshaDataActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_asha_data)

        recyclerView = findViewById(R.id.recyclerViewAsha)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        etSearchAsha = findViewById(R.id.etSearchAsha)

        ashaAdapter = AshaAdapter(emptyList()) { ashaWorker ->
            navigateToAshaDetails(ashaWorker)
        }
        recyclerView.adapter = ashaAdapter

        setupSearchFilter()
        fetchAshaList()
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
        
        if (filteredList.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(this, "No ASHA workers found matching '$query'", Toast.LENGTH_SHORT).show()
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
        startActivity(intent)
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
                    
                    ashaListFull = ashaWorkers // Store full list
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
