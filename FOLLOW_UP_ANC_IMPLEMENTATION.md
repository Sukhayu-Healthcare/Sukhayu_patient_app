# Follow-up ANC Visit Implementation Summary

## Overview
Successfully implemented a complete Follow-up ANC Visit form for the ASHA EHR Android app. The implementation follows the same patterns and design as the existing FirstAncVisitActivity.

## Files Created/Modified

### 1. **activity_follow_up_anc_visit.xml** (NEW)
Location: `app/src/main/res/layout/activity_follow_up_anc_visit.xml`

**Layout Structure:**
- Uses `NestedScrollView` with vertical `LinearLayout`
- Multiple `MaterialCardView` sections for organized content
- Consistent styling with FirstAncVisitActivity

**Sections:**

#### A) Patient Header (Read-only)
- `tvPatientNameHeader` - Displays patient name
- `tvPatientPhoneHeader` - Displays phone number
- `tvPatientGenderHeader` - Displays gender
- `tvPatientWeightHeader` - Displays weight

#### B) Visit Details
- `etVisitDate` - Visit date picker (defaults to today)
- `etVisitNumber` - Visit number (2, 3, 4, ...)
- `rgFacilityType` - Radio group with 3 options:
  - `rbFacilityGovt` - Government facility
  - `rbFacilityPrivate` - Private facility
  - `rbFacilityHome` - Home visit

#### C) Current Condition
**Symptoms (Checkboxes):**
- `cbSymptomBleeding` - Vaginal bleeding
- `cbSymptomHeadacheBlurredVision` - Severe headache / blurred vision
- `cbSymptomSwelling` - Swelling of face / hands
- `cbSymptomFeverChills` - Fever with chills
- `cbSymptomReducedMovements` - Reduced baby movements
- `cbSymptomSevereAbdominalPain` - Severe abdominal pain
- `cbSymptomNone` - None of the above (mutually exclusive)

**BP Recording:**
- `switchBpRecorded` - Toggle for BP recording
- `etBpSystolic` - Systolic BP (conditional visibility)
- `etBpDiastolic` - Diastolic BP (conditional visibility)

**Weight:**
- `etWeightKg` - Weight in kg

#### D) Interventions
- `etIfaTablets` - Number of IFA tablets given
- `etCalciumTablets` - Number of calcium tablets given
- `autoTtDose` - TT/TD dose dropdown (None, First, Second, Booster)
- `switchReferralMade` - Toggle for referral
- `etReferralReason` - Referral reason (conditional visibility)
- `etNextVisitDate` - Next visit date picker (optional)

#### E) Save Button
- `btnSaveFollowUpAnc` - Full-width save button

### 2. **FollowUpAncVisitActivity.kt** (MODIFIED)
Location: `app/src/main/java/com/sukhayu/patient/asha/ui/surveys/pregnancy/FollowUpAncVisitActivity.kt`

**Features Implemented:**

#### ViewBinding Setup
- Uses `ActivityFollowUpAncVisitBinding` for type-safe view access
- Proper lifecycle management

#### Intent Extras
Reads the following extras:
- `EXTRA_PREGNANCY_ID` - Pregnancy record ID (for future use)
- `EXTRA_PATIENT_ID` - Patient ID
- `EXTRA_PATIENT_NAME` - Patient name
- `EXTRA_PATIENT_PHONE` - Patient phone
- `EXTRA_PATIENT_GENDER` - Patient gender
- `EXTRA_PATIENT_WEIGHT` - Patient weight

#### Date Pickers
- Visit date defaults to today
- Next visit date is optional
- Uses `SimpleDateFormat("dd/MM/yyyy")` for consistent formatting
- `DatePickerDialog` for user-friendly date selection

#### TT/TD Dose Dropdown
- `AutoCompleteTextView` with 4 options: None, First, Second, Booster
- Defaults to "None"

#### Conditional Field Visibility
1. **BP Recording Logic:**
   - When `switchBpRecorded` is OFF: BP fields are hidden and cleared
   - When ON: BP fields (Systolic & Diastolic) are shown

2. **Referral Logic:**
   - When `switchReferralMade` is OFF: Referral reason field is hidden and cleared
   - When ON: Referral reason field is shown

#### Symptoms Logic (Mutual Exclusivity)
- When "None of the above" is checked: All other symptoms are unchecked
- When any other symptom is checked: "None of the above" is unchecked

