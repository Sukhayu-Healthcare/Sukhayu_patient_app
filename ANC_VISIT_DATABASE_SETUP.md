# ANC Visit Room Database - Implementation Summary

## ✅ Completed Files

### 1. AncVisitEntity.kt
**Location:** `com.sukhayu.patient.data.local.entity.AncVisitEntity`

**Table Name:** `anc_visits`

**Schema:**
```kotlin
@Entity(tableName = "anc_visits")
data class AncVisitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    
    // Required fields
    val pregnancyId: String,        // FK to PregnancyEntity
    val visitNumber: Int,           // 2, 3, 4, ...
    val visitDate: String,          // dd/MM/yyyy format
    val facilityType: String,       // GOVT / PRIVATE / HOME
    
    // Current condition
    val symptomsToday: String?,     // Comma-separated symptoms
    val bpSystolic: Int?,
    val bpDiastolic: Int?,
    val weightKg: Float?,
    
    // Interventions
    val ifaTabletsGiven: Int?,
    val calciumTabletsGiven: Int?,
    val ttDose: String?,            // NONE / FIRST / SECOND / BOOSTER
    
    // Referral
    val referred: Boolean = false,
    val referralReason: String?,
    
    // Next visit
    val nextVisitDate: String?,     // Optional, dd/MM/yyyy format
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
```

**Features:**
- ✅ UUID-based primary key generation
- ✅ Foreign key reference to PregnancyEntity via `pregnancyId`
- ✅ All nullable fields properly marked
- ✅ Offline-first sync tracking with `isSynced` flag
- ✅ Timestamp tracking for audit trail

---

### 2. AncVisitDao.kt
**Location:** `com.sukhayu.patient.data.local.dao.AncVisitDao`

**Operations:**

```kotlin
@Dao
interface AncVisitDao {
    
    // Insert or update
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVisit(entity: AncVisitEntity)
    
    // Query visits for a pregnancy
    @Query("SELECT * FROM anc_visits WHERE pregnancyId = :pregnancyId ORDER BY visitDate DESC")
    suspend fun getVisitsForPregnancy(pregnancyId: String): List<AncVisitEntity>
    
    // Get unsynced records
    @Query("SELECT * FROM anc_visits WHERE isSynced = 0")
    suspend fun getUnsyncedVisits(): List<AncVisitEntity>
    
    // Update sync status
    @Query("UPDATE anc_visits SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: String, synced: Boolean)
    
    // Delete by ID
    @Query("DELETE FROM anc_visits WHERE id = :id")
    suspend fun deleteVisitById(id: String)
}
```

**Features:**
- ✅ REPLACE conflict strategy for upsert operations
- ✅ Visits sorted by date (most recent first)
- ✅ Offline sync support
- ✅ All operations use coroutines (`suspend`)

---

### 3. AshaLocalDatabase.kt (Updated)
**Location:** `com.sukhayu.patient.data.local.AshaLocalDatabase`

**Changes:**
1. ✅ Added `AncVisitEntity::class` to entities list
2. ✅ Added `AncVisitDao` import
3. ✅ Added `abstract fun ancVisitDao(): AncVisitDao`
4. ✅ **Incremented database version from 4 to 5**
5. ✅ `fallbackToDestructiveMigration()` will handle schema changes

**Current Database Configuration:**
```kotlin
@Database(
    entities = [
        ConsultationEntity::class,
        PrescriptionItemEntity::class,
        PatientEntity::class,
        PregnancyEntity::class,
        AncVisitEntity::class           // ← NEW
    ],
    version = 5,                         // ← INCREMENTED
    exportSchema = false
)
```

---

## 📊 Database Schema Relationship

```
PatientEntity (patients table)
    ↓ 1:N
PregnancyEntity (pregnancies table)
    ↓ 1:N
AncVisitEntity (anc_visits table)
```

- One Patient can have multiple Pregnancies
- One Pregnancy can have multiple ANC Visits
- Each ANC Visit tracks visit number, conditions, interventions

---

## 🔄 Offline-First Architecture

All entities follow the offline-first pattern:

1. **Local Storage First**
   - All data written to Room database immediately
   - App works fully offline

2. **Background Sync**
   - `isSynced = false` marks unsynced records
   - Background worker can query `getUnsyncedVisits()`
   - After successful API sync: `updateSyncStatus(id, true)`

3. **Conflict Resolution**
   - `OnConflictStrategy.REPLACE` handles duplicate inserts
   - Server timestamp can be used for merge conflicts

---

## 🎯 Next Steps - Connecting to UI

### Step 1: Create Repository
**File:** `AncVisitRepository.kt`

```kotlin
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

### Step 2: Create ViewModel
**File:** `FollowUpAncVisitViewModel.kt`

```kotlin
class FollowUpAncVisitViewModel(
    private val repository: AncVisitRepository
) : ViewModel() {
    
    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving
    
    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess
    
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
            } finally {
                _isSaving.value = false
            }
        }
    }
}
```

---

### Step 3: Create ViewModelFactory
**File:** `FollowUpAncVisitViewModelFactory.kt`

```kotlin
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

### Step 4: Update FollowUpAncVisitActivity

**Initialize ViewModel:**
```kotlin
private lateinit var viewModel: FollowUpAncVisitViewModel

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // ... existing setup ...
    
    // Initialize ViewModel
    val dao = AshaLocalDatabase.getInstance(this).ancVisitDao()
    val repository = AncVisitRepository(dao)
    val factory = FollowUpAncVisitViewModelFactory(repository)
    viewModel = ViewModelProvider(this, factory)[FollowUpAncVisitViewModel::class.java]
    
    observeViewModel()
}
```

