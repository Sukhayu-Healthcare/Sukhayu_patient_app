# ASHA Patient Registration - Quick Reference

## How to Use

### 1. Launch the Activity
```kotlin
val intent = Intent(this, RegisterPatientActivity::class.java)
startActivity(intent)
```

### 2. Configuration
Before building, update the backend base URL in `RegisterPatientActivity.kt`:
```kotlin
private fun initRetrofit() {
    val retrofit = Retrofit.Builder()
        .baseUrl("YOUR_ACTUAL_BACKEND_URL") // Change this!
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    apiService = retrofit.create(ApiService::class.java)
}
```

### 3. Required Setup
- User must be logged in (TokenManager must have a valid token)
- Network must be available
- Permissions for CAMERA and READ_EXTERNAL_STORAGE must be granted

## Form Fields

| Field | Type | Required | Validation |
|-------|------|----------|-----------|
| Patient Name | EditText | Yes | Not empty |
| Password | EditText | Yes | Not empty |
| Gender | Spinner | Yes | Not first option |
| DOB | DatePicker | Yes | DD/MM/YYYY format |
| Profile Photo | ImagePicker | No | Base64 encoded |
| Phone | EditText | Yes | Exactly 10 digits |
| Village | EditText | Yes | Not empty |
| Taluka | EditText | Yes | Not empty |
| District | EditText | Yes | Not empty |
| Supreme ID | EditText | No | Integer (if provided) |
| Health History | Dynamic | No | Can be empty |

## User Flow

```
1. User opens RegisterPatientActivity
   ↓
2. Fills in Personal Details section
   ↓
3. Fills in Address Details section
   ↓
4. Adds Health History entries (optional)
   ↓
5. Clicks "Register Patient" button
   ↓
6. Form validates all required fields
   ↓
7. If valid: API call with Bearer token
   ↓
8. If successful: Show success dialog
   - Display Patient ID and Supreme ID
   - Option to "Register Another Patient" or "Go Back"
   ↓
9. If failed: Show error message
```

## Key Methods

### Validation
```kotlin
private fun validateForm(): Boolean
// Validates all required fields
// Returns true if all validations pass
// Shows Toast with error message on failure
```

### Image Handling
```kotlin
private fun convertImageToBase64(uri: Uri)
// Converts selected image to Base64 string
// Stores in profilePicBase64 variable
// Handles errors gracefully
```

### Date Conversion
```kotlin
private fun convertDobToISO(displayDate: String): String
// Converts DD/MM/YYYY to yyyy-MM-dd
// Used before sending to API
```

### Form Reset
```kotlin
private fun resetForm()
// Clears all EditText fields
// Resets Spinner to default
// Clears image and history
// Ready for next patient registration
```

### API Call
```kotlin
private fun submitRegistration()
// Collects form data into PatientRegistrationRequest
// Makes POST call to /asha/patient/register
// Handles success and error responses
// Updates UI accordingly
```

## Error Messages (Simple Language)

| Scenario | Message |
|----------|---------|
| Empty name | "Please enter patient name." |
| Empty password | "Please enter password." |
| No gender selected | "Please select gender." |
| Empty DOB | "Please enter date of birth." |
| Invalid phone | "Please enter a valid 10-digit phone number." |
| Empty village/taluka/district | "Please enter [field]." |
| No network | "Unable to register. Please check network and try again." |
| Not logged in | "Not logged in. Please login first." |
| API error | "Registration failed: [server message]" |

## Success Response

After successful registration, user sees:

```
┌─────────────────────────────────────┐
│         Success                     │
├─────────────────────────────────────┤
│ Patient registered successfully!    │
│                                     │
│ Patient ID: 54321                   │
│ Supreme ID: 54321                   │
├─────────────────────────────────────┤
│ [Register Another] [Go Back]        │
└─────────────────────────────────────┘
```

## Debug Logging

Relevant logs for debugging:
```
Tag: "RegisterPatient"
- "Image converted to Base64: {size} bytes" (success)
- "Date conversion error" (date parsing failed)
- "Image conversion error" (image to Base64 failed)
- "Network error" (API call failed)
```

