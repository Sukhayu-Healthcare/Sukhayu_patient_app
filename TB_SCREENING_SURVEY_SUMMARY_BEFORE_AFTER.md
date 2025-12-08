## TB Screening Survey Summary Integration - Before/After Code Comparison

---

## 1. SurveySummaryMappers.kt (NEW FILE)

### Before:
❌ No mapper existed

### After:
✅ New file: `com/sukhayu/patient/data/local/entity/SurveySummaryMappers.kt`

```kotlin
package com.sukhayu.patient.data.local.entity

import java.util.UUID

/**
 * Mapper functions to convert domain entities to SurveySummaryEntity
 * for the "View Surveys" screen.
 */

/**
 * Convert a TbScreeningEntity to a SurveySummaryEntity.
 * This creates a lightweight summary row for the View Surveys list.
 *
 * @param entity The TB screening entity to convert
 * @param ashaId The ASHA worker ID
 * @param isSynced Whether this survey has been synced to the backend
 * @return A SurveySummaryEntity ready to insert into the survey_summary table
 */
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

---

## 2. SurveySummaryDao.kt

### Before:
```kotlin
@Dao
interface SurveySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(summary: SurveySummaryEntity)

    @Query("""
        SELECT * FROM survey_summary
        WHERE ashaId = :ashaId
        ORDER BY surveyDate DESC
    """)
    fun getAllForAsha(ashaId: String): Flow<List<SurveySummaryEntity>>

    @Query("""
        SELECT COUNT(*) FROM survey_summary
        WHERE ashaId = :ashaId AND isSynced = 1
    """)
    fun countSyncedForAsha(ashaId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM survey_summary
        WHERE ashaId = :ashaId AND isSynced = 0
    """)
    fun countPendingForAsha(ashaId: String): Flow<Int>
}
```

### After:
```kotlin
@Dao
interface SurveySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(summary: SurveySummaryEntity)

    @Query("""
        SELECT * FROM survey_summary
        WHERE ashaId = :ashaId
        ORDER BY surveyDate DESC
    """)
    fun getAllForAsha(ashaId: String): Flow<List<SurveySummaryEntity>>

    @Query("""
        SELECT COUNT(*) FROM survey_summary
        WHERE ashaId = :ashaId AND isSynced = 1
    """)
    fun countSyncedForAsha(ashaId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM survey_summary
        WHERE ashaId = :ashaId AND isSynced = 0
    """)
    fun countPendingForAsha(ashaId: String): Flow<Int>

    /**
     * Mark a survey summary as synced by its surveyLocalId and ashaId
     */
    @Query("""
        UPDATE survey_summary
        SET isSynced = 1
        WHERE surveyLocalId = :surveyLocalId AND ashaId = :ashaId
    """)
    suspend fun markSummaryAsSynced(surveyLocalId: String, ashaId: String)  // ✅ NEW
}
```

**Changes:**
- ✅ Added `markSummaryAsSynced()` method
- ✅ Allows marking summary as synced after backend sync

---

## 3. TbScreeningRepository.kt

### Before:
```kotlin
package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.TbScreeningDao
import com.sukhayu.patient.data.local.entity.TbScreeningEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for TB Screening data
 * Handles offline-first data persistence and sync helpers
 */
class TbScreeningRepository(
    private val tbScreeningDao: TbScreeningDao
) {

    /**
     * Save or update a TB screening record
     */
    suspend fun createOrUpdateTbScreening(entity: TbScreeningEntity) = withContext(Dispatchers.IO) {
        tbScreeningDao.upsertTbScreening(entity)
    }

    // ... other methods ...

    /**
     * Mark TB screening as synced
     */
    suspend fun markAsSynced(id: String) = withContext(Dispatchers.IO) {
        tbScreeningDao.markTbScreeningAsSynced(id, System.currentTimeMillis())
    }
}
```

### After:
```kotlin
package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.TbScreeningDao
import com.sukhayu.patient.data.local.dao.SurveySummaryDao  // ✅ NEW IMPORT
import com.sukhayu.patient.data.local.entity.TbScreeningEntity
import com.sukhayu.patient.data.local.entity.fromTbScreening  // ✅ NEW IMPORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for TB Screening data
 * Handles offline-first data persistence and sync helpers
 */
