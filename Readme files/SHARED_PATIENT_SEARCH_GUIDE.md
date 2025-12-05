# Shared Patient Search - Offline-First Architecture

## Overview

This document explains the unified patient search system that serves both **Pregnancy/ANC** and **TB modules** in the ASHA Sukhayu app.

## Key Changes

### 1. Expanded Dummy Patient Data

**File:** `app/src/main/java/com/sukhayu/patient/DummyData.kt`

The `getDummyPatients()` function now returns a comprehensive list including:

- **Pregnant Women** (4 patients) - for ANC survey flows
  - Priya Sharma, Sunita Devi, Lakshmi Patel, Meera Gupta
  
- **Adult Men** (6 patients) - for TB screening and treatment follow-up
  - Rajesh Kumar, Amit Singh, Vijay Patil, Suresh Yadav, Ramesh Verma, Mohan Reddy
  
- **Adolescents/Children** (5 patients) - for TB screening in adolescents
  - Rohit Sharma (M), Anjali Desai (F), Karan Patel (M), Pooja Singh (F), Arjun Kumar (M)

**Total:** 15 diverse dummy patients covering all demographics needed for both modules.

### 2. Database Auto-Seeding

**Files:** 
- `app/src/main/java/com/sukhayu/patient/data/local/DatabaseInitializer.kt` (NEW)
- `app/src/main/java/com/sukhayu/patient/MyApp.kt` (UPDATED)

On first app launch, the local Room database is automatically seeded with all 15 dummy patients if the patient table is empty.

```kotlin
// In MyApp.onCreate()
DatabaseInitializer.initialize(this)
```

This ensures:
- ✅ Offline-first operation from the start
- ✅ No need to manually insert dummy data
- ✅ All modules share the same patient source

### 3. Unified Patient Search

**File:** `app/src/main/java/com/sukhayu/patient/data/repository/PatientRepository.kt`

The `searchPatients(query, token)` function is the **single source of truth** for all patient searches:

```kotlin
suspend fun searchPatients(query: String, token: String?): List<PatientEntity>
```

**How it works:**
1. Searches local Room database first (offline-first)
2. If online and token provided, tries to fetch from API
3. Updates local cache with API results
4. Returns results (local or remote)

**Usage in any module:**

```kotlin
val patientRepository = PatientRepository(db, apiService)
val results = patientRepository.searchPatients("Rajesh", token)
```

## For TB Module Developers

### How to Implement TB Patient Search

#### Option 1: Use PatientRepository Directly (Recommended)

```kotlin
class TbScreeningViewModel(
    private val patientRepository: PatientRepository
) : ViewModel() {

    fun searchPatients(query: String, token: String?) {
        viewModelScope.launch {
            try {
                // This searches local DB first (includes dummy data)
                val results = patientRepository.searchPatients(query, token)
                
                // Handle results (same PatientEntity used by pregnancy flows)
                _searchResults.value = results
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
```

#### Option 2: If You Must Use DummyData Directly (Not Recommended)

```kotlin
// Fallback for testing - but prefer repository approach
val dummyResults = DummyData.searchDummyPatients(query)
```

**However,** this bypasses the offline-first architecture. Always prefer `PatientRepository.searchPatients()`.

### Patient Entity Structure

All patient searches return `PatientEntity` objects:

```kotlin
data class PatientEntity(
    val id: String,              // e.g., "DUMMY_M001"
    val name: String,            // e.g., "Rajesh Kumar"
    val phone: String?,          // e.g., "+91-9876543220"
    val gender: String?,         // "Male", "Female"
    val weightKg: Double?,       // e.g., 68.0
    val supremeId: String?,      // e.g., "SUP_M001"
    val lastUpdated: Long        // Timestamp
)
```

### Filtering by Gender (if needed)

```kotlin
// Example: TB screening might want to filter by gender or age range
val allResults = patientRepository.searchPatients(query, token)

// Filter to males only (for adult TB screening)
val malePatients = allResults.filter { it.gender?.equals("Male", ignoreCase = true) == true }

// Note: Age field is not currently in PatientEntity
// If you need age filtering, you'll need to add an `age` or `dateOfBirth` field
```

## Database Schema

**Table:** `patients`

| Column       | Type   | Description                    |
|--------------|--------|--------------------------------|
| id           | String | Primary key (e.g., DUMMY_M001) |
| name         | String | Full name                      |
| phone        | String | Phone number (nullable)        |
| gender       | String | "Male" / "Female" (nullable)   |
| weightKg     | Double | Weight in kg (nullable)        |
| supremeId    | String | Supreme system ID (nullable)   |
| lastUpdated  | Long   | Timestamp                      |

## Important Notes

### DO NOT Create New Dummy Lists

❌ **Wrong:**
```kotlin
// Don't do this in TB module!
val tbDummyPatients = listOf(
    PatientEntity(id = "TB_001", name = "Rahul", ...)
)
```

✅ **Correct:**
```kotlin
// Use the shared repository
val results = patientRepository.searchPatients("Rahul", token)
```

### Migration Path for Existing Pregnancy Code

The existing `PregnancySurveyViewModel` has a `useDummyData` flag. This is now **deprecated** because:

1. Dummy data is automatically seeded into the local DB
2. `patientRepository.searchPatients()` already returns dummy data when offline

**TODO for future refactoring:**
- Remove the `useDummyData` flag
- Always use `patientRepository.searchPatients()`
- Remove direct calls to `DummyData.searchDummyPatients()`

## Testing

### Test Queries to Verify Setup

Try searching for these patients to verify the system works:

1. **Pregnant Women:** "Priya", "Sunita", "Lakshmi", "Meera"
2. **Adult Men:** "Rajesh", "Amit", "Vijay", "Suresh", "Ramesh", "Mohan"
3. **Adolescents:** "Rohit", "Anjali", "Karan", "Pooja", "Arjun"

### Verify Database Seeding

```kotlin
// In any activity or fragment
lifecycleScope.launch {
    val db = AshaLocalDatabase.getInstance(requireContext())
    val count = db.patientDao().getPatientCount()
    Log.d("PatientDB", "Total patients in DB: $count") // Should be 15 after first run
}
```

## File Reference

| File | Purpose |
|------|---------|
| `DummyData.kt` | Expanded dummy patient list (15 patients) |
| `DatabaseInitializer.kt` | Auto-seeds DB on first run |
| `MyApp.kt` | Calls DatabaseInitializer in onCreate() |
| `PatientRepository.kt` | Unified search function with offline-first logic |
| `PatientDao.kt` | Room DAO with search, insert, count queries |
| `PatientEntity.kt` | Data model for all patients |

## Future Enhancements

### Add Age Field (Recommended for TB Module)

Currently, `PatientEntity` doesn't have an age field. To add it:

1. Add to `PatientEntity.kt`:
```kotlin
data class PatientEntity(
    // ...existing fields...
    val ageYears: Int?,  // NEW
)
```

2. Update `DummyData.kt` to include realistic ages:
```kotlin
PatientEntity(
    id = "DUMMY_M001",
    name = "Rajesh Kumar",
    ageYears = 45,  // NEW
    // ...rest...
)
```

3. Increment database version in `AshaLocalDatabase.kt`

### Add Village/Location Field

```kotlin
val village: String?,  // e.g., "Dharavi Village, Mumbai"
```

This would help ASHAs identify patients by location.

---

## Questions?

If you have questions about patient search integration for the TB module, refer to:
- Existing pregnancy flows in `PregnancySurveyViewModel.kt`
- Repository pattern in `PatientRepository.kt`
- This documentation

**Key Principle:** One shared patient source for all modules = simpler, more maintainable code.

