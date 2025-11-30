# General Survey Database - Quick Testing Guide

## Pre-Testing Setup

1. **Clean Build**
   ```bash
   ./gradlew clean build
   ```

2. **Install on Device**
   ```bash
   ./gradlew installDebug
   ```

3. **Open Logcat** (in separate terminal)
   ```bash
   adb logcat -s GENERAL_SURVEY_DB:D GENERAL_SURVEY_VM:D AndroidRuntime:E *:S
   ```

---

## Test Case 1: Basic Save Flow

### Steps:
1. Open app
2. Navigate: **Surveys → General Survey**
3. Should see: **"Search Patient - General Survey"** screen
4. Enter patient name: `"John"` (or any dummy patient)
5. Click: **Search Patient**
6. Should see: Patient chooser or direct navigation
7. Select patient (if multiple)
8. Should see: **General Health Survey** form with patient header

### Fill Form:
```
Patient Header:
  - Name: John Doe (pre-filled)
  - Phone: 1234567890 (pre-filled)
  
Section 1 - Identification:
  - Visit Date: [Today's date] (pre-filled)
  - Location: "Test Clinic"
  
Section 2 - Existing Conditions:
  - Diabetes: Yes ✓
  - Hypertension: No ✓
  - Heart Disease: (leave empty)
  - Other: "Family history of diabetes"
  
Section 3 - Symptoms:
  - Frequent Urination: Yes ✓
  - Excessive Thirst: Yes ✓
  - Weight Loss: No ✓
  
Section 4 - Risk Factors:
  - Family History: Yes ✓
  - Tobacco Use: No ✓
  - Physical Activity: Yes ✓ (inactive)
  
Section 5 - Service Use:
  - Regular Checkups: No ✓
  - Current Medication: Yes ✓
  - Medication Details: "Metformin 500mg"
  
Section 6 - ASHA Assessment:
  - Referral Needed: Yes ✓
  - Referral Facility: PHC ✓
  - Remarks: "High risk patient, needs immediate follow-up"
```

9. Click: **Save General Survey**

### Expected Results:
✅ Logcat shows:
```
D/GENERAL_SURVEY_DB: saveGeneralSurvey called
D/GENERAL_SURVEY_DB: Survey entity created: GeneralSurveyEntity(id=0, patientId=patient_xxx, ...)
D/GENERAL_SURVEY_VM: saveSurvey called with patientId: patient_xxx
D/GENERAL_SURVEY_DB: Inserting survey: GeneralSurveyEntity(...)
D/GENERAL_SURVEY_DB: Survey inserted with ID: 1. Total count = 1
D/GENERAL_SURVEY_VM: Survey saved successfully with ID: 1
```

✅ Toast appears: **"General Survey saved successfully (ID: 1)"**

✅ App returns to dashboard

---

## Test Case 2: Verify Database

### Using Android Studio Device File Explorer:
1. View → Tool Windows → Device File Explorer
2. Navigate: `/data/data/com.sukhayu.patient/databases/`
3. Right-click `asha_local_db` → Save As
4. Open with SQLite browser (e.g., DB Browser for SQLite)
5. Check `general_survey` table
6. Verify:
   - 1 record exists
   - `patient_id` matches selected patient
   - `has_diabetes` = 1 (true)
   - `has_hypertension` = 0 (false)
   - `symptom_frequent_urination` = 1
   - `on_current_medication` = 1
   - `medication_details` = "Metformin 500mg"
   - `referral_needed` = 1
   - `referral_facility` = "PHC"
   - `remarks` = "High risk patient, needs immediate follow-up"

### Using adb shell:
```bash
adb shell
cd /data/data/com.sukhayu.patient/databases/
sqlite3 asha_local_db

# Count records
SELECT COUNT(*) FROM general_survey;
# Expected: 1

# View all records
SELECT * FROM general_survey;

# View specific fields
SELECT patient_id, visit_date, has_diabetes, has_hypertension, referral_needed 
FROM general_survey;

# Exit
.quit
exit
```

---

## Test Case 3: Multiple Saves

### Steps:
1. Go back to: **Surveys → General Survey**
2. Search for same patient: `"John"`
3. Fill form with **different values**:
   - Diabetes: No
   - Hypertension: Yes
   - Referral Needed: No
4. Click: **Save**

### Expected Results:
✅ Logcat shows: **"Total count = 2"**

✅ Toast: **"General Survey saved successfully (ID: 2)"**

### Verify Database:
```sql
SELECT COUNT(*) FROM general_survey;
-- Expected: 2

SELECT id, patient_id, has_diabetes, has_hypertension, created_at 
FROM general_survey 
ORDER BY created_at DESC;
-- Should show 2 records for same patient with different values
```

---

## Test Case 4: Different Patient

### Steps:
1. Go to: **Surveys → General Survey**
2. Search for different patient: `"Mary"` (or any other dummy patient)
3. Fill form
4. Click: **Save**

### Expected Results:
✅ Logcat shows: **"Total count = 3"**

✅ Different `patient_id` in logs

