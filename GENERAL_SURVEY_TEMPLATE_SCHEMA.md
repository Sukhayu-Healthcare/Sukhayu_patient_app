# General Survey Template JSON Schema - Complete

## ✅ COMPLETE - November 30, 2025

---

## 📋 Overview

Created a comprehensive JSON schema for the **General Health Survey** template following the same format and structure as the TB templates in the system.

**Template ID:** `general_survey_template`  
**Version:** 1.0  
**File Location:** `app/src/main/assets/general_survey_template.json`

---

## 🎯 Template Structure

### Metadata
```json
{
  "templateId": "general_survey_template",
  "templateName": "General Health Survey",
  "version": "1.0",
  "description": "Community health screening for chronic conditions, symptoms, risk factors, and service utilization"
}
```

### Sections
The template contains **6 sections** with a total of **28 fields**.

---

## 📊 Section Breakdown

### Section 1: Identification (2 fields)

| Key | Label | Type | Required | Options/Format |
|-----|-------|------|----------|----------------|
| `visit_date` | Visit Date | date | ✅ Yes | dd/MM/yyyy |
| `location` | Location/Village | text | No | - |

**Purpose:** Record basic survey information and location.

---

### Section 2: Existing Conditions (6 fields)

| Key | Label | Type | Required | Options |
|-----|-------|------|----------|---------|
| `diabetes` | Diabetes | select | No | Yes, No |
| `hypertension` | Hypertension (High BP) | select | No | Yes, No |
| `heart_disease` | Heart Disease | select | No | Yes, No |
| `stroke` | Stroke | select | No | Yes, No |
| `kidney_disease` | Kidney Disease | select | No | Yes, No |
| `other_conditions` | Other Conditions (if any) | text | No | - |

**Purpose:** Document known chronic conditions.

---

### Section 3: Current Symptoms (7 fields)

| Key | Label | Type | Required | Options |
|-----|-------|------|----------|---------|
| `frequent_urination` | Frequent Urination | select | No | Yes, No |
| `excessive_thirst` | Excessive Thirst | select | No | Yes, No |
| `weight_loss` | Unexplained Weight Loss | select | No | Yes, No |
| `blurred_vision` | Blurred Vision | select | No | Yes, No |
| `chest_pain` | Chest Pain | select | No | Yes, No |
| `shortness_of_breath` | Shortness of Breath | select | No | Yes, No |
| `fatigue` | Fatigue/Weakness | select | No | Yes, No |

**Purpose:** Screen for active symptoms requiring attention.

---

### Section 4: Risk Factors (5 fields)

| Key | Label | Type | Required | Options |
|-----|-------|------|----------|---------|
| `family_history` | Family History of Diabetes/BP | select | No | Yes, No |
| `tobacco_use` | Tobacco Use | select | No | Yes, No |
| `alcohol_use` | Alcohol Use | select | No | Yes, No |
| `physical_activity` | Physical Activity Level | select | No | Active, Moderate, Sedentary |
| `unhealthy_diet` | Diet (High Salt/Sugar/Oil) | select | No | Yes, No |

**Purpose:** Identify behavioral and hereditary risk factors.

---

### Section 5: Service Use (5 fields)

| Key | Label | Type | Required | Options | Conditional |
|-----|-------|------|----------|---------|-------------|
| `regular_checkups` | Regular Health Check-ups | select | No | Yes, No | - |
| `current_medication` | Currently on Medication | select | No | Yes, No | - |
| `medication_details` | Medication Details (if yes) | text | No | - | Shows if `current_medication` = "Yes" |
| `recent_bp_check` | Last BP Check (within 6 months) | select | No | Yes, No | - |
| `recent_sugar_check` | Last Blood Sugar Check (within 6 months) | select | No | Yes, No | - |

**Purpose:** Track healthcare utilization and screening history.

**Conditional Logic:**
- `medication_details` only appears when `current_medication` is "Yes"

---

### Section 6: ASHA Assessment (3 fields)

| Key | Label | Type | Required | Options | Conditional |
|-----|-------|------|----------|---------|-------------|
| `referral_needed` | Referral Needed | select | No | Yes, No | - |
| `referral_facility` | Referral Facility | select | No | PHC, CHC, District Hospital, Other | Shows if `referral_needed` = "Yes" |
| `remarks` | Remarks/Notes | textarea | No | - | - |

**Purpose:** ASHA's professional assessment and follow-up planning.

**Conditional Logic:**
- `referral_facility` only appears when `referral_needed` is "Yes"

---

## 📐 Field Type Reference

### Supported Field Types

| Type | Description | Example |
|------|-------------|---------|
| `text` | Single-line text input | Location, Other Conditions |
| `textarea` | Multi-line text input | Remarks/Notes |
| `select` | Radio button or dropdown selection | Yes/No, facility options |
| `date` | Date picker with format | Visit Date (dd/MM/yyyy) |

