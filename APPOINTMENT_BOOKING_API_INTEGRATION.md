# Appointment Booking API Integration

## Overview
Updated the Android patient app's `BookAppointmentActivity` to properly integrate with the backend appointment booking endpoint.

## Backend Endpoint
**Endpoint:** `POST /appointments`
**Authentication:** Bearer token required
**Base URL:** `https://sukhayu-backend.onrender.com/api/v1/`

### Request Body
```json
{
  "doctor_id": 1,
  "appointment_date": "2025-12-20",
  "appointment_time": "14:30",
  "notes": "Optional notes"
}
```

### Response
```json
{
  "message": "Appointment booked successfully",
  "appointment": [
    {
      "appointment_id": 123,
      "patient_id": 5,
      "doctor_id": 1,
      "appointment_date": "2025-12-20",
      "appointment_time": "14:30",
      "notes": "Optional notes"
    }
  ]
}
```

## Changes Made

### 1. **BookAppointmentActivity.kt**
#### Updates:
- Added `doctor_id` field to `DoctorAppointment` data class
- Updated sample doctors data with proper `id` values
- Modified `saveAppointment()` to make API calls instead of local database saves
- Uses `TokenManager.getToken()` to retrieve authentication token
- Properly validates all required fields before API call
- Handles API response success/failure with appropriate user feedback
- Imports API classes from `com.sukhayu.patient.data.remote`

#### Key Changes:
```kotlin
// Before: Saved locally
val appointment = AppointmentEntity(
    patient_id = patientId,
    doctor_name = doctorName,
    // ...
)

// After: API call to backend
val appointmentRequest = BookAppointmentRequest(
    doctor_id = selectedDoctor!!.id,  // Uses doctor ID from API
    appointment_date = appointmentDate,
    appointment_time = appointmentTime,
    notes = notes.ifEmpty { null }
)
apiService.bookAppointment("Bearer $token", appointmentRequest)
```

### 2. **ApiService.kt**
#### New Data Classes:
```kotlin
data class BookAppointmentRequest(
    val doctor_id: Int,
    val appointment_date: String,
    val appointment_time: String,
    val notes: String? = null
)

data class AppointmentData(
    val appointment_id: Int,
    val patient_id: Int,
    val doctor_id: Int,
    val appointment_date: String,
    val appointment_time: String,
    val notes: String?
)

data class AppointmentResponse(
    val message: String,
    val appointment: List<AppointmentData>
)
```

#### New API Method:
```kotlin
@POST("appointments")
fun bookAppointment(
    @Header("Authorization") token: String,
    @Body body: BookAppointmentRequest
): Call<AppointmentResponse>
```

## Features Implemented

✅ **Proper Authentication:**
- Uses `TokenManager.getToken()` for secure token management
- Sends bearer token in Authorization header

✅ **Validation:**
- Checks for required fields (doctor_id, appointment_date, appointment_time)
- Validates that a doctor is selected
- Ensures token is available

✅ **API Integration:**
- Makes HTTP POST request to backend endpoint
- Proper request body with all required fields
- Handles API responses and errors

✅ **User Feedback:**
- Success toast message with backend response
- Error messages showing HTTP error codes and details
- Failure messages for network errors

✅ **Field Mapping:**
- Uses proper field names matching backend (`doctor_id`, `appointment_date`, `appointment_time`)
- Correctly maps patient_id from authentication token
- Notes field is optional (can be null)

## Sample Doctor Data
The app includes sample doctors with proper IDs for testing:

### CHO (Community Health Officer):
- Dr. Rajesh Kumar (ID: 1)
- Dr. Priya Singh (ID: 2)
- Dr. Amit Patel (ID: 3)

### MO (Medical Officer):
- Dr. Vikram Sharma (ID: 4)
- Dr. Neha Gupta (ID: 5)
- Dr. Suresh Desai (ID: 6)

## Error Handling

The implementation handles:
1. **Missing authentication token** - Shows "Authentication token not found" message
2. **Missing required fields** - Shows "Please fill all required fields"
3. **Doctor not selected** - Shows "Please select a doctor"
4. **API errors** - Shows HTTP error code and details from response body
5. **Network failures** - Shows failure message with exception details

## Testing Checklist

- [ ] Test appointment booking with valid doctor selection
- [ ] Test with missing appointment time (should show validation error)
- [ ] Test with invalid date format
- [ ] Test error response when doctor doesn't exist (404)
- [ ] Test error response when doctor is already booked at same time (400)
- [ ] Test error response when patient already has appointment at same time (400)
- [ ] Verify token is properly sent in Authorization header
- [ ] Verify form clears after successful booking
- [ ] Verify saved appointments list updates after booking

## Notes

- The app now makes real API calls instead of storing data locally first
- Patient ID is automatically extracted from the authentication token on the backend
- All field validation follows the backend endpoint requirements
- The layout and visual elements remain unchanged as per requirements
