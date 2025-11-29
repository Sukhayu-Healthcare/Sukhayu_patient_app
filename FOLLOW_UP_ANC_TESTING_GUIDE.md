# Follow-up ANC Visit - Manual Testing Guide

## Prerequisites
- Build and run the app on an emulator or device
- Navigate to Pregnancy Survey Activity
- Load a patient's details
- Select "Follow-up ANC Visit" from the dropdown

## Test Scenarios

### 1. Patient Header Display
**Steps:**
1. Open Follow-up ANC Visit from Pregnancy Survey
2. Verify patient header shows:
   - Name
   - Phone
   - Gender
   - Weight

**Expected:** All patient details display correctly with "Name: [value]" format

---

### 2. Date Picker - Visit Date
**Steps:**
1. Tap on "Visit Date" field
2. Verify DatePickerDialog appears
3. Select a date
4. Verify date displays in dd/MM/yyyy format

**Expected:** 
- Default date is today's date
- Selected date shows in field
- Format is correct (e.g., 29/11/2025)

---

### 3. Visit Number Input
**Steps:**
1. Tap on "Visit Number" field
2. Type a number (e.g., 2, 3, 4)

**Expected:** 
- Only numeric input accepted
- Max 2 digits

---

### 4. Facility Type Selection
**Steps:**
1. Try selecting each radio button:
   - Govt facility
   - Private
   - Home visit

**Expected:** Only one can be selected at a time

---

### 5. Symptoms Checkboxes - Normal Selection
**Steps:**
1. Check "Vaginal bleeding"
2. Check "Fever with chills"

**Expected:** Both checkboxes remain checked

---

### 6. Symptoms Checkboxes - "None of the Above" Logic
**Steps:**
1. Check several symptoms (e.g., bleeding, swelling)
2. Then check "None of the above"

**Expected:** All other symptom checkboxes get unchecked automatically

---

### 7. Symptoms Checkboxes - Reverse Logic
**Steps:**
1. Check "None of the above"
2. Then check any other symptom (e.g., "Severe headache")

**Expected:** "None of the above" gets unchecked automatically

---

### 8. BP Recording - Toggle OFF
**Steps:**
1. Ensure "BP recorded?" switch is OFF
2. Verify BP fields (Systolic, Diastolic) are not visible

**Expected:** BP fields are hidden

---

### 9. BP Recording - Toggle ON
**Steps:**
1. Toggle "BP recorded?" switch ON
2. Verify BP fields appear
3. Enter values in Systolic and Diastolic

**Expected:** 
- Fields become visible
- Accept numeric input only
- Max 3 digits each

---

### 10. BP Recording - Toggle OFF After Input
**Steps:**
1. Toggle BP switch ON
2. Enter values in BP fields
3. Toggle switch OFF
4. Toggle switch ON again

**Expected:** 
- Fields are cleared when toggled off
- Fields are empty when toggled back on

---

### 11. Weight Input
**Steps:**
1. Tap "Weight (kg)" field
2. Enter a decimal value (e.g., 55.5)

**Expected:** 
- Accepts decimal input
- Max 5 characters

---

### 12. IFA and Calcium Tablets
**Steps:**
1. Enter numbers in both fields

**Expected:** 
- Numeric input only
- Max 3 digits

---

### 13. TT/TD Dose Dropdown
**Steps:**
1. Tap on "TT/TD dose" field
2. Verify dropdown shows: None, First, Second, Booster
3. Select "First"

**Expected:** 
- Dropdown shows all options
- Selected value displays
- Default is "None"

---

### 14. Referral - Toggle OFF
**Steps:**
1. Ensure "Any referral made?" switch is OFF
2. Verify referral reason field is not visible

**Expected:** Referral reason field is hidden

---

### 15. Referral - Toggle ON
**Steps:**
1. Toggle "Any referral made?" switch ON
2. Verify referral reason field appears
3. Enter text

**Expected:** 
- Field becomes visible
- Accepts text input
- Max 100 characters

---

### 16. Referral - Toggle OFF After Input
**Steps:**
1. Toggle referral switch ON
2. Enter text in reason field
3. Toggle switch OFF
4. Toggle switch ON again

**Expected:** 
- Field is cleared when toggled off
- Field is empty when toggled back on

---

### 17. Next Visit Date (Optional)
**Steps:**
1. Tap on "Next visit date" field
2. Select a future date from picker
3. Leave it blank and proceed

**Expected:** 
- Date picker works
- Field can remain empty (optional)

---

### 18. Validation - Empty Visit Date
**Steps:**
1. Clear visit date field
2. Tap "Save Follow-up Visit"

**Expected:** 
- Error message appears on field
- Toast: "Please fill all required fields"
- Save does not proceed

---

### 19. Validation - Empty Visit Number
**Steps:**
1. Leave visit number blank
2. Tap "Save Follow-up Visit"

**Expected:** 
- Error message on field
- Toast message shown
- Save does not proceed

---

### 20. Validation - No Facility Type Selected
**Steps:**
1. Fill all other required fields
2. Don't select any facility type
3. Tap "Save Follow-up Visit"

**Expected:** 
- Toast: "Please select facility type"
- Save does not proceed

---

### 21. Validation - BP Recorded but Empty Fields
**Steps:**
1. Toggle "BP recorded?" ON
2. Leave BP fields empty
3. Tap "Save Follow-up Visit"

**Expected:** 
- Errors on BP fields: "Required when BP recorded"
- Toast message shown
- Save does not proceed

---

### 22. Validation - Referral Made but Empty Reason
**Steps:**
1. Toggle "Any referral made?" ON
2. Leave reason field empty
3. Tap "Save Follow-up Visit"

**Expected:** 
- Error on reason field: "Required when referral made"
- Toast message shown
- Save does not proceed

---

### 23. Successful Save - Minimal Required Fields
**Steps:**
1. Fill only required fields:
   - Visit date (default today)
   - Visit number: 2
   - Facility type: Home visit
2. Leave all other fields empty/unchecked
3. Tap "Save Follow-up Visit"

**Expected:** 
- No errors
- Toast: "Follow-up ANC Visit saved for [Patient Name]"
- Form accepts save

---

### 24. Successful Save - All Fields Filled
**Steps:**
1. Fill all fields:
   - Visit date, number, facility type
   - Check some symptoms
   - Toggle BP ON and enter values
   - Enter weight
   - Enter IFA/Calcium tablets
   - Select TT dose
   - Toggle referral ON and enter reason
   - Set next visit date
2. Tap "Save Follow-up Visit"

**Expected:** 
- All validations pass
- Success toast appears
- Form accepts save

---

### 25. Back Navigation
**Steps:**
1. Tap back arrow in toolbar
2. Verify returns to Pregnancy Survey Activity

**Expected:** 
- Returns to previous screen
- No crash

---

### 26. Scrolling
**Steps:**
1. Fill form and scroll to bottom
2. Verify all sections are accessible
3. Save button is visible at bottom

**Expected:** 
- Smooth scrolling
- No content cut off
- Save button accessible

---

## Bug Report Template

If you find any issues, report them with this format:

```
**Test Case:** [Number and name]
**Steps to Reproduce:**
1. 
2. 
3. 

**Expected Result:**

**Actual Result:**

**Screenshots:** (if applicable)
```

## Notes
- The form does NOT save to database yet - only validates and shows success Toast
- All warnings about hardcoded strings are expected during development
- Database integration is pending (see FOLLOW_UP_ANC_IMPLEMENTATION.md for details)

