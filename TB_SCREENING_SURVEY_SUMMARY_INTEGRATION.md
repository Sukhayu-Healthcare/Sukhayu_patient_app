## TB Screening Survey Summary Integration - Implementation Summary

### Overview
This implementation integrates TB Screening surveys into the survey_summary Room table, ensuring that whenever a TB Screening is saved locally, it appears in the View Surveys screen with proper sync tracking.

---

## Changes Made

### 1. **New File: SurveySummaryMappers.kt**
Location: `com.sukhayu.patient.data.local.entity.SurveySummaryMappers`

**Purpose:** Mapper function to convert TbScreeningEntity → SurveySummaryEntity

**Key Function:**
```kotlin
fun fromTbScreening(
    entity: TbScreeningEntity,
    ashaId: String,
    isSynced: Boolean = false
): SurveySummaryEntity
```

**What it does:**
- Creates a unique `summaryId` using UUID
- Links to the TB screening via `surveyLocalId = entity.id`
- Maps patient info (name, phone, ID)
- Sets `surveyType = "TB_SCREENING"`
- Uses current timestamp for `surveyDate`
- Sets `status = "COMPLETED"`
- Accepts ashaId and isSynced as parameters

---

### 2. **Updated: SurveySummaryDao.kt**
Location: `com.sukhayu.patient.data.local.dao`

**New Method Added:**
```kotlin
@Query("""
    UPDATE survey_summary
    SET isSynced = 1
    WHERE surveyLocalId = :surveyLocalId AND ashaId = :ashaId
""")
suspend fun markSummaryAsSynced(surveyLocalId: String, ashaId: String)
```

**Purpose:** Mark a survey summary as synced by matching TB screening ID and ASHA ID.

---

### 3. **Updated: TbScreeningRepository.kt**
Location: `com.sukhayu.patient.data.repository`

**Changes:**

#### Constructor Update:
```kotlin
class TbScreeningRepository(
    private val tbScreeningDao: TbScreeningDao,
    private val surveySummaryDao: SurveySummaryDao? = null  // ✅ NEW
)
```

#### Method: createOrUpdateTbScreening (Updated)
```kotlin
suspend fun createOrUpdateTbScreening(
    entity: TbScreeningEntity, 
    ashaId: String? = null  // ✅ NEW
)
```
- Now accepts optional `ashaId` parameter
- After saving TB screening, creates a SurveySummaryEntity via mapper
- Inserts it into survey_summary table
- All work happens on Dispatchers.IO

#### Method: markAsSynced (Updated)
```kotlin
suspend fun markAsSynced(
    id: String, 
    ashaId: String? = null  // ✅ NEW
)
```
- Now accepts optional `ashaId` parameter
- Marks TB screening as synced
- Also marks corresponding SurveySummaryEntity as synced using `surveySummaryDao.markSummaryAsSynced()`

---

### 4. **Updated: TbScreeningViewModel.kt**
Location: `com.sukhayu.patient.asha.ui.surveys.tb`

#### Init Block (Updated):
```kotlin
init {
    val db = AshaLocalDatabase.getInstance(application)
    repository = TbScreeningRepository(
        db.tbScreeningDao(),
        db.surveySummaryDao()  // ✅ NEW - inject SurveySummaryDao
    )
}
```

#### Method: saveTbScreening (Updated)
```kotlin
fun saveTbScreening(entity: TbScreeningEntity) {
    viewModelScope.launch {
        try {
            _isSaving.value = true
            _errorMessage.value = null

            val ashaId = TokenManager.getUserId()  // ✅ NEW
            repository.createOrUpdateTbScreening(entity, ashaId)  // ✅ Pass ashaId
            
            // ... rest of method
        }
    }
}
```

#### Method: syncPendingTbScreenings (Updated)
```kotlin
fun syncPendingTbScreenings(onFinished: (Int) -> Unit = {}) {
    viewModelScope.launch {
        // ... network checks ...
        
        val ashaId = TokenManager.getUserId()  // ✅ NEW
        
        // ... sync logic ...
        
        for (entity in pending) {
            try {
                val request = entity.toTbFirstRequest()
                val response = api.submitTbFirst("Bearer $token", request)
                
                repository.markAsSynced(entity.id, ashaId)  // ✅ Pass ashaId
                successCount++
            }
            // ... error handling ...
        }
    }
}
```

---

## How It Works

### When a TB Screening is Saved:
1. User fills out TB Screening form and saves
2. `TbScreeningViewModel.saveTbScreening()` is called
3. ViewModel retrieves current ASHA ID via `TokenManager.getUserId()`
4. Repository saves TbScreeningEntity to `tb_screenings` table
5. Repository calls mapper to create SurveySummaryEntity
6. SurveySummaryEntity is inserted to `survey_summary` table with `isSynced = false`
7. **Result:** New survey appears in View Surveys list as "Pending"

### When TB Screening is Synced to Backend:
1. `TbScreeningViewModel.syncPendingTbScreenings()` is called
2. For each successful backend sync:
   - TbScreeningEntity marked as synced in `tb_screenings` table
   - **NEW:** SurveySummaryEntity marked as synced in `survey_summary` table
3. **Result:** Survey status updates in View Surveys list from "Pending" to "Synced"

---

## Database Integration Points

### SurveySummaryEntity Fields (for TB Screening):
| Field | Value | Notes |
|-------|-------|-------|
| summaryId | UUID.randomUUID() | Unique identifier |
| surveyLocalId | entity.id | Reference to TB screening ID |
| serverId | null | Will be populated on sync |
| patientId | entity.patientId | Patient reference |
| patientName | entity.name | TB screening patient name |
| patientPhone | entity.mobileNumber | TB screening patient phone |
| surveyType | "TB_SCREENING" | Survey type identifier |
| surveyDate | System.currentTimeMillis() | When saved |
| village | entity.addressVillage | Patient village |
| status | "COMPLETED" | Survey status |
| isSynced | false/true | Sync state |
| ashaId | TokenManager.getUserId() | ASHA worker ID |

---

## No Changes Required To:
- ✅ View Surveys screen (already reads from survey_summary)
- ✅ View Surveys ViewModel (already observes survey_summary)
- ✅ View Surveys Adapter (already displays SurveySummaryUiModel)
- ✅ TbScreeningEntity structure
- ✅ AshaLocalDatabase (already has surveySummaryDao())
- ✅ TB Screening UI/Form

---

## Testing Checklist

- [ ] Save a new TB Screening survey
- [ ] Verify it appears in View Surveys list
- [ ] Verify it shows as "Pending" (not synced)
- [ ] Verify sync counts update correctly
- [ ] Trigger sync (make internet available)
- [ ] Verify survey status changes to "Synced"
- [ ] Verify sync counts update after sync

---

## Package Structure
```
com.sukhayu.patient.data.local.entity
  ├── SurveySummaryMappers.kt (NEW)
  └── SurveySummaryEntity.kt

com.sukhayu.patient.data.local.dao
  ├── SurveySummaryDao.kt (UPDATED)
  └── TbScreeningDao.kt

com.sukhayu.patient.data.repository
  └── TbScreeningRepository.kt (UPDATED)

com.sukhayu.patient.asha.ui.surveys.tb
  └── TbScreeningViewModel.kt (UPDATED)
```

