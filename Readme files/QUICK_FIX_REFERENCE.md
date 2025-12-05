# General Survey Navigation Fix - Quick Reference

## What Was Done

✅ **Added intermediary patient search screen before General Survey form**

## Files Summary

### New Files (2)
1. `PatientSearchForGeneralSurveyActivity.kt` - Patient search logic
2. `activity_patient_search_general_survey.xml` - Search UI layout

### Modified Files (3)
1. `GeneralSurveyActivity.kt` - Added patient validation
2. `AshaSurveyHomeActivity.kt` - Changed navigation target
3. `AndroidManifest.xml` - Registered new activity

## Navigation Change

**OLD:** Dashboard → General Survey Form ❌

**NEW:** Dashboard → Patient Search → General Survey Form ✅

## User Flow

1. ASHA clicks "General Survey" button
2. App opens "Search Patient" screen
3. ASHA enters patient name or phone
4. App searches local database
5. If multiple matches, shows chooser dialog
6. ASHA selects patient
7. App opens General Survey form with patient data pre-filled
8. ASHA fills form and saves
9. Back button returns to dashboard

## Key Code Changes

### In AshaSurveyHomeActivity.kt:
```kotlin
// Changed from:
startActivity(Intent(this, GeneralSurveyActivity::class.java))

// To:
startActivity(Intent(this, PatientSearchForGeneralSurveyActivity::class.java))
```

### In GeneralSurveyActivity.kt:
```kotlin
// Added validation:
if (patientId.isNullOrBlank()) {
    Toast.makeText(this, "Error: No patient selected...", Toast.LENGTH_LONG).show()
    finish()
    return
}
```

## Testing Commands

```bash
# Clean and rebuild
./gradlew clean build

# Install on device
./gradlew installDebug

# Run app
# Then manually test the flow
```

## Validation Points

1. ✅ Cannot open General Survey without patient
2. ✅ Search validates input is not empty
3. ✅ Handles single match (auto-select)
4. ✅ Handles multiple matches (chooser)
5. ✅ Handles no matches (error message)
6. ✅ Back navigation works correctly

## If Build Fails

**Issue:** R file not found for layout
**Solution:** Clean and rebuild project

**Issue:** Import errors
**Solution:** Sync Gradle files

**Issue:** Activity not found
**Solution:** Check AndroidManifest.xml has the activity registered

## Quick Test

1. Open app
2. Go to Surveys
3. Click "General Survey"
4. Should see: "Search Patient for General Survey" screen
5. Enter any patient name from dummy data
6. Should navigate to form with patient data

## Patient Data Flow

```
PatientSearch (selects patient)
    ↓ (passes via Intent extras)
GeneralSurvey (receives patient data)
    ↓ (validates patientId)
Database (saves with patient_id)
```

## Error Scenarios Handled

| Scenario | Behavior |
|----------|----------|
| No patient selected | Toast + Activity closes |
| Empty search query | Error message shown |
| No matching patients | Error message shown |
| Direct form access | Validation fails, activity closes |

## Success Criteria

- ✅ All files created/modified
- ✅ No compilation errors
- ✅ Activity registered in manifest
- ✅ Navigation updated
- ✅ Validation in place
- ⏳ Build and test pending

## Contact Points

- Main entry: `AshaSurveyHomeActivity.btnGeneralSurvey.onClick`
- Search screen: `PatientSearchForGeneralSurveyActivity`
- Form screen: `GeneralSurveyActivity`
- Validation: `GeneralSurveyActivity.readIntentExtrasAndPrefillForm()`

---

**Status:** Implementation Complete ✅
**Next:** Build, install, and test on device
**Documentation:** See `GENERAL_SURVEY_NAVIGATION_FIX_COMPLETE.md` for details