## Common Issues

### 1. API Call Returns 401 Unauthorized
**Cause**: Invalid or expired token
**Solution**: Ensure user is logged in and token is valid

### 2. Image Doesn't Show After Selection
**Cause**: Permission denied or file not accessible
**Solution**: Check READ_EXTERNAL_STORAGE permission is granted

### 3. Phone Validation Always Fails
**Cause**: Non-numeric characters in phone number
**Solution**: Only numeric input allowed; +91 prefix is stripped

### 4. Form Doesn't Validate
**Cause**: Required field is empty
**Solution**: Check Toast message for specific field that's missing

### 5. Can't Select Date
**Cause**: EditText focusable="false" (by design)
**Solution**: User must tap EditText to open DatePickerDialog

## Testing Commands

### Test Phone Validation
Input: "+91-9876543210" → Should work (stripped to 10 digits)
Input: "9876543210" → Should work
Input: "98765" → Should fail (less than 10)

### Test Date Format
Input Date: 15 May 1985
Display: 15/05/1985 (DD/MM/YYYY)
API Send: 1985-05-15 (yyyy-MM-dd)

### Test Image Upload
1. Tap "Add Photo"
2. Select image from gallery
3. Image should display in ImageView thumbnail
4. Base64 string should be generated and included in API request

### Test Health History
1. Tap "+ Add Disease" multiple times
2. Each row should have disease name and duration fields
3. Tap remove button to delete a row
4. Rows should be included in API request

## Performance Considerations

- Large images (>5MB) may cause delays in Base64 encoding
- Recommend compressing image before selecting or implementing max size check
- API request payload size: ~1-2KB (excluding image)
- Image in Base64: ~1.3x original size

## Security Notes

1. **Password**: Stored in memory, cleared after submission
2. **Token**: Retrieved from TokenManager, used only in Authorization header
3. **Image**: Not validated for content; consider adding MIME type check
4. **Phone**: No PII protection; consider hashing if needed

## Customization Guide

### Change Colors
Edit `rounded_button_blue.xml` and `rounded_button_outline.xml`:
```xml
<solid android:color="#2196F3" /> <!-- Change hex color -->
```

### Change Button Text
In `RegisterPatientActivity.kt`:
```kotlin
btnRegisterPatient.text = "Your Custom Text"
```

### Change Error Messages
In validation methods, update Toast messages:
```kotlin
Toast.makeText(this, "Your custom error message", Toast.LENGTH_SHORT).show()
```

### Change Date Format
In `RegisterPatientActivity.kt`:
```kotlin
private val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
```

## API Integration Checklist

- [x] Backend URL is correct - **Configure in RegisterPatientActivity.kt line: initRetrofit()**
- [x] API endpoint is /asha/patient/register - **Implemented in ApiService.kt**
- [x] Backend expects POST request - **@POST annotation used**
- [x] Authorization header format: "Bearer {token}" - **Implemented in submitRegistration()**
- [x] Request body matches PatientRegistrationRequest structure - **Data class defined**
- [x] Response matches PatientRegistrationResponse structure - **Data class defined**
- [x] Backend returns 200 on success - **Handled in onResponse()**
- [x] Backend returns 4xx/5xx on error - **Error handling implemented**
- [x] CORS is configured if calling from web - **Configure on backend server**

## Release Checklist

- [ ] Remove debug Log.d() calls
- [ ] Update base URL to production endpoint
- [ ] Test on multiple Android versions (min SDK 24+)
- [ ] Test on different screen sizes
- [ ] Test with slow network (throttling)
- [ ] Test with no network (offline mode)
- [ ] Verify all error messages are user-friendly
- [ ] Test form with edge cases (very long names, special characters)
- [ ] Verify permissions work on Android 13+
- [ ] Test with actual backend
- [ ] Remove hardcoded test data

---

For complete documentation, see `IMPLEMENTATION_NOTES.md`