class TbScreeningRepository(
    private val tbScreeningDao: TbScreeningDao,
    private val surveySummaryDao: SurveySummaryDao? = null  // ✅ NEW
) {

    /**
     * Save or update a TB screening record.
     * Also creates/updates a corresponding SurveySummaryEntity for the View Surveys screen.
     *
     * @param entity The TB screening entity to save
     * @param ashaId The ASHA worker ID
     */
    suspend fun createOrUpdateTbScreening(entity: TbScreeningEntity, ashaId: String? = null) = withContext(Dispatchers.IO) {
        tbScreeningDao.upsertTbScreening(entity)
        
        // ✅ NEW: Also create a summary entry for the View Surveys screen
        ashaId?.let {
            surveySummaryDao?.let { dao ->
                val summary = fromTbScreening(entity, ashaId, isSynced = false)
                dao.insertOrUpdate(summary)
            }
        }
    }

    // ... other methods unchanged ...

    /**
     * Mark TB screening as synced.
     * Also updates the corresponding SurveySummaryEntity row.
     *
     * @param id The TB screening entity ID
     * @param ashaId The ASHA worker ID (used to find and update the summary row)
     */
    suspend fun markAsSynced(id: String, ashaId: String? = null) = withContext(Dispatchers.IO) {
        tbScreeningDao.markTbScreeningAsSynced(id, System.currentTimeMillis())
        
        // ✅ NEW: Also mark the summary as synced
        ashaId?.let {
            surveySummaryDao?.let { dao ->
                dao.markSummaryAsSynced(id, ashaId)
            }
        }
    }
}
```

**Changes:**
- ✅ Added `SurveySummaryDao` import
- ✅ Added `fromTbScreening` mapper import
- ✅ Constructor now accepts optional `SurveySummaryDao`
- ✅ `createOrUpdateTbScreening()` now accepts optional `ashaId`
- ✅ After saving TB screening, creates and inserts summary entity
- ✅ `markAsSynced()` now accepts optional `ashaId`
- ✅ After marking TB screening as synced, also marks summary as synced

---

## 4. TbScreeningViewModel.kt

### Before - Init Block:
```kotlin
init {
    val db = AshaLocalDatabase.getInstance(application)
    repository = TbScreeningRepository(db.tbScreeningDao())
}
```

### After - Init Block:
```kotlin
init {
    val db = AshaLocalDatabase.getInstance(application)
    repository = TbScreeningRepository(
        db.tbScreeningDao(),
        db.surveySummaryDao()  // ✅ NEW - inject SurveySummaryDao
    )
}
```

---

### Before - saveTbScreening():
```kotlin
/**
 * Save TB screening locally (offline-first).
 * Shows a toast: "TB screening saved on this phone. It will sync when internet is available."
 */
