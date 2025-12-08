# View Surveys - Quick Reference Guide

## What Was Done

Your View Surveys screen now loads **REAL data from Room Database** instead of dummy data.

---

## Key Components

### 1. ViewModel (`AshaViewSurveysViewModel`)
Loads surveys from Room:
```kotlin
val entities = dao.getAllForAsha(ashaId).first()  // Get surveys
val surveys = entities.map { it.toUiModel() }     // Convert to UI models
val synced = dao.countSyncedForAsha(ashaId)       // Sync count
val pending = dao.countPendingForAsha(ashaId)     // Pending count
```

**Date Format:** `dd MMM yyyy` (e.g., "15 Dec 2024")
**Type Format:** `TB_SCREENING` → `TB Screening`

### 2. Activity (`AshaViewSurveysActivity`)
Displays surveys with filtering:

#### SearchView Filtering
- Searches by **patient name** (case-insensitive)
- Searches by **survey type** (case-insensitive)
- Real-time as user types

#### Spinner Filtering
Select from:
- All Types (default)
- TB Screening
- TB Follow-up
- ANC First Visit
- ANC Follow-up
- General Survey

#### Combined Filtering
Both SearchView AND Spinner filters work together automatically.

---

## Data Flow

```
Room Database
    ↓
ViewModel.getAllForAsha(ashaId)
    ↓
Map Entity → UiModel (format dates & types)
    ↓
Observer stores in fullSurveyList
    ↓
SearchView + Spinner filter the list
    ↓
RecyclerView displays filtered results
```

---

## Sync Status

Displays in `tv_sync_summary`:
```
"Synced: X | Pending: Y"
```

Where:
- **X** = count of surveys with `isSynced = true`
- **Y** = count of surveys with `isSynced = false`

Updates automatically from Room database.

---

## No Backend Calls

✅ All data comes from **Room Database** only
✅ No API calls made
✅ Works offline
✅ Fast local access

---

## Files Modified

1. **AshaViewSurveysActivity.kt** - Added SearchView & Spinner filtering
2. **AshaViewSurveysViewModel.kt** - Already complete (no changes needed)

All other files were already in place:
- SurveySummaryEntity
- SurveySummaryDao
- AshaLocalDatabase
- SurveySummaryAdapter
- Layout XML

---

## Testing

To verify it works:

1. Make sure `survey_summary` table has test data
2. Open the Activity
3. Verify:
   - ✅ Surveys display in list
   - ✅ Sync count shows correct numbers
   - ✅ SearchView filters by name/type
   - ✅ Spinner filters by type
   - ✅ Both filters work together
   - ✅ Dates show as `dd MMM yyyy`
   - ✅ Survey types show friendly names

---

## Code Locations

| Component | Path |
|-----------|------|
| ViewModel | `com.sukhayu.patient.asha.ui.surveys.AshaViewSurveysViewModel` |
| Activity | `com.sukhayu.patient.asha.ui.surveys.AshaViewSurveysActivity` |
| Adapter | `com.sukhayu.patient.asha.ui.surveys.SurveySummaryAdapter` |
| Entity | `com.sukhayu.patient.data.local.entity.SurveySummaryEntity` |
| DAO | `com.sukhayu.patient.data.local.dao.SurveySummaryDao` |
| Database | `com.sukhayu.patient.data.local.AshaLocalDatabase` |

---

## Ready to Use ✅

The View Surveys screen is fully functional and ready to display real survey data from your Room database.

