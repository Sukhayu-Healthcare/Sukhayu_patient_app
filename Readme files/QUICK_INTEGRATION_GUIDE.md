# Quick Integration Guide - Connect FollowUpAncVisitActivity to Room Database

## Prerequisites
✅ AncVisitEntity.kt created
✅ AncVisitDao.kt created  
✅ AshaLocalDatabase.kt updated (version 5)
✅ AncVisitFormMapper.kt created (helper)

## Step-by-Step Integration

### Step 1: Create Repository (5 minutes)

Create file: `AncVisitRepository.kt` in `com.sukhayu.patient.data.repository`

```kotlin
package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.AncVisitDao
import com.sukhayu.patient.data.local.entity.AncVisitEntity

class AncVisitRepository(private val dao: AncVisitDao) {
    
    suspend fun saveVisit(entity: AncVisitEntity) {
        dao.upsertVisit(entity)
    }
    
    suspend fun getVisitsForPregnancy(pregnancyId: String): List<AncVisitEntity> {
        return dao.getVisitsForPregnancy(pregnancyId)
    }
    
    suspend fun getUnsyncedVisits(): List<AncVisitEntity> {
        return dao.getUnsyncedVisits()
    }
}
```

---

### Step 2: Create ViewModel (10 minutes)

Create file: `FollowUpAncVisitViewModel.kt` in `com.sukhayu.patient.asha.ui.surveys.pregnancy`

```kotlin
package com.sukhayu.patient.asha.ui.surveys.pregnancy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sukhayu.patient.data.local.entity.AncVisitEntity
import com.sukhayu.patient.data.repository.AncVisitRepository
import kotlinx.coroutines.launch

class FollowUpAncVisitViewModel(
    private val repository: AncVisitRepository
) : ViewModel() {
    
    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving
    
    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun saveVisit(entity: AncVisitEntity) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                repository.saveVisit(entity)
                _saveSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save visit: ${e.message}"
                _saveSuccess.value = false
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun loadVisitsForPregnancy(pregnancyId: String) {
        viewModelScope.launch {
            try {
                val visits = repository.getVisitsForPregnancy(pregnancyId)
                // Handle visits list if needed
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load visits: ${e.message}"
            }
        }
    }
}
```

---

### Step 3: Create ViewModelFactory (5 minutes)

Create file: `FollowUpAncVisitViewModelFactory.kt` in same package

```kotlin
package com.sukhayu.patient.asha.ui.surveys.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.repository.AncVisitRepository

class FollowUpAncVisitViewModelFactory(
    private val repository: AncVisitRepository
) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FollowUpAncVisitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FollowUpAncVisitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

---

### Step 4: Update FollowUpAncVisitActivity (15 minutes)

**Add imports:**
```kotlin
import androidx.lifecycle.ViewModelProvider
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.patient.data.repository.AncVisitRepository
```

**Add ViewModel property:**
```kotlin
private lateinit var viewModel: FollowUpAncVisitViewModel
```

**In onCreate(), initialize ViewModel (BEFORE setupSaveButton()):**
```kotlin
// Initialize ViewModel
val dao = AshaLocalDatabase.getInstance(this).ancVisitDao()
val repository = AncVisitRepository(dao)
val factory = FollowUpAncVisitViewModelFactory(repository)
viewModel = ViewModelProvider(this, factory)[FollowUpAncVisitViewModel::class.java]

