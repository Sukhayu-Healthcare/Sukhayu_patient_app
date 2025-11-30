# Quick Reference: TB Patient Search Integration

## 🚀 Quick Start (Copy & Paste)

### Step 1: Create TB ViewModel

```kotlin
package com.sukhayu.patient.tb.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.repository.PatientRepository
import kotlinx.coroutines.launch

class TbPatientSearchViewModel(
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<PatientEntity>>()
    val searchResults: LiveData<List<PatientEntity>> = _searchResults

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun searchPatients(query: String, token: String? = null) {
        if (query.isBlank()) {
            _error.value = "Please enter patient name or ID"
            return
        }

        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                
                // This searches the local DB (which includes all 15 dummy patients)
                val results = patientRepository.searchPatients(query, token)
                
                // Optional: Filter by gender if needed for TB screening
                // val maleResults = results.filter { it.gender == "Male" }
                
                _searchResults.value = results
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
```

### Step 2: Create TB Search Activity/Fragment

```kotlin
package com.sukhayu.patient.tb.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.remote.ApiService
import com.sukhayu.patient.data.repository.PatientRepository
import com.sukhayu.patient.databinding.ActivityTbPatientSearchBinding
import com.sukhayu.patient.ui.asha.search.PatientListAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TbPatientSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTbPatientSearchBinding
    private lateinit var viewModel: TbPatientSearchViewModel
    private lateinit var adapter: PatientListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTbPatientSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewModel
        val db = AshaLocalDatabase.getInstance(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://your-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        val repository = PatientRepository(db, apiService)
        
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return TbPatientSearchViewModel(repository) as T
                }
            }
        ).get(TbPatientSearchViewModel::class.java)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup RecyclerView
        adapter = PatientListAdapter { patient ->
            // Handle patient selection
            onPatientSelected(patient)
        }
        binding.recyclerViewPatients.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPatients.adapter = adapter

        // Setup search button
        binding.buttonSearch.setOnClickListener {
            val query = binding.editTextSearch.text.toString()
            val token = getAuthToken() // Implement this based on your auth system
            viewModel.searchPatients(query, token)
        }
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(this) { patients ->
            adapter.submitList(patients)
        }

        viewModel.loading.observe(this) { isLoading ->
            // Show/hide loading indicator
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun onPatientSelected(patient: PatientEntity) {
        // Navigate to TB screening form with selected patient
        // Example:
        // val intent = Intent(this, TbScreeningFormActivity::class.java)
        // intent.putExtra("PATIENT_ID", patient.id)
        // startActivity(intent)
    }

    private fun getAuthToken(): String? {
        // Return auth token from SharedPreferences or session manager
        // Example:
        // val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        // return prefs.getString("token", null)
        return null
    }
}
```

### Step 3: Create Layout (activity_tb_patient_search.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="TB Patient Search"
        android:textSize="24sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp" />

    <EditText
        android:id="@+id/editTextSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Enter patient name or ID"
        android:inputType="text" />

    <Button
        android:id="@+id/buttonSearch"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Search"
        android:layout_marginTop="8dp" />

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:layout_marginTop="16dp"
        android:visibility="gone" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerViewPatients"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:layout_marginTop="16dp" />

</LinearLayout>
```

---

## 🔍 Available Test Patients

### Adult Men (for TB screening/treatment)
- Rajesh Kumar - `+91-9876543220`
- Amit Singh - `+91-9876543221`
- Vijay Patil - `+91-9876543222`
- Suresh Yadav - `+91-9876543223`
- Ramesh Verma - `+91-9876543224`
- Mohan Reddy - `+91-9876543225`

### Adolescents (for TB screening)
- Rohit Sharma (M) - `+91-9876543230`
- Anjali Desai (F) - `+91-9876543231`
- Karan Patel (M) - `+91-9876543232`
- Pooja Singh (F) - `+91-9876543233`
- Arjun Kumar (M) - `+91-9876543234`

### Women (for ANC, but can also have TB)
- Priya Sharma - `+91-9876543210`
- Sunita Devi - `+91-9876543211`
- Lakshmi Patel - `+91-9876543212`
- Meera Gupta - `+91-9876543213`

---

## 📋 Key Points

✅ **PatientRepository.searchPatients()** is your friend - use it everywhere  
✅ **Offline-first** - works without network  
✅ **No new dummy lists** - all patients are already in the DB  
✅ **Same PatientEntity** - reuse existing data model  
✅ **Reuse PatientListAdapter** - already exists in the codebase  

---

## 🎯 Testing Queries

Try these to verify it works:

```kotlin
viewModel.searchPatients("Rajesh", null)    // Male adult
viewModel.searchPatients("Rohit", null)     // Adolescent male
viewModel.searchPatients("Priya", null)     // Female (ANC patient)
viewModel.searchPatients("9876543220", null) // Search by phone
```

---

## ⚠️ Common Mistakes to Avoid

❌ **DON'T** create a new dummy list:
```kotlin
// WRONG!
val tbPatients = listOf(PatientEntity(...))
```

✅ **DO** use the repository:
```kotlin
// CORRECT!
val results = patientRepository.searchPatients(query, token)
```

❌ **DON'T** access DummyData directly:
```kotlin
// WRONG!
val results = DummyData.searchDummyPatients(query)
```

✅ **DO** use the repository (it's offline-first):
```kotlin
// CORRECT!
val results = patientRepository.searchPatients(query, token)
```

---

## 📚 Full Documentation

See `SHARED_PATIENT_SEARCH_GUIDE.md` for:
- Detailed architecture explanation
- Database schema
- Future enhancements (age field, village field)
- More examples

---

**Ready to code? Just copy the ViewModel and Activity code above!** 🚀

