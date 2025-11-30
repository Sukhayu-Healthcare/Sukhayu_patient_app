# General Survey Navigation Flow - Visual Guide

## Current Implementation (After Fix)

```
┌─────────────────────────────────────┐
│   AshaSurveyHomeActivity            │
│   (Dashboard / Create Menu)         │
│                                     │
│   [Pregnancy Survey]                │
│   [Child Survey]                    │
│   [TB Survey]                       │
│   [General Survey] ← Click here    │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ PatientSearchForGeneralSurveyActivity│
│                                     │
│  Search Patient:                    │
│  ┌─────────────────────────────┐   │
│  │ Enter name or phone...      │   │
│  └─────────────────────────────┘   │
│  [Search Patient]                   │
│                                     │
│  ℹ️ Instructions:                   │
│  1. Enter patient name/phone        │
│  2. Click Search Patient            │
│  3. Select from results             │
│  4. Fill General Survey form        │
└─────────────────────────────────────┘
              ↓
         (Search DB)
              ↓
    ┌─────────┴─────────┐
    │                   │
    ↓                   ↓
┌─────────┐      ┌─────────────┐
│ 1 Match │      │ Multiple    │
│         │      │ Matches     │
│ Auto    │      │ ┌─────────┐ │
│ Select  │      │ │ Choose: │ │
│         │      │ │ • Pat 1 │ │
└────┬────┘      │ │ • Pat 2 │ │
     │           │ │ • Pat 3 │ │
     │           │ └─────────┘ │
     │           └──────┬──────┘
     │                  │
     └────────┬─────────┘
              ↓
┌─────────────────────────────────────┐
│   GeneralSurveyActivity             │
│                                     │
│  Patient: John Doe                  │
│  Phone: 1234567890                  │
│  Gender: Male                       │
│                                     │
│  ── Section 1: Identification ──    │
│  Visit Date: [30/11/2025]          │
│  Location: [________]              │
│                                     │
│  ── Section 2: Conditions ──        │
│  Diabetes: ○ Yes ○ No              │
│  Hypertension: ○ Yes ○ No          │
│  ...                                │
│                                     │
│  [Save General Survey]              │
└─────────────────────────────────────┘
              ↓
         (Save to DB)
              ↓
    ✅ Saved Successfully!
              ↓
       (Back button)
              ↓
┌─────────────────────────────────────┐
│   AshaSurveyHomeActivity            │
│   (Returns to dashboard)            │
└─────────────────────────────────────┘
```

## Error Prevention

### Scenario 1: Try to open GeneralSurveyActivity directly
```
Direct Intent to GeneralSurveyActivity
              ↓
    (No patient ID provided)
              ↓
    readIntentExtrasAndPrefillForm()
              ↓
    if (patientId.isNullOrBlank())
              ↓
    ❌ Toast: "No patient selected"
              ↓
          finish()
              ↓
    (Activity closes immediately)
```

### Scenario 2: No patient found
```
PatientSearchForGeneralSurveyActivity
              ↓
    Search: "NonExistentPatient"
              ↓
    repository.searchPatients(query)
              ↓
    patients.isEmpty() == true
              ↓
    ❌ Show error message:
    "No patient found with name or phone: NonExistentPatient"
              ↓
    (User can search again)
```

## Data Flow

```
┌──────────────┐
│ User Input   │
│ "John"       │
└──────┬───────┘
       ↓
┌──────────────────────┐
│ PatientRepository    │
│ searchPatients()     │
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ Room Database        │
│ SELECT * FROM        │
│ patients WHERE       │
│ name LIKE '%John%'   │
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ List<PatientEntity>  │
│ [John Doe,           │
│  John Smith]         │
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ Show Chooser Dialog  │
│ "Select Patient"     │
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ Selected Patient     │
│ ID: "patient_123"    │
│ Name: "John Doe"     │
│ Phone: "1234567890"  │
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ Intent Extras        │
│ EXTRA_PATIENT_ID     │
│ EXTRA_PATIENT_NAME   │
│ EXTRA_PATIENT_PHONE  │
│ EXTRA_PATIENT_GENDER │
└──────┬───────────────┘
       ↓
┌──────────────────────┐
│ GeneralSurveyActivity│
│ (Form pre-filled)    │
└──────────────────────┘
```

## Key Validations

### ✅ Validation 1: Patient Search Activity
- Checks if search query is not empty
- Shows error if empty: "Please enter patient name or phone number"

### ✅ Validation 2: Patient Selection
- Requires at least one patient to be selected
- No navigation to form without patient

### ✅ Validation 3: General Survey Activity
- Checks `patientId` on activity start
- If missing → Shows toast → Calls `finish()`
- Prevents any form interaction without valid patient

## Activity Lifecycle

```
AshaSurveyHomeActivity (Running)
    ↓ startActivity(PatientSearchForGeneralSurveyActivity)
    
AshaSurveyHomeActivity (Background)
PatientSearchForGeneralSurveyActivity (Running)
    ↓ startActivity(GeneralSurveyActivity) + finish()
    
AshaSurveyHomeActivity (Background)
PatientSearchForGeneralSurveyActivity (Destroyed) ← finish() called
GeneralSurveyActivity (Running)
    ↓ User presses back
    
AshaSurveyHomeActivity (Running) ← Returns here
GeneralSurveyActivity (Destroyed)
```

## Benefits of This Approach

1. **Data Integrity**: Every survey is linked to a patient
2. **User-Friendly**: Clear step-by-step flow
3. **Offline-First**: Works without internet
4. **Reusable Pattern**: Can be applied to other surveys
5. **Error Prevention**: Multiple validation layers
6. **Clean Back Stack**: Proper activity lifecycle management

## Comparison with Other Survey Flows

### TB Survey (Similar Pattern)
```
TbSurveyActivity (patient search inline)
    ↓
TbScreeningActivity (with patient)
```

### General Survey (New Pattern)
```
PatientSearchForGeneralSurveyActivity (dedicated search)
    ↓
GeneralSurveyActivity (with patient)
```

### Pregnancy Survey (Needs Update)
```
PregnancySurveyActivity (direct access) ← Should be updated
```

### Child Survey (Needs Update)
```
ChildSurveyActivity (direct access) ← Should be updated
```

---

**Note:** This pattern should be replicated for Pregnancy and Child surveys to ensure consistent behavior across all survey types.

