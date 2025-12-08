## TB Screening → Survey Summary Integration - Code Reference Guide

### Quick Summary
When a TB Screening is saved locally or synced to backend, a corresponding row is automatically added/updated in the `survey_summary` table. This allows TB screenings to appear in the View Surveys screen.

---

## 1. SurveySummaryMappers.kt (NEW FILE)

**Location:** `com.sukhayu.patient.data.local.entity.SurveySummaryMappers.kt`

**Purpose:** Single mapper function to convert TB screening entities to summary entities.

```kotlin
fun fromTbScreening(
    entity: TbScreeningEntity,
    ashaId: String,
    isSynced: Boolean = false
): SurveySummaryEntity {
    return SurveySummaryEntity(
        summaryId = UUID.randomUUID().toString(),
        surveyLocalId = entity.id,
        serverId = null,
        patientId = entity.patientId,
        patientName = entity.name,
        patientPhone = entity.mobileNumber,
        surveyType = "TB_SCREENING",
        surveyDate = System.currentTimeMillis(),
        village = entity.addressVillage,
        status = "COMPLETED",
        isSynced = isSynced,
        ashaId = ashaId
    )
}
```

**Usage:**
```kotlin
val summary = fromTbScreening(
    entity = tbScreeningEntity,
    ashaId = "asha_id_123",
    isSynced = false
)
surveySummaryDao.insertOrUpdate(summary)
```

---

## 2. SurveySummaryDao.kt (UPDATED)

**New Method Added:**

```kotlin
@Query("""
    UPDATE survey_summary
    SET isSynced = 1
    WHERE surveyLocalId = :surveyLocalId AND ashaId = :ashaId
""")
suspend fun markSummaryAsSynced(surveyLocalId: String, ashaId: String)
```

**Usage:**
```kotlin
surveySummaryDao.markSummaryAsSynced(
    surveyLocalId = tbScreeningId,
    ashaId = currentAshaId
)
```

---

## 3. TbScreeningRepository.kt (UPDATED)

### Constructor - Inject SurveySummaryDao:
```kotlin
class TbScreeningRepository(
    private val tbScreeningDao: TbScreeningDao,
    private val surveySummaryDao: SurveySummaryDao? = null  // NEW
)
```

### Method 1: createOrUpdateTbScreening
```kotlin
suspend fun createOrUpdateTbScreening(
    entity: TbScreeningEntity, 
    ashaId: String? = null  // NEW
) = withContext(Dispatchers.IO) {
    // Save to tb_screenings table
    tbScreeningDao.upsertTbScreening(entity)
    
    // NEW: Also save summary entry
    ashaId?.let {
        surveySummaryDao?.let { dao ->
            val summary = fromTbScreening(entity, ashaId, isSynced = false)
            dao.insertOrUpdate(summary)
        }
    }
}
```

**Key Points:**
- Called when user saves a new TB screening
- Automatically creates a SurveySummaryEntity
- Runs on Dispatchers.IO for database operations
- Summary created with `isSynced = false` (pending sync)

### Method 2: markAsSynced
```kotlin
suspend fun markAsSynced(
    id: String, 
    ashaId: String? = null  // NEW
) = withContext(Dispatchers.IO) {
    // Mark TB screening as synced
    tbScreeningDao.markTbScreeningAsSynced(id, System.currentTimeMillis())
    
    // NEW: Also mark summary as synced
    ashaId?.let {
        surveySummaryDao?.let { dao ->
            dao.markSummaryAsSynced(id, ashaId)
        }
    }
}
```

**Key Points:**
- Called after successful backend sync
- Updates both tb_screenings and survey_summary tables
- Maintains consistency between tables

---

## 4. TbScreeningViewModel.kt (UPDATED)

### Init Block - Inject SurveySummaryDao:
```kotlin
init {
    val db = AshaLocalDatabase.getInstance(application)
    repository = TbScreeningRepository(
        db.tbScreeningDao(),
        db.surveySummaryDao()  // NEW - inject summary dao
    )
}
```

### Method 1: saveTbScreening
```kotlin
fun saveTbScreening(entity: TbScreeningEntity) {
    viewModelScope.launch {
        try {
            _isSaving.value = true
            _errorMessage.value = null

            val ashaId = TokenManager.getUserId()  // NEW
            repository.createOrUpdateTbScreening(entity, ashaId)  // Pass ashaId

            _isSaving.value = false
            Toast.makeText(
                getApplication(),
                "TB screening saved on this phone. It will sync when internet is available.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save TB screening", e)
            _isSaving.value = false
            _errorMessage.value = "Failed to save TB screening: ${e.message}"
            Toast.makeText(
                getApplication(),
                "Failed to save TB screening locally.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
```

**Key Points:**
- Gets current ASHA ID from TokenManager
- Passes ashaId to repository method
- Repository then creates survey summary entry
- User sees "Pending" status in View Surveys after this completes

### Method 2: syncPendingTbScreenings
```kotlin
fun syncPendingTbScreenings(onFinished: (Int) -> Unit = {}) {
    viewModelScope.launch {
        // ... network validation ...
        
        val ashaId = TokenManager.getUserId()  // NEW
        
        // ... fetch pending screenings ...
        
        for (entity in pending) {
            try {
                val request = entity.toTbFirstRequest()
                val response = api.submitTbFirst("Bearer $token", request)
                Log.d(TAG, "Synced TB screening id=${entity.id}. Response: $response")

                repository.markAsSynced(entity.id, ashaId)  // Pass ashaId
                successCount++
            } catch (e: HttpException) {
                // ... error handling ...
            } catch (e: Exception) {
                // ... error handling ...
            }
        }
        
        // ... completion logic ...
    }
}
```