fun saveTbScreening(entity: TbScreeningEntity) {
    viewModelScope.launch {
        try {
            _isSaving.value = true
            _errorMessage.value = null

            repository.createOrUpdateTbScreening(entity)  // ❌ No ashaId passed

            _isSaving.value = false

            Toast.makeText(
                getApplication(),
                "TB screening saved on this phone. It will sync when internet is available.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            // ... error handling ...
        }
    }
}
```

### After - saveTbScreening():
```kotlin
/**
 * Save TB screening locally (offline-first).
 * Shows a toast: "TB screening saved on this phone. It will sync when internet is available."
 */
fun saveTbScreening(entity: TbScreeningEntity) {
    viewModelScope.launch {
        try {
            _isSaving.value = true
            _errorMessage.value = null

            val ashaId = TokenManager.getUserId()  // ✅ NEW - get ASHA ID
            repository.createOrUpdateTbScreening(entity, ashaId)  // ✅ NEW - pass ashaId

            _isSaving.value = false

            Toast.makeText(
                getApplication(),
                "TB screening saved on this phone. It will sync when internet is available.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            // ... error handling ...
        }
    }
}
```

---

### Before - syncPendingTbScreenings():
```kotlin
fun syncPendingTbScreenings(onFinished: (Int) -> Unit = {}) {
    viewModelScope.launch {
        val app = getApplication<Application>()

        // ... network validation ...

        val token = TokenManager.getToken()
        if (token.isBlank()) {
            // ...
            return@launch
        }

        // ❌ NO ASHA ID RETRIEVED HERE

        try {
            val pending = repository.getUnsyncedTbScreenings()
            // ...

            for (entity in pending) {
                try {
                    val request = entity.toTbFirstRequest()
                    val response = api.submitTbFirst("Bearer $token", request)
                    Log.d(TAG, "Synced TB screening id=${entity.id}. Response: $response")

                    repository.markAsSynced(entity.id)  // ❌ No ashaId passed
                    successCount++
                } catch (e: Exception) {
                    // ... error handling ...
                }
            }

            if (successCount > 0) {
                // ...
            }

            onFinished(successCount)
        } catch (e: Exception) {
            // ...
        }
    }
}
```

### After - syncPendingTbScreenings():
```kotlin
fun syncPendingTbScreenings(onFinished: (Int) -> Unit = {}) {
    viewModelScope.launch {
        val app = getApplication<Application>()

        // ... network validation ...

        val token = TokenManager.getToken()
        if (token.isBlank()) {
            // ...
            return@launch
        }

        val ashaId = TokenManager.getUserId()  // ✅ NEW - get ASHA ID

        try {
            val pending = repository.getUnsyncedTbScreenings()
            // ...

            for (entity in pending) {
                try {
                    val request = entity.toTbFirstRequest()
                    val response = api.submitTbFirst("Bearer $token", request)
                    Log.d(TAG, "Synced TB screening id=${entity.id}. Response: $response")

                    repository.markAsSynced(entity.id, ashaId)  // ✅ NEW - pass ashaId
                    successCount++
                } catch (e: Exception) {
                    // ... error handling ...
                }
            }

            if (successCount > 0) {
                // ...
            }

            onFinished(successCount)
        } catch (e: Exception) {
            // ...
        }
    }
}
```

---

## Summary of Changes by File

| File | Type | Changes |
|------|------|---------|
| SurveySummaryMappers.kt | NEW | New mapper function `fromTbScreening()` |
| SurveySummaryDao.kt | UPDATED | Added `markSummaryAsSynced()` method |
| TbScreeningRepository.kt | UPDATED | Inject SurveySummaryDao, create/update survey summary on save and sync |
| TbScreeningViewModel.kt | UPDATED | Get ashaId from TokenManager, pass to repository methods |

---

## Impact Analysis

### What Changed:
✅ TB screenings now automatically create entries in survey_summary
✅ View Surveys screen can now display TB screenings
✅ Sync state properly tracked for TB screenings
✅ Synced/Pending counts include TB screenings

### What Didn't Change:
✅ TbScreeningEntity structure
✅ TB screening backend API
✅ TbScreeningDao methods
✅ View Surveys Activity/Fragment
✅ View Surveys ViewModel
✅ View Surveys Adapter
✅ View Surveys Layout
✅ No changes to other surveys (ANC, General, etc.) - they continue working as before

### Breaking Changes:
❌ NONE - All changes are backward compatible
- Old code paths still work
- Optional parameters have defaults
- Safe null checks prevent crashes

---

## Usage Examples

### Example 1: Saving a TB Screening
```kotlin
// In TbScreeningActivity or elsewhere
val tbScreening = TbScreeningEntity(
    id = UUID.randomUUID().toString(),
    patientId = "pat_123",
    name = "John Doe",
    ageYears = 45,
    sex = "Male",
    mobileNumber = "9876543210",
    addressVillage = "Village A",
    ashaIdOrName = "asha_001",
    dateOfScreening = "08/12/2025",
    cough2WeeksOrMore = true,
    coughWithBlood = false,
    // ... other symptoms ...
)

viewModel.saveTbScreening(tbScreening)
// Result: TB screening saved to tb_screenings
//         Summary created in survey_summary with isSynced=false
//         View Surveys immediately shows it as Pending
```

### Example 2: What happens in View Surveys
```kotlin
// In AshaViewSurveysViewModel (no changes needed)
viewModel.surveys.observe(this) { surveys ->
    // surveys includes TB screenings from survey_summary
    val tbScreenings = surveys.filter { it.surveyType == "TB_SCREENING" }
    val pending = surveys.filter { !it.isSynced }
    val synced = surveys.filter { it.isSynced }
    
    // Adapter shows all of them
    adapter.submitList(surveys)
}
```

### Example 3: After sync
```kotlin
// In TbScreeningViewModel
viewModel.syncPendingTbScreenings { successCount ->
    // Each synced TB screening:
    // 1. TB entity marked isSynced=true in tb_screenings
    // 2. Summary marked isSynced=true in survey_summary
    // View Surveys counts automatically update via Flow
}
```

---

## Testing the Changes

### Test 1: Save TB Screening
```
1. Open TB Screening form
2. Fill in all details
3. Click Save
4. Expected: Toast appears
5. Go to View Surveys
6. Expected: New survey visible as Pending
```

### Test 2: Verify Summary Created
```
1. Open DB inspector (Android Studio)
2. Check survey_summary table
3. Expected: New row with:
   - surveyType = "TB_SCREENING"
   - isSynced = false
   - patientName = entered name
   - ashaId = current user
```

### Test 3: Sync and Verify
```
1. Ensure internet connected
2. Go to Dashboard
3. Trigger sync
4. Expected: Survey syncs to backend
5. Go to View Surveys
6. Expected: Survey status changes to Synced
7. Check survey_summary in DB
8. Expected: isSynced = true
```

---

**Detailed Code Documentation:** See `TB_SCREENING_SURVEY_SUMMARY_CODE_REFERENCE.md`
**Implementation Checklist:** See `TB_SCREENING_SURVEY_SUMMARY_CHECKLIST.md`
**Integration Summary:** See `TB_SCREENING_SURVEY_SUMMARY_INTEGRATION.md`

