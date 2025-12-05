# General Survey Template - Quick Reference Card

## 📋 Basic Info
- **Template ID:** `general_survey_template`
- **Version:** 1.0
- **File:** `app/src/main/assets/general_survey_template.json`
- **Total Fields:** 28
- **Sections:** 6

---

## 🗂️ Complete Field List (Alphabetical)

### A-E
- `alcohol_use` → Yes/No (Risk Factors)
- `blurred_vision` → Yes/No (Symptoms)
- `chest_pain` → Yes/No (Symptoms)
- `current_medication` → Yes/No (Service Use)
- `diabetes` → Yes/No (Existing Conditions)
- `excessive_thirst` → Yes/No (Symptoms)

### F-M
- `family_history` → Yes/No (Risk Factors)
- `fatigue` → Yes/No (Symptoms)
- `frequent_urination` → Yes/No (Symptoms)
- `heart_disease` → Yes/No (Existing Conditions)
- `hypertension` → Yes/No (Existing Conditions)
- `kidney_disease` → Yes/No (Existing Conditions)
- `location` → Text (Identification)
- `medication_details` → Text (Service Use) *conditional*

### O-S
- `other_conditions` → Text (Existing Conditions)
- `physical_activity` → Active/Moderate/Sedentary (Risk Factors)
- `recent_bp_check` → Yes/No (Service Use)
- `recent_sugar_check` → Yes/No (Service Use)
- `referral_facility` → PHC/CHC/District/Other (ASHA) *conditional*
- `referral_needed` → Yes/No (ASHA Assessment)
- `regular_checkups` → Yes/No (Service Use)
- `remarks` → Textarea (ASHA Assessment)
- `shortness_of_breath` → Yes/No (Symptoms)
- `stroke` → Yes/No (Existing Conditions)

### T-W
- `tobacco_use` → Yes/No (Risk Factors)
- `unhealthy_diet` → Yes/No (Risk Factors)
- `visit_date` → Date (Identification) **REQUIRED**
- `weight_loss` → Yes/No (Symptoms)

---

## 📊 Fields by Section

### 1. Identification (2)
```
visit_date ⭐ | location
```

### 2. Existing Conditions (6)
```
diabetes | hypertension | heart_disease | stroke
kidney_disease | other_conditions
```

### 3. Symptoms (7)
```
frequent_urination | excessive_thirst | weight_loss
blurred_vision | chest_pain | shortness_of_breath
fatigue
```

### 4. Risk Factors (5)
```
family_history | tobacco_use | alcohol_use
physical_activity | unhealthy_diet
```

### 5. Service Use (5)
```
regular_checkups | current_medication
medication_details 🔗 | recent_bp_check
recent_sugar_check
```

### 6. ASHA Assessment (3)
```
referral_needed | referral_facility 🔗 | remarks
```

---

## 🔤 Field Types

| Type | Count | Fields |
|------|------:|--------|
| select (Yes/No) | 20 | Most fields |
| select (Multi) | 2 | physical_activity, referral_facility |
| text | 3 | location, other_conditions, medication_details |
| textarea | 1 | remarks |
| date | 1 | visit_date |

---

## 🔗 Conditional Fields

| Field | Shows When |
|-------|------------|
| `medication_details` | `current_medication` = "Yes" |
| `referral_facility` | `referral_needed` = "Yes" |

---

## 📝 Required Fields

Only **1 required field:**
- `visit_date` (dd/MM/yyyy format)

All other fields are optional.

---

## 🎯 Quick Copy-Paste

### Sample Field Structure
```json
{
  "key": "field_name",
  "label": "Field Label",
  "type": "select",
  "options": ["Yes", "No"],
  "required": false
}
```

### Conditional Field
```json
{
  "key": "conditional_field",
  "label": "Conditional Field",
  "type": "text",
  "required": false,
  "conditional": {
    "dependsOn": "parent_field",
    "showWhen": "Yes"
  }
}
```

---

## 🚀 Usage

### Load in Kotlin
```kotlin
val json = assets.open("general_survey_template.json")
    .bufferedReader().use { it.readText() }
val template = Gson().fromJson(json, GeneralSurveyTemplate::class.java)
```

### Access Fields
```kotlin
template.sections.forEach { section ->
    println("Section: ${section.sectionName}")
    section.fields.forEach { field ->
        println("  ${field.label}: ${field.type}")
    }
}
```

---

## 📐 Validation Rules

1. `visit_date` is required
2. Select fields must use specified options only
3. Conditional fields validate only if parent condition met
4. Date must be in dd/MM/yyyy format

---

## 🎨 Color Key (for UI)

- 🟢 **Identification** - Green theme
- 🔵 **Existing Conditions** - Blue theme
- 🟣 **Symptoms** - Purple theme
- 🟠 **Risk Factors** - Orange theme
- 🟡 **Service Use** - Yellow theme
- 🔴 **ASHA Assessment** - Red theme

---

## 📈 Statistics

- **Yes/No Questions:** 71% (20/28)
- **Text Input:** 14% (4/28)
- **Multi-Option:** 7% (2/28)
- **Date:** 4% (1/28)
- **Textarea:** 4% (1/28)

---

## ✅ Completeness Checklist

- [x] All 6 sections defined
- [x] All 28 fields specified
- [x] All field properties complete
- [x] Conditional logic documented
- [x] Required fields marked
- [x] Options specified for select fields
- [x] Format specified for date fields
- [x] JSON syntax validated

---

**Quick Reference v1.0 | November 30, 2025**