---

## 🔗 Conditional Fields

The template supports conditional field visibility:

```json
{
  "key": "medication_details",
  "conditional": {
    "dependsOn": "current_medication",
    "showWhen": "Yes"
  }
}
```

**Implemented Conditions:**
1. **Medication Details** → Shows when "Currently on Medication" = "Yes"
2. **Referral Facility** → Shows when "Referral Needed" = "Yes"

---

## 📊 Template Statistics

| Metric | Count |
|--------|------:|
| **Total Sections** | 6 |
| **Total Fields** | 28 |
| **Required Fields** | 1 (visit_date) |
| **Optional Fields** | 27 |
| **Yes/No Fields** | 20 |
| **Multi-Option Fields** | 2 |
| **Text Fields** | 3 |
| **Textarea Fields** | 1 |
| **Date Fields** | 1 |
| **Conditional Fields** | 2 |

---

## 🔧 Usage Examples

### 1. Reading the Template in Android

```kotlin
// In GeneralSurveyActivity.kt or ViewModel
fun loadTemplate(): GeneralSurveyTemplate {
    val json = assets.open("general_survey_template.json").bufferedReader().use { it.readText() }
    val gson = Gson()
    return gson.fromJson(json, GeneralSurveyTemplate::class.java)
}
```

### 2. Data Classes for Template

```kotlin
data class GeneralSurveyTemplate(
    val templateId: String,
    val templateName: String,
    val version: String,
    val description: String,
    val sections: List<TemplateSection>
)

data class TemplateSection(
    val sectionId: String,
    val sectionName: String,
    val fields: List<TemplateField>
)

data class TemplateField(
    val key: String,
    val label: String,
    val type: String,
    val required: Boolean,
    val options: List<String>? = null,
    val format: String? = null,
    val conditional: ConditionalRule? = null
)

data class ConditionalRule(
    val dependsOn: String,
    val showWhen: String
)
```

### 3. Validating Against Template

```kotlin
fun validateFormData(data: Map<String, Any?>, template: GeneralSurveyTemplate): Boolean {
    template.sections.forEach { section ->
        section.fields.forEach { field ->
            if (field.required && data[field.key] == null) {
                throw ValidationException("${field.label} is required")
            }
        }
    }
    return true
}
```

### 4. Generating UI from Template (Dynamic Forms)

```kotlin
fun generateFormFields(section: TemplateSection, container: LinearLayout) {
    section.fields.forEach { field ->
        when (field.type) {
            "text" -> addTextInputField(field, container)
            "textarea" -> addTextAreaField(field, container)
            "select" -> addRadioGroupField(field, container)
            "date" -> addDatePickerField(field, container)
        }
    }
}
```

---

## 🎯 Template Design Principles

### 1. Compact Structure
- Minimal nesting
- Clear key names (snake_case)
- No redundant fields

### 2. ASHA-Friendly
- Short, clear labels
- Mostly Yes/No options
- Simple field types

### 3. Extensible
- Easy to add new sections
- Support for conditional logic
- Version tracking

### 4. Consistent with TB Templates
- Same JSON structure
- Same field property names
- Same conditional format

---

## 📝 Field Naming Convention

### Keys (snake_case)
- `visit_date` ✅
- `frequent_urination` ✅
- `current_medication` ✅

### Labels (Title Case, Human-Readable)
- "Visit Date" ✅
- "Frequent Urination" ✅
- "Currently on Medication" ✅

### Options (Title Case)
- "Yes", "No" ✅
- "Active", "Moderate", "Sedentary" ✅
- "PHC", "CHC", "District Hospital", "Other" ✅

---

## 🔄 Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Nov 30, 2025 | Initial release with 6 sections, 28 fields |

---

## 🚀 Integration Status

### Current Status: ✅ READY
- [x] JSON schema created
- [x] All 6 sections defined
- [x] All 28 fields specified
- [x] Conditional logic documented
- [x] File saved in assets folder
- [x] Documentation complete

### Next Steps (Future Implementation)
- [ ] Create Kotlin data classes for template
- [ ] Create template loader utility
- [ ] Wire template to GeneralSurveyActivity
- [ ] Generate UI from template (optional - currently hardcoded)
- [ ] Add template validation in ViewModel
- [ ] Export form data matching template structure

---

## 📄 Complete Template JSON

The complete JSON file is located at:
```
app/src/main/assets/general_survey_template.json
```

### Quick View (Abbreviated)
```json
{
  "templateId": "general_survey_template",
  "templateName": "General Health Survey",
  "version": "1.0",
  "sections": [
    {
      "sectionId": "identification",
      "fields": [...]
    },
    {
      "sectionId": "existing_conditions",
      "fields": [...]
    },
    {
      "sectionId": "symptoms",
      "fields": [...]
    },
    {
      "sectionId": "risk_factors",
      "fields": [...]
    },
    {
      "sectionId": "service_use",
      "fields": [...]
    },
    {
      "sectionId": "asha_assessment",
      "fields": [...]
    }
  ]
}
```