**Key Points:**
- Gets current ASHA ID from TokenManager
- For each successfully synced TB screening:
  - Marks TB entity as synced
  - Also marks survey summary as synced
- View Surveys count automatically updates via Flow

---

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────┐
│  TB Screening Form (TbScreeningActivity)            │
│  User enters symptoms, patient info, etc.           │
└──────────────────┬──────────────────────────────────┘
                   │ saveTbScreening()
                   ↓
┌──────────────────────────────────────────────────────┐
│  TbScreeningViewModel                                │
│  - Gets ASHA ID from TokenManager                    │
│  - Calls repository.createOrUpdateTbScreening()     │
└──────────────────┬──────────────────────────────────┘
                   │
        ┌──────────┴──────────┐
        ↓                     ↓
┌─────────────────┐  ┌──────────────────────┐
│ TbScreeningDao  │  │ SurveySummaryDao     │
│                 │  │                      │
│ Insert to       │  │ Insert to            │
│ tb_screenings   │  │ survey_summary       │
│ isSynced=false  │  │ isSynced=false       │
└─────────────────┘  └──────────────────────┘
                              ↑
                   fromTbScreening() mapper
                   converts entity → summary

        Later: Backend Sync
                   ↓
      ┌──────────────────────────┐
      │ syncPendingTbScreenings()│
      │ - Gets ASHA ID           │
      │ - Calls API              │
      │ - On success:            │
      │   markAsSynced(id, ashaId)│
      └──────────────┬───────────┘
                     │
        ┌────────────┴────────────┐
        ↓                         ↓
    Update                    Update
    tb_screenings             survey_summary
    isSynced=true             isSynced=true
```

---

## Database Tables Involved

### tb_screenings table
- Columns: id, patientId, name, mobileNumber, addressVillage, dateOfScreening, [symptoms], [risk factors], isSynced, createdAt, updatedAt
- **Role:** Detailed TB screening record

### survey_summary table
- Columns: summaryId, surveyLocalId, patientId, patientName, patientPhone, surveyType, surveyDate, village, status, isSynced, ashaId
- **Role:** Lightweight summary for View Surveys list

**Relationship:**
- `survey_summary.surveyLocalId` = `tb_screenings.id`
- `survey_summary.surveyType` = `"TB_SCREENING"`
- Both have `isSynced` flag kept in sync

---

## Error Handling

### If SurveySummaryDao is null:
```kotlin
ashaId?.let {
    surveySummaryDao?.let { dao ->  // Safe call - skips if null
        val summary = fromTbScreening(entity, ashaId, isSynced = false)
        dao.insertOrUpdate(summary)
    }
}
```
- If `ashaId` is null → summary not created (old code path still works)
- If `surveySummaryDao` is null → summary not created (backward compatible)
- TB screening is still saved normally

### If TokenManager.getUserId() fails:
```kotlin
val ashaId = TokenManager.getUserId()  // May be empty/null
repository.createOrUpdateTbScreening(entity, ashaId)
// Repository handles null ashaId gracefully
```

---

## Testing Scenarios

### Scenario 1: Save TB Screening (Offline)
1. User fills form and clicks Save
2. ✅ TB screening saved to `tb_screenings` with `isSynced=false`
3. ✅ Summary created in `survey_summary` with `isSynced=false`
4. ✅ View Surveys shows survey as "Pending"
5. ✅ Pending count increases

### Scenario 2: Sync TB Screening (Online)
1. User clicks Sync (or auto-sync on network available)
2. ✅ TB screening POST to backend succeeds
3. ✅ TB screening marked as `isSynced=true` in `tb_screenings`
4. ✅ Summary marked as `isSynced=true` in `survey_summary`
5. ✅ View Surveys updates survey to "Synced"
6. ✅ Synced count increases, Pending count decreases

### Scenario 3: Multiple TB Screenings
1. Save TB Screening #1 → appears in View Surveys as Pending (1/0)
2. Save TB Screening #2 → appears in View Surveys as Pending (2/0)
3. Sync #1 → View Surveys shows (1/1)
4. Sync #2 → View Surveys shows (0/2)

---

## Backward Compatibility

✅ Old code that doesn't pass `ashaId` still works:
```kotlin
// This still works - survey just won't be added to summary
repository.createOrUpdateTbScreening(entity)  // ashaId defaults to null
```

✅ If `surveySummaryDao` is null, still works:
```kotlin
repository = TbScreeningRepository(tbScreeningDao)  // surveySummaryDao = null
// TB screenings saved normally, just not to survey_summary
```

---

## Next Steps

If you want to add other survey types (ANC, General Survey, etc.) to survey_summary:

1. Create similar mapper function:
   ```kotlin
   fun fromAncVisit(entity: AncVisitEntity, ashaId: String, isSynced: Boolean = false): SurveySummaryEntity
   ```

2. Update their repositories with same pattern

3. Update their ViewModels to pass ashaId

4. Sync logic follows the same pattern

That's it! The View Surveys screen will automatically show all survey types.