**Observe ViewModel:**
```kotlin
private fun observeViewModel() {
    viewModel.isSaving.observe(this) { saving ->
        binding.btnSaveFollowUpAnc.isEnabled = !saving
    }
    
    viewModel.saveSuccess.observe(this) { success ->
        if (success == true) {
            Toast.makeText(this, "Follow-up ANC Visit saved successfully", Toast.LENGTH_LONG).show()
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

**Update Save Method:**
```kotlin
private fun saveFollowUpAncVisit() {
    // Build entity from form
    val entity = AncVisitEntity(
        pregnancyId = pregnancyId ?: return, // Must have pregnancy ID
        visitNumber = binding.etVisitNumber.text.toString().toInt(),
        visitDate = binding.etVisitDate.text.toString(),
        facilityType = getFacilityType(),
        symptomsToday = getSelectedSymptoms(),
        bpSystolic = if (binding.switchBpRecorded.isChecked) 
            binding.etBpSystolic.text.toString().toIntOrNull() else null,
        bpDiastolic = if (binding.switchBpRecorded.isChecked)
            binding.etBpDiastolic.text.toString().toIntOrNull() else null,
        weightKg = binding.etWeightKg.text.toString().toFloatOrNull(),
        ifaTabletsGiven = binding.etIfaTablets.text.toString().toIntOrNull(),
        calciumTabletsGiven = binding.etCalciumTablets.text.toString().toIntOrNull(),
        ttDose = binding.autoTtDose.text.toString(),
        referred = binding.switchReferralMade.isChecked,
        referralReason = if (binding.switchReferralMade.isChecked)
            binding.etReferralReason.text.toString() else null,
        nextVisitDate = binding.etNextVisitDate.text.toString().ifBlank { null }
    )
    
    // Save via ViewModel
    viewModel.saveVisit(entity)
}

private fun getFacilityType(): String {
    return when (binding.rgFacilityType.checkedRadioButtonId) {
        R.id.rbFacilityGovt -> "GOVT"
        R.id.rbFacilityPrivate -> "PRIVATE"
        R.id.rbFacilityHome -> "HOME"
        else -> "HOME"
    }
}

private fun getSelectedSymptoms(): String? {
    val symptoms = mutableListOf<String>()
    if (binding.cbSymptomBleeding.isChecked) symptoms.add("BLEEDING")
    if (binding.cbSymptomHeadacheBlurredVision.isChecked) symptoms.add("HEADACHE_BLURRED_VISION")
    if (binding.cbSymptomSwelling.isChecked) symptoms.add("SWELLING")
    if (binding.cbSymptomFeverChills.isChecked) symptoms.add("FEVER_CHILLS")
    if (binding.cbSymptomReducedMovements.isChecked) symptoms.add("REDUCED_MOVEMENTS")
    if (binding.cbSymptomSevereAbdominalPain.isChecked) symptoms.add("ABDOMINAL_PAIN")
    if (binding.cbSymptomNone.isChecked) symptoms.add("NONE")
    
    return symptoms.joinToString(",").ifBlank { null }
}
```

---

## 🧪 Testing Database Operations

### Test 1: Insert Visit
```kotlin
val dao = AshaLocalDatabase.getInstance(context).ancVisitDao()

lifecycleScope.launch {
    val visit = AncVisitEntity(
        pregnancyId = "test-pregnancy-123",
        visitNumber = 2,
        visitDate = "29/11/2025",
        facilityType = "GOVT",
        symptomsToday = "NONE",
        bpSystolic = 120,
        bpDiastolic = 80,
        weightKg = 55.5f,
        referred = false,
        referralReason = null,
        nextVisitDate = "06/12/2025"
    )
    
    dao.upsertVisit(visit)
    Log.d("TEST", "Visit saved: ${visit.id}")
}
```

### Test 2: Query Visits
```kotlin
lifecycleScope.launch {
    val visits = dao.getVisitsForPregnancy("test-pregnancy-123")
    Log.d("TEST", "Found ${visits.size} visits")
    visits.forEach { visit ->
        Log.d("TEST", "Visit #${visit.visitNumber} on ${visit.visitDate}")
    }
}
```

### Test 3: Check Unsynced
```kotlin
lifecycleScope.launch {
    val unsynced = dao.getUnsyncedVisits()
    Log.d("TEST", "${unsynced.size} visits need syncing")
}
```

---

## 📝 Important Notes

1. **Pregnancy ID Required**
   - You MUST pass `pregnancyId` from the First ANC Visit or retrieve it
   - Consider storing pregnancyId when creating PregnancyEntity
   - Update PregnancySurveyActivity to track current pregnancy

2. **Database Migration**
   - Version bumped from 4 to 5
   - `fallbackToDestructiveMigration()` will drop and recreate tables
   - **Warning:** This will delete existing data on upgrade
   - For production, implement proper migrations

3. **Data Validation**
   - All validation is currently in the Activity
   - Consider moving validation to ViewModel or Repository
   - Database accepts all values that pass UI validation

4. **Sync Strategy**
   - `isSynced = false` by default
   - Implement background WorkManager to sync unsynced records
   - Update `isSynced = true` after successful API call

---

## ✅ Summary

**Created:**
- ✅ `AncVisitEntity.kt` - Room entity with all required fields
- ✅ `AncVisitDao.kt` - DAO with CRUD operations
- ✅ Updated `AshaLocalDatabase.kt` - Added entity & DAO, version 5

**Ready for:**
- 🔧 Repository creation
- 🔧 ViewModel integration
- 🔧 Activity connection
- 🔧 Background sync implementation

**Database Status:**
- ✅ Schema defined
- ✅ DAO operations ready
- ✅ Offline-first architecture
- ✅ No compile errors
- ⚠️ Not yet connected to UI (warnings expected)

