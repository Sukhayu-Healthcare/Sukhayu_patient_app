# NCD to General Survey Refactoring - Complete

## Overview
Successfully refactored the NCD Survey feature to "General Survey" by renaming all classes, files, packages, and references.

## Changes Made

### 1. Package Structure
**Old:** `com.sukhayu.patient.asha.ui.surveys.ncd`
**New:** `com.sukhayu.patient.asha.ui.surveys.general_survey`

### 2. Activity Class
**Old:** `NcdSurveyActivity.kt`
**New:** `GeneralSurveyActivity.kt`
- Class name: `NcdSurveyActivity` → `GeneralSurveyActivity`
- Package: Updated to `general_survey`
- Layout reference: `activity_ncd_survey` → `activity_general_survey`

### 3. Layout Files
**Old:** `activity_ncd_survey.xml`
**New:** `activity_general_survey.xml`
- Updated display text from "NCD Survey – coming soon" to "General Survey – coming soon"

### 4. AndroidManifest.xml
- Activity declaration updated:
  - Class: `com.sukhayu.patient.asha.ui.surveys.ncd.NcdSurveyActivity` → `com.sukhayu.patient.asha.ui.surveys.general_survey.GeneralSurveyActivity`
  - Label: "NCD Screening" → "General Screening"

### 5. AshaSurveyHomeActivity.kt
- Import statement updated: `NcdSurveyActivity` → `GeneralSurveyActivity`
- Button ID reference: `btnNcdSurvey` → `btnGeneralSurvey`
- Intent target: `NcdSurveyActivity::class.java` → `GeneralSurveyActivity::class.java`

### 6. activity_asha_survey_home.xml
- Button ID: `btnNcdSurvey` → `btnGeneralSurvey`
- Card title: "NCD Screening" → "General Screening"
- Content description: "NCD Survey" → "General Survey"

## Verification
✅ No references to "Ncd", "ncd", "NcdSurvey", "ncd_survey", or "btnNcdSurvey" remain in the codebase
✅ All references to "GeneralSurvey", "general_survey", and "btnGeneralSurvey" are properly implemented
✅ No compilation errors introduced by the refactoring
✅ Old files and directories have been deleted

## Files Created
1. `app/src/main/java/com/sukhayu/patient/asha/ui/surveys/general_survey/GeneralSurveyActivity.kt`
2. `app/src/main/res/layout/activity_general_survey.xml`

## Files Modified
1. `app/src/main/AndroidManifest.xml`
2. `app/src/main/java/com/sukhayu/patient/asha/ui/surveys/AshaSurveyHomeActivity.kt`
3. `app/src/main/res/layout/activity_asha_survey_home.xml`

## Files Deleted
1. `app/src/main/java/com/sukhayu/patient/asha/ui/surveys/ncd/NcdSurveyActivity.kt`
2. `app/src/main/res/layout/activity_ncd_survey.xml`
3. `app/src/main/java/com/sukhayu/patient/asha/ui/surveys/ncd/` (directory)

## Next Steps
The refactoring is complete and ready for further feature development. The General Survey feature now has:
- A clean, consistent naming convention
- Proper package structure
- All references updated throughout the codebase
- No logic changes (as requested)

The feature is now ready for Step 2 implementation where business logic and functionality can be added.