---

## 🎨 Template Visualization

```
general_survey_template
│
├── Section 1: Identification (2 fields)
│   ├── visit_date (date, required) ⭐
│   └── location (text)
│
├── Section 2: Existing Conditions (6 fields)
│   ├── diabetes (Yes/No)
│   ├── hypertension (Yes/No)
│   ├── heart_disease (Yes/No)
│   ├── stroke (Yes/No)
│   ├── kidney_disease (Yes/No)
│   └── other_conditions (text)
│
├── Section 3: Current Symptoms (7 fields)
│   ├── frequent_urination (Yes/No)
│   ├── excessive_thirst (Yes/No)
│   ├── weight_loss (Yes/No)
│   ├── blurred_vision (Yes/No)
│   ├── chest_pain (Yes/No)
│   ├── shortness_of_breath (Yes/No)
│   └── fatigue (Yes/No)
│
├── Section 4: Risk Factors (5 fields)
│   ├── family_history (Yes/No)
│   ├── tobacco_use (Yes/No)
│   ├── alcohol_use (Yes/No)
│   ├── physical_activity (Active/Moderate/Sedentary)
│   └── unhealthy_diet (Yes/No)
│
├── Section 5: Service Use (5 fields)
│   ├── regular_checkups (Yes/No)
│   ├── current_medication (Yes/No)
│   ├── medication_details (text) 🔗 conditional
│   ├── recent_bp_check (Yes/No)
│   └── recent_sugar_check (Yes/No)
│
└── Section 6: ASHA Assessment (3 fields)
    ├── referral_needed (Yes/No)
    ├── referral_facility (PHC/CHC/District/Other) 🔗 conditional
    └── remarks (textarea)

Legend:
⭐ Required field
🔗 Conditional field (depends on another field)
```

---

## 📦 Export Format

When exporting survey data, use this JSON structure:

```json
{
  "templateId": "general_survey_template",
  "templateVersion": "1.0",
  "surveyId": "uuid-here",
  "patientId": "patient-id-here",
  "surveyDate": "30/11/2025",
  "data": {
    "identification": {
      "visit_date": "30/11/2025",
      "location": "Village Name"
    },
    "existing_conditions": {
      "diabetes": "Yes",
      "hypertension": "No",
      "heart_disease": "No",
      "stroke": "No",
      "kidney_disease": "No",
      "other_conditions": ""
    },
    "symptoms": {
      "frequent_urination": "Yes",
      "excessive_thirst": "Yes",
      "weight_loss": "No",
      "blurred_vision": "No",
      "chest_pain": "No",
      "shortness_of_breath": "No",
      "fatigue": "Yes"
    },
    "risk_factors": {
      "family_history": "Yes",
      "tobacco_use": "No",
      "alcohol_use": "No",
      "physical_activity": "Moderate",
      "unhealthy_diet": "Yes"
    },
    "service_use": {
      "regular_checkups": "No",
      "current_medication": "Yes",
      "medication_details": "Metformin 500mg BD",
      "recent_bp_check": "Yes",
      "recent_sugar_check": "Yes"
    },
    "asha_assessment": {
      "referral_needed": "Yes",
      "referral_facility": "PHC",
      "remarks": "Patient needs BP monitoring and diabetes counselling"
    }
  }
}
```

---

## ✅ Validation Rules

### Field-Level Validation
1. **visit_date** - Must be in dd/MM/yyyy format, required
2. **select fields** - Must be one of the specified options
3. **conditional fields** - Only validate if parent condition is met

### Form-Level Validation
1. At least one field must have data (besides visit_date)
2. If `current_medication` = "Yes", `medication_details` recommended
3. If `referral_needed` = "Yes", `referral_facility` recommended

---

## 🎉 Success Summary

### What Was Delivered
- ✅ Complete JSON schema with 6 sections
- ✅ 28 well-defined fields
- ✅ Conditional logic for 2 fields
- ✅ Consistent with TB template format
- ✅ ASHA-optimized field structure
- ✅ Comprehensive documentation
- ✅ Usage examples provided
- ✅ Export format specified

### Template Quality
- ✅ Compact and efficient
- ✅ Easy to parse
- ✅ Extensible structure
- ✅ Clear naming conventions
- ✅ Production-ready

---

## 📚 Related Files

1. **GeneralSurveyActivity.kt** - Uses TEMPLATE_ID = "general_survey_template"
2. **activity_general_survey.xml** - UI implementation of template fields
3. **GENERAL_SURVEY_FORM_IMPLEMENTATION.md** - UI documentation
4. **This file** - Template schema documentation

---

**Step 3 Complete!** The General Survey template JSON schema is ready for use. 🎉

