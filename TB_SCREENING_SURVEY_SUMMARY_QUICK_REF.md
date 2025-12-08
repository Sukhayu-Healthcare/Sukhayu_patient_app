## TB Screening → Survey Summary Integration - Quick Reference Card

---

## 🎯 What Was Done

TB Screening surveys are now automatically added to the `survey_summary` table when:
1. **Saved locally** → Summary created with `isSynced=false` (Pending)
2. **Synced to backend** → Summary updated with `isSynced=true` (Synced)

This allows TB screenings to appear in the View Surveys screen with proper sync tracking.

---

## 📁 4 Files Modified/Created

### 1️⃣ SurveySummaryMappers.kt (NEW)
**Purpose:** Convert TB screening → survey summary

```kotlin
fun fromTbScreening(
    entity: TbScreeningEntity,
    ashaId: String,
    isSynced: Boolean = false
): SurveySummaryEntity
```

### 2️⃣ SurveySummaryDao.kt (UPDATED)
**Purpose:** Mark surveys as synced

```kotlin
suspend fun markSummaryAsSynced(
    surveyLocalId: String,
    ashaId: String
)
```

### 3️⃣ TbScreeningRepository.kt (UPDATED)
**Purpose:** Orchestrate TB screening + survey summary saves/syncs

```kotlin
// Save: TbScreening + Survey Summary
suspend fun createOrUpdateTbScreening(
    entity: TbScreeningEntity,
    ashaId: String? = null
)

// Sync: Mark both as synced
suspend fun markAsSynced(
    id: String,
    ashaId: String? = null
)
```

### 4️⃣ TbScreeningViewModel.kt (UPDATED)
**Purpose:** Get ASHA ID and pass to repository

```kotlin
// When saving
val ashaId = TokenManager.getUserId()
repository.createOrUpdateTbScreening(entity, ashaId)

// When syncing
val ashaId = TokenManager.getUserId()
repository.markAsSynced(entity.id, ashaId)
```

---

## 🔄 Data Flow

```
User Saves TB Screening
        ↓
TbScreeningViewModel.saveTbScreening()
        ↓
Get ashaId = TokenManager.getUserId()
        ↓
TbScreeningRepository.createOrUpdateTbScreening(entity, ashaId)
        ├→ Save to tb_screenings table (isSynced=false)
        └→ Create summary in survey_summary (isSynced=false)
               ↓
        Survey appears in View Surveys as "Pending"

---

User Triggers Sync
        ↓
TbScreeningViewModel.syncPendingTbScreenings()
        ↓
Get ashaId = TokenManager.getUserId()
        ↓
For each pending TB screening:
  POST to backend API
        ↓
  On success:
  TbScreeningRepository.markAsSynced(id, ashaId)
    ├→ Mark tb_screenings as synced (isSynced=true)
    └→ Mark survey_summary as synced (isSynced=true)
             ↓
  Survey status updates to "Synced" in View Surveys
```

---

## 📊 Database Impact

### tb_screenings table
- **No changes** to schema
- Now has corresponding summary rows
- `isSynced` flag kept in sync with survey_summary

### survey_summary table
- **New rows** created when TB screening saved
- `surveyType` = `"TB_SCREENING"`
- `surveyLocalId` = TB screening ID
- `isSynced` updated when TB screening synced

### Relationship
```
tb_screenings.id ←→ survey_summary.surveyLocalId
    tb_screenings.isSynced = survey_summary.isSynced (kept in sync)
```

---

## ✅ Key Features

- ✅ **Automatic Summary Creation** - No manual steps needed
- ✅ **Sync Tracking** - Pending/Synced status properly maintained
- ✅ **View Surveys Integration** - TB screenings appear alongside other surveys
- ✅ **No UI Changes** - View Surveys screen works as-is
- ✅ **No Backend Changes** - Uses existing API
- ✅ **Backward Compatible** - Old code still works
- ✅ **Coroutine Safe** - All DB ops on Dispatchers.IO

---

## 🧪 Testing Quick Guide

### Step 1: Save TB Screening
```
1. Open TB Screening form
2. Fill details and Save
3. Check: Toast appears ✓
```

### Step 2: Verify in View Surveys
```
1. Go to View Surveys
2. Look for saved TB screening
3. Check: Status shows "Pending" ✓
4. Check: Pending count increased ✓
```

### Step 3: Sync
```
1. Enable internet
2. Go to Dashboard → Sync
3. Check: Survey syncs to backend ✓
```

### Step 4: Verify Sync Complete
```
1. Go to View Surveys
2. Check: Status changed to "Synced" ✓
3. Check: Pending count decreased ✓
4. Check: Synced count increased ✓
```

---

## 🔧 Implementation Details

### When saveTbScreening() called:
1. Get ASHA ID from TokenManager
2. Repository saves TbScreeningEntity
3. Repository calls `fromTbScreening()` mapper
4. Repository inserts SurveySummaryEntity
5. View Surveys Flow gets new entry → UI updates