// Observe ViewModel
observeViewModel()
```

**Add observeViewModel() method:**
```kotlin
private fun observeViewModel() {
    viewModel.isSaving.observe(this) { saving ->
        binding.btnSaveFollowUpAnc.isEnabled = !saving
    }
    
    viewModel.saveSuccess.observe(this) { success ->
        if (success == true) {
            Toast.makeText(
                this, 
                "Follow-up ANC Visit saved successfully", 
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }
    
    viewModel.errorMessage.observe(this) { msg ->
        msg?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }
    }
}
```

**Replace saveFollowUpAncVisit() method:**
```kotlin
private fun saveFollowUpAncVisit() {
    // Check if we have a pregnancy ID
    if (pregnancyId.isNullOrBlank()) {
        Toast.makeText(this, "Error: No pregnancy ID available", Toast.LENGTH_LONG).show()
        return
    }
    
    // Build entity from form using helper
    val entity = AncVisitFormMapper.buildEntityFromForm(binding, pregnancyId!!)
    
    // Save via ViewModel
    viewModel.saveVisit(entity)
}
```

---

### Step 5: Handle Pregnancy ID (IMPORTANT!)

**Option A: Pass from PregnancySurveyActivity**

Update `PregnancySurveyActivity.kt`:

```kotlin
private fun navigateToFollowUpAncVisit(patientId: String, patientName: String) {
    // Get current patient details from ViewModel
    val patient = viewModel.patientDetails.value
    
    // TODO: Get actual pregnancy ID from database
    // For now, you need to query PregnancyEntity for this patient
    lifecycleScope.launch {
        val pregnancies = AshaLocalDatabase.getInstance(this@PregnancySurveyActivity)
            .pregnancyDao()
            .getPregnanciesForWoman(patientId)
        
        val currentPregnancy = pregnancies.firstOrNull()
        
        val intent = Intent(this@PregnancySurveyActivity, FollowUpAncVisitActivity::class.java).apply {
            putExtra(FollowUpAncVisitActivity.EXTRA_PREGNANCY_ID, currentPregnancy?.id)
            putExtra(FollowUpAncVisitActivity.EXTRA_PATIENT_ID, patientId)
            putExtra(FollowUpAncVisitActivity.EXTRA_PATIENT_NAME, patientName)
            patient?.let {
                putExtra(FollowUpAncVisitActivity.EXTRA_PATIENT_PHONE, it.phone)
                putExtra(FollowUpAncVisitActivity.EXTRA_PATIENT_GENDER, it.gender)
                putExtra(FollowUpAncVisitActivity.EXTRA_PATIENT_WEIGHT, it.weight)
            }
        }
        startActivity(intent)
    }
}
```

**Option B: Query in FollowUpAncVisitActivity**

In `FollowUpAncVisitActivity.onCreate()`:

```kotlin
// If pregnancy ID not passed, query for it
if (pregnancyId.isNullOrBlank() && patientId != null) {
    lifecycleScope.launch {
        val pregnancies = AshaLocalDatabase.getInstance(this@FollowUpAncVisitActivity)
            .pregnancyDao()
            .getPregnanciesForWoman(patientId!!)
        
        pregnancyId = pregnancies.firstOrNull()?.id
        
        if (pregnancyId == null) {
            Toast.makeText(
                this@FollowUpAncVisitActivity,
                "Error: No pregnancy found for this patient. Please create First ANC Visit first.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }
}
```

Add import:
```kotlin
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
```

---

## Testing the Integration

### Test 1: Save a Visit
1. Run the app
2. Navigate to Pregnancy Survey
3. Load a patient
4. Select "Follow-up ANC Visit"
5. Fill the form with valid data
6. Tap "Save Follow-up Visit"
7. ✅ Should see success toast and activity closes

### Test 2: Verify in Database
Use Database Inspector in Android Studio:
1. View → Tool Windows → App Inspection
2. Select "Database Inspector" tab
3. Select your app process
4. Look for `anc_visits` table
5. ✅ Should see your saved record

### Test 3: Check Logcat
Add logging to verify:
```kotlin
private fun saveFollowUpAncVisit() {
    if (pregnancyId.isNullOrBlank()) {
        Toast.makeText(this, "Error: No pregnancy ID available", Toast.LENGTH_LONG).show()
        return
    }
    
    val entity = AncVisitFormMapper.buildEntityFromForm(binding, pregnancyId!!)
    
    // Log for debugging
    android.util.Log.d("ANC_VISIT", "Saving visit: ${entity.id}")
    android.util.Log.d("ANC_VISIT", "Pregnancy: ${entity.pregnancyId}")
    android.util.Log.d("ANC_VISIT", "Visit #: ${entity.visitNumber}")
    
    viewModel.saveVisit(entity)
}
```

---

## Common Issues & Solutions

### Issue 1: "No pregnancy ID available"
**Cause:** Pregnancy ID not passed or not found
**Solution:** Use Option B in Step 5 to query pregnancy

### Issue 2: "Function 'ancVisitDao' is never used" warning
**Cause:** IDE hasn't updated
**Solution:** Sync Gradle, rebuild project, restart IDE

### Issue 3: Database version conflict
**Cause:** Old version cached
**Solution:** Uninstall app and reinstall (destructive migration will recreate DB)

### Issue 4: Save doesn't work
**Cause:** ViewModel not initialized
**Solution:** Ensure ViewModel initialized BEFORE setupSaveButton() in onCreate()

---

## Verification Checklist

Before testing:
- [ ] Repository class created
- [ ] ViewModel class created
- [ ] ViewModelFactory class created
- [ ] Activity updated with ViewModel initialization
- [ ] observeViewModel() method added
- [ ] saveFollowUpAncVisit() method updated to use ViewModel
- [ ] Pregnancy ID handling implemented
- [ ] Imports added (lifecycleScope, ViewModelProvider, etc.)
- [ ] Gradle sync completed
- [ ] No compile errors

After testing:
- [ ] Form saves successfully
- [ ] Success toast appears
- [ ] Activity closes after save
- [ ] Data visible in Database Inspector
- [ ] Multiple visits can be saved for same pregnancy
- [ ] Visit number increments correctly

---

## Next Steps After Integration

1. **List Previous Visits**
   - Create a RecyclerView to show all visits for a pregnancy
   - Use `getVisitsForPregnancy(pregnancyId)`

2. **Edit Existing Visits**
   - Pass visit ID to activity
   - Load entity and populate form using `AncVisitFormMapper.populateFormFromEntity()`

3. **Background Sync**
   - Create WorkManager task
   - Query `getUnsyncedVisits()`
   - Sync to backend API
   - Update `isSynced = true` on success

4. **Visit History Screen**
   - Show timeline of all ANC visits
   - Display key metrics (BP trends, weight changes)
   - Flag high-risk visits

---

## Estimated Time: 35-45 minutes

- Step 1: 5 min
- Step 2: 10 min
- Step 3: 5 min
- Step 4: 15 min
- Step 5: 5-10 min
- Testing: 10 min

**Total:** ~45 minutes to full database integration!