### Verify Database:
```sql
SELECT patient_id, patient_name, COUNT(*) as survey_count
FROM general_survey
GROUP BY patient_id;
-- Should show 2 surveys for John, 1 for Mary
```

---

## Test Case 5: Empty Optional Fields

### Steps:
1. Go to: **Surveys → General Survey**
2. Search patient
3. Fill **only required fields**:
   - Visit Date: [Today] (already filled)
4. Fill **one section** to pass validation:
   - Diabetes: Yes
5. Leave all other fields empty
6. Click: **Save**

### Expected Results:
✅ Save succeeds

✅ Logcat shows survey with many null values

### Verify Database:
```sql
SELECT * FROM general_survey WHERE location IS NULL;
-- Should show records with null optional fields
```

---

## Test Case 6: Validation Errors

### Test 6.1: No Patient Selected
**Steps:** Try to open GeneralSurveyActivity directly (if possible)
**Expected:** Error toast + Activity closes

### Test 6.2: Empty Form
**Steps:**
1. Open form
2. Clear visit date
3. Don't fill any fields
4. Click Save

**Expected:** Validation error: "Please fill at least some survey information"

### Test 6.3: No Visit Date
**Steps:**
1. Open form
2. Clear visit date
3. Fill other fields
4. Click Save

**Expected:** Validation error: "Please select visit date"

---

## Test Case 7: Stress Test

### Steps:
1. Save 10 surveys rapidly for same patient
2. Check logcat for count increasing
3. Verify database has 10+ records

### Expected Results:
✅ All saves succeed

✅ Count increases: 1, 2, 3, ..., 10+

✅ No crashes or errors

---

## Test Case 8: App Restart

### Steps:
1. Save a survey
2. Note the record count
3. Close app completely (swipe from recents)
4. Reopen app
5. Check database

### Expected Results:
✅ Data persists after app restart

✅ Record count unchanged

---

## Debugging Common Issues

### Issue: No logs appear
**Solution:** Check logcat filter is correct
```bash
adb logcat -s GENERAL_SURVEY_DB
```

### Issue: Toast says "Error saving survey"
**Check:** Logcat for exception
**Common causes:**
- Database not initialized
- Entity field mismatch
- DAO method signature error

### Issue: Count stays at 0
**Check:**
1. Database version incremented?
2. Entity in @Database entities list?
3. DAO method in database class?
4. `.fallbackToDestructiveMigration()` present?

### Issue: App crashes on save
**Check:**
1. Logcat for full stack trace
2. Verify all imports present
3. Check for null pointer exceptions
4. Ensure patientId is not null

---

## Quick Verification SQL Queries

```sql
-- Count all surveys
SELECT COUNT(*) FROM general_survey;

-- View latest 5 surveys
SELECT id, patient_name, visit_date, created_at 
FROM general_survey 
ORDER BY created_at DESC 
LIMIT 5;

-- Count surveys per patient
SELECT patient_name, COUNT(*) as survey_count
FROM general_survey
GROUP BY patient_id
ORDER BY survey_count DESC;

-- View surveys with referrals
SELECT patient_name, visit_date, referral_facility
FROM general_survey
WHERE referral_needed = 1;

-- View high-risk patients (has any condition)
SELECT DISTINCT patient_name, patient_id
FROM general_survey
WHERE has_diabetes = 1 
   OR has_hypertension = 1 
   OR has_heart_disease = 1;

-- View unsynced surveys
SELECT COUNT(*) FROM general_survey WHERE synced_to_server = 0;
```

---

## Success Criteria

### ✅ All Tests Pass If:
1. Logcat shows all expected messages
2. Database has correct number of records
3. All fields are correctly saved
4. Patient ID is properly linked
5. Optional fields can be null
6. Validation works correctly
7. Data persists after app restart
8. No crashes or errors

### ✅ Production Ready If:
- All 8 test cases pass
- Stress test completes
- Database schema is correct
- Logging is comprehensive
- Error handling works

---

## Rollback Plan (If Issues)

If critical issues found:

1. **Revert Activity Changes**
   ```bash
   git checkout HEAD -- app/src/main/java/.../GeneralSurveyActivity.kt
   ```

2. **Remove New Files**
   ```bash
   rm app/src/main/java/.../GeneralSurveyEntity.kt
   rm app/src/main/java/.../GeneralSurveyDao.kt
   rm app/src/main/java/.../GeneralSurveyRepository.kt
   rm app/src/main/java/.../GeneralSurveyViewModel.kt
   ```

3. **Revert Database**
   ```bash
   git checkout HEAD -- app/src/main/java/.../AshaLocalDatabase.kt
   ```

4. **Uninstall App** (clears database)
   ```bash
   adb uninstall com.sukhayu.patient
   ```

---

**Quick Test Summary:**
1. Fill form → Save → Check logs ✓
2. Query database → Verify data ✓
3. Multiple saves → Count increases ✓
4. App restart → Data persists ✓

**Time Estimate:** 15-20 minutes for complete testing

**Status After Testing:** Mark as ✅ Production Ready