### When syncPendingTbScreenings() called:
1. Get ASHA ID from TokenManager
2. For each pending TB screening:
   - POST to backend
   - On success: call `markAsSynced(id, ashaId)`
3. Repository updates both tables
4. View Surveys Flow detects isSynced change → UI updates

### Safe Null Handling:
```kotlin
ashaId?.let {
    surveySummaryDao?.let { dao ->
        // Only execute if both are non-null
    }
}
```
- If ashaId is null → summary not created (no crash)
- If surveySummaryDao is null → summary not created (no crash)
- TB screening always saved successfully

---

## 📋 Mapper Field Mapping

| SurveySummaryEntity Field | Source | Value |
|---------------------------|--------|-------|
| summaryId | Generated | UUID.randomUUID() |
| surveyLocalId | TbScreeningEntity.id | TB screening ID |
| serverId | N/A | null (populated on backend) |
| patientId | TbScreeningEntity.patientId | Patient ID |
| patientName | TbScreeningEntity.name | Patient name |
| patientPhone | TbScreeningEntity.mobileNumber | Phone number |
| surveyType | Hardcoded | "TB_SCREENING" |
| surveyDate | Generated | System.currentTimeMillis() |
| village | TbScreeningEntity.addressVillage | Village |
| status | Hardcoded | "COMPLETED" |
| isSynced | Parameter | false (initially) |
| ashaId | TokenManager.getUserId() | Current ASHA ID |

---

## 🚀 No Changes Needed To:

- ✅ View Surveys Activity
- ✅ View Surveys ViewModel  
- ✅ View Surveys Adapter
- ✅ View Surveys Layout
- ✅ TbScreeningEntity
- ✅ TB Screening UI/Form
- ✅ TB Screening API
- ✅ TbScreeningDao
- ✅ AshaLocalDatabase
- ✅ SurveySummaryEntity

---

## 💡 Common Questions

### Q: Why create a separate survey_summary table?
A: Keeps View Surveys screen lightweight and fast. Instead of querying TB, ANC, General survey tables separately, View Surveys queries one table.

### Q: Can I edit TB screening after saving?
A: Yes. TB screening updates in tb_screenings, survey_summary is updated on next sync.

### Q: What if sync fails?
A: TB screening stays marked as not synced, will retry on next sync attempt.

### Q: Can other survey types be added?
A: Yes, follow the same pattern with new mappers for ANC, General Survey, etc.

### Q: Is there any backend call in View Surveys?
A: No, only Room database queries. Completely offline-capable.

---

## 🐛 Debugging Tips

### If survey not appearing in View Surveys:
```kotlin
// Check ashaId
Log.d("TB_SYNC", "ashaId: ${TokenManager.getUserId()}")

// Check summary creation
Log.d("TB_SYNC", "Creating summary for ashaId: $ashaId")

// Check DB
// Use Android Studio DB Inspector on survey_summary table
```

### If sync count not updating:
```kotlin
// Check markAsSynced called
Log.d("TB_SYNC", "Marking as synced: id=$id, ashaId=$ashaId")

// Check surveySummaryDao not null
Log.d("TB_SYNC", "surveySummaryDao: $surveySummaryDao")
```

### If crash on save:
```kotlin
// Check surveySummaryDao injected
Log.d("TB_SYNC", "surveySummaryDao initialized: $surveySummaryDao")

// Check for DB exceptions
// Look for Room/SQLite errors in logcat
```

---

## 📚 Documentation Files

1. **TB_SCREENING_SURVEY_SUMMARY_INTEGRATION.md** - Full implementation overview
2. **TB_SCREENING_SURVEY_SUMMARY_CODE_REFERENCE.md** - Detailed code explanations
3. **TB_SCREENING_SURVEY_SUMMARY_BEFORE_AFTER.md** - Before/after code comparison
4. **TB_SCREENING_SURVEY_SUMMARY_CHECKLIST.md** - Testing and verification checklist
5. **TB_SCREENING_SURVEY_SUMMARY_QUICK_REF.md** - This file (quick reference)

---

## ✨ Summary

✅ **Created:** `SurveySummaryMappers.kt` mapper function
✅ **Updated:** `SurveySummaryDao.kt` with sync marking method
✅ **Updated:** `TbScreeningRepository.kt` to create/update summaries
✅ **Updated:** `TbScreeningViewModel.kt` to pass ASHA ID
✅ **Result:** TB screenings now appear in View Surveys with sync tracking
✅ **No Breaking Changes:** All existing code still works

---

## 🎓 Key Takeaways

1. **Mapper Pattern** - Easy conversion between entities
2. **Safe Null Handling** - No crashes if something is missing
3. **Sync Consistency** - Both tables updated together
4. **Coroutine Safe** - All DB ops on proper dispatchers
5. **View Surveys Integration** - Works seamlessly with existing code

---

**Status:** ✅ Implementation Complete
**Date:** December 8, 2025
**Package:** com.sukhayu.patient.asha.ui.surveys.*