#### Validation (on Save)
Required fields:
- Visit date must not be blank
- Visit number must not be blank
- Facility type must be selected
- If BP recorded is ON: Systolic and Diastolic must not be blank
- If referral made is ON: Referral reason must not be blank

Validation feedback:
- Shows field-specific errors in TextInputLayout
- Shows Toast message if validation fails

#### Save Behavior (Current)
- Validates all required fields
- Shows success Toast: "Follow-up ANC Visit saved for [patientName]"
- **Note:** Does NOT save to database yet - ready for ViewModel integration

### 3. **PregnancySurveyActivity.kt** (MODIFIED)
Location: `app/src/main/java/com/sukhayu/patient/asha/ui/surveys/pregnancy/PregnancySurveyActivity.kt`

**Changes:**
- Updated `navigateToFollowUpAncVisit()` to pass all patient details:
  - Patient phone
  - Patient gender
  - Patient weight
- Also updated `navigateToFirstAncVisit()` for consistency
- Added TODO comment for future pregnancy ID tracking

## Design Patterns Used

1. **ViewBinding** - Type-safe view access
2. **Observer Pattern** - Ready for LiveData integration
3. **Material Design** - Consistent with app design
4. **Validation Pattern** - Inline field errors + Toast messages
5. **Conditional Visibility** - Dynamic UI based on user input

## Future Integration Steps

When you're ready to connect to the database:

1. **Create FollowUpAncVisitEntity**
   ```kotlin
   @Entity(tableName = "follow_up_anc_visits")
   data class FollowUpAncVisitEntity(
       @PrimaryKey val visitId: String = UUID.randomUUID().toString(),
       val pregnancyId: String,
       val visitDate: String,
       val visitNumber: Int,
       val facilityType: String,
       val symptoms: String, // JSON or comma-separated
       val bpRecorded: Boolean,
       val bpSystolic: Int?,
       val bpDiastolic: Int?,
       val weightKg: Float?,
       val ifaTabletsGiven: Int?,
       val calciumTabletsGiven: Int?,
       val ttDose: String,
       val referralMade: Boolean,
       val referralReason: String?,
       val nextVisitDate: String?,
       val createdAt: Long = System.currentTimeMillis(),
       val isSynced: Boolean = false
   )
   ```

2. **Create FollowUpAncVisitDao**
   ```kotlin
   @Dao
   interface FollowUpAncVisitDao {
       @Insert(onConflict = OnConflictStrategy.REPLACE)
       suspend fun insertVisit(visit: FollowUpAncVisitEntity): Long
       
       @Query("SELECT * FROM follow_up_anc_visits WHERE pregnancyId = :pregnancyId ORDER BY visitDate DESC")
       fun getVisitsForPregnancy(pregnancyId: String): Flow<List<FollowUpAncVisitEntity>>
   }
   ```

3. **Create FollowUpAncVisitViewModel**
   - Follow same pattern as `FirstAncVisitViewModel`
   - Handle save operation with LiveData states
   - Offline-first approach

4. **Update Activity**
   - Initialize ViewModel
   - Observe save states
   - Call `viewModel.saveFollowUpVisit(entity)` in `saveFollowUpAncVisit()`

## Testing Checklist

- [x] Layout renders correctly
- [x] ViewBinding works
- [x] Intent extras are read correctly
- [x] Patient details display in header
- [x] Date pickers work (visit date, next visit date)
- [x] Visit date defaults to today
- [x] TT/TD dropdown works
- [x] BP toggle shows/hides BP fields
- [x] Referral toggle shows/hides reason field
- [x] "None of the above" symptom logic works
- [x] Other symptoms uncheck "None of the above"
- [x] Validation shows appropriate errors
- [x] Save button shows success Toast
- [ ] Integration with Room database (pending)
- [ ] Pregnancy ID tracking (pending)

## Notes

- All warnings about hardcoded strings are acceptable during development
- Consider extracting strings to `strings.xml` before production
- The form is ready for immediate use with validation
- Database integration can be added without changing the UI
- Consistent with FirstAncVisitActivity patterns for easy maintenance

## Screenshots Locations
When testing, the form will show:
1. Patient header card with all details
2. Visit details card (date, number, facility type)
3. Current condition card (symptoms, BP, weight)
4. Interventions card (tablets, TT dose, referral)
5. Save button at bottom

All sections use Material Design cards with proper spacing and padding.

