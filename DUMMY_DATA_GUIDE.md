# Dummy Data Implementation Guide

## Overview
I've added **temporary dummy data support** to the PregnancySurveyActivity to allow testing when the backend is unavailable. This feature can be easily toggled on/off.

## Files Created/Modified

### 1. DummyData.kt (NEW)
**Location**: `com.sukhayu.patient.DummyData`

**Purpose**: Provides dummy patient data for testing

**Available Functions**:
```kotlin
// Get a single dummy patient
DummyData.getDummyPatient(name: String? = null)

// Get a list of dummy patients
DummyData.getDummyPatients()

// Search dummy patients by name or phone
DummyData.searchDummyPatients(query: String)
```

**Dummy Patients Included**:
1. Priya Sharma - +91-9876543210, Female, 55kg
2. Sunita Devi - +91-9876543211, Female, 60kg
3. Lakshmi Patel - +91-9876543212, Female, 52kg

### 2. PregnancySurveyViewModel.kt (MODIFIED)
**New Properties**:
```kotlin
var useDummyData: Boolean = false  // Toggle between real/dummy data
var isPatientLoaded: Boolean = false  // Track if patient is loaded
```

**Modified Logic**:
- `onLoadPatientClicked()` now checks `useDummyData` flag
- If true → uses `DummyData.searchDummyPatients()`
- If false → uses real `PatientRepository.searchPatients()`
- `selectPatient()` sets `isPatientLoaded = true`
- `onContinueClicked()` validates `isPatientLoaded` flag

### 3. PregnancySurveyActivity.kt (MODIFIED)
**Auto-Detection**:
```kotlin
// Automatically enables dummy data if no auth token available
val token = getAuthToken()
viewModel.useDummyData = token == null
```

## How It Works

### Scenario 1: Dummy Data Mode (Backend Unavailable)
```
User enters "Priya" → Taps "LOAD DETAILS"
    ↓
ViewModel checks: useDummyData = true
    ↓
Searches DummyData.searchDummyPatients("Priya")
    ↓
Finds: Priya Sharma
    ↓
Auto-fills form with:
  Name: Priya Sharma
  Phone: +91-9876543210
  Gender: Female
  Weight: 55.0 kg
    ↓
Sets isPatientLoaded = true ✅
    ↓
User selects survey type → Taps "CONTINUE"
    ↓
Validates: isPatientLoaded = true ✅
    ↓
Navigates to FirstAncVisitActivity/FollowUpAncVisitActivity
```

### Scenario 2: Real Data Mode (Backend Available)
```
User enters patient name → Taps "LOAD DETAILS"
    ↓
ViewModel checks: useDummyData = false
    ↓
Queries PatientRepository (Room + API)
    ↓
Returns real patient data
    ↓
Auto-fills form with real backend data ✅
```

## How to Toggle Dummy Data

### Option 1: Automatic (Current Implementation)
```kotlin
// In PregnancySurveyActivity.initializeViewModel()
val token = getAuthToken()
viewModel.useDummyData = token == null  // Auto-enable if no token
```

### Option 2: Force Enable (For Testing)
```kotlin
// In PregnancySurveyActivity.initializeViewModel()
viewModel.useDummyData = true  // Always use dummy data
```

### Option 3: Force Disable (Production)
```kotlin
// In PregnancySurveyActivity.initializeViewModel()
viewModel.useDummyData = false  // Always use real backend
```

### Option 4: User Toggle (Advanced)
Add a menu item or switch in the UI:
```kotlin
// In PregnancySurveyActivity
override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.pregnancy_survey_menu, menu)
    return true
}

override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_toggle_dummy_data -> {
            viewModel.useDummyData = !viewModel.useDummyData
            Toast.makeText(this, 
                "Dummy data: ${if (viewModel.useDummyData) "ON" else "OFF"}", 
                Toast.LENGTH_SHORT).show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
```

## Testing with Dummy Data

### Test 1: Single Match
1. Type "Priya" in patient name field
2. Tap "LOAD DETAILS"
3. ✅ Should auto-fill: Priya Sharma, +91-9876543210, Female, 55kg

### Test 2: Multiple Matches
1. Type "a" in patient name field
2. Tap "LOAD DETAILS"
3. ✅ Should show chooser with: Priya Sharma, Sunita Devi, Lakshmi Patel

### Test 3: Custom Name
1. Type "Unknown Patient" in patient name field
2. Tap "LOAD DETAILS"
3. ✅ Should create dummy with entered name: Unknown Patient, +91-9876543210, Female, 55kg

### Test 4: Phone Search
1. Type "9876543211" in patient name field
2. Tap "LOAD DETAILS"
3. ✅ Should find: Sunita Devi

### Test 5: Continue Validation
1. Don't load patient details
2. Select survey type
3. Tap "CONTINUE"
4. ✅ Should show error: "Please load patient details first"

### Test 6: Full Flow
1. Load patient details (any name)
2. Select "First ANC Visit"
3. Tap "CONTINUE"
4. ✅ Should navigate to FirstAncVisitActivity with patient data

## ViewBinding Implementation (Clean Code)

Here's a complete example using ViewBinding for reference:

```kotlin
class PregnancySurveyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPregnancySurveyBinding
    private lateinit var viewModel: PregnancySurveyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPregnancySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViewModel()
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Load patient details
        binding.btnLoadDetails.setOnClickListener {
            val query = binding.etPatientName.text.toString().trim()
            val token = getAuthToken()
            viewModel.onLoadPatientClicked(query, token)
        }

        // Continue button
        binding.btnContinue.setOnClickListener {
            viewModel.onContinueClicked()
        }

        // Clear error on focus
        binding.etPatientName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearError()
        }
    }

    private fun observeViewModel() {
        viewModel.patientDetails.observe(this) { patient ->
            if (patient != null) {
                binding.tvPatientName.text = patient.name
                binding.tvPatientPhone.text = patient.phone
                binding.tvPatientGender.text = patient.gender
                binding.tvPatientWeight.text = patient.weight
                binding.patientDetailsCard.visibility = View.VISIBLE
            }
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLoadDetails.isEnabled = false
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
                // ... other states
            }
        }
    }
}
```

## When to Remove Dummy Data

Once the backend is stable and working:

1. **Delete** `DummyData.kt`
2. **Remove** from `PregnancySurveyViewModel.kt`:
   ```kotlin
   // DELETE these lines:
   var useDummyData: Boolean = false
   var isPatientLoaded: Boolean = false
   
   // In onLoadPatientClicked, DELETE the if/else:
   val results = if (useDummyData) { ... } else { ... }
   
   // Replace with direct call:
   val results = patientRepository.searchPatients(query, token)
   ```

3. **Remove** from `PregnancySurveyActivity.kt`:
   ```kotlin
   // DELETE these lines:
   viewModel.useDummyData = token == null
   ```

## Summary

✅ **Dummy data support added** for testing without backend
✅ **Auto-detection** based on auth token availability
✅ **Clean architecture** maintained (MVVM pattern)
✅ **Easy to toggle** on/off
✅ **Easy to remove** when backend is ready
✅ **isPatientLoaded flag** tracks patient load state
✅ **Works with both** single and multiple patient selection
✅ **Full validation** before navigation

The app now supports BOTH real backend data AND dummy data for testing! 🎉

