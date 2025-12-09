# Quick Reference: Appointment Booking API

## 🚀 Implementation Summary

The Android patient app now properly integrates with the backend appointment booking endpoint.

---

## 📝 API Endpoint Details

```
POST /appointments
Authorization: Bearer {token}
Content-Type: application/json
```

### Request Body
```json
{
  "doctor_id": 1,
  "appointment_date": "2025-12-20",
  "appointment_time": "14:30",
  "notes": null
}
```

### Success Response (201)
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
      "notes": null
    }
  ]
}
```

### Error Response (400/404/500)
```json
{
  "error": "Doctor is not available at this time"
}
```

---

## 📂 Modified Files

### 1. `BookAppointmentActivity.kt`
- **Location:** `app/src/main/java/com/sukhayu/patient/ui/patient/appointment/`
- **Key Changes:**
  - `DoctorAppointment` now has `id: Int` field
  - `saveAppointment()` makes API call instead of local save
  - Uses `TokenManager` for authentication
  - Handles API success/error/failure responses

### 2. `ApiService.kt`
- **Location:** `app/src/main/java/com/sukhayu/patient/data/remote/`
- **Key Changes:**
  - Added `BookAppointmentRequest` data class
  - Added `AppointmentResponse` data class
  - Added `bookAppointment()` API method

---

## 🔑 Key Implementation Details

### Doctor IDs (For Testing)
```
CHO (Community Health Officer):
  1 - Dr. Rajesh Kumar
  2 - Dr. Priya Singh
  3 - Dr. Amit Patel

MO (Medical Officer):
  4 - Dr. Vikram Sharma
  5 - Dr. Neha Gupta
  6 - Dr. Suresh Desai
```

### Date/Time Formats
- **Date:** `yyyy-MM-dd` (ISO format)
- **Time:** `HH:mm` (24-hour format, e.g., `14:30`)

### Field Mapping
| Android | Backend |
|---------|---------|
| `DoctorAppointment.id` | `doctor_id` |
| etAppointmentDate | `appointment_date` |
| etAppointmentTime | `appointment_time` |
| etNotes | `notes` |
| Token from Auth | Extracted to `patient_id` on backend |

---

## 🧪 Test Scenario

```kotlin
// User flow
1. Open BookAppointmentActivity
2. Click "CHO" button
3. Select "Dr. Rajesh Kumar" (doctor_id = 1)
4. Select date: 2025-12-20
5. Select time: 14:30
6. Add notes: "Routine checkup"
7. Click "Save Appointment"

// Expected API Call
POST /appointments
Header: Authorization: Bearer <token>
Body: {
  "doctor_id": 1,
  "appointment_date": "2025-12-20",
  "appointment_time": "14:30",
  "notes": "Routine checkup"
}

// Expected Response
{
  "message": "Appointment booked successfully",
  "appointment": [{
    "appointment_id": 123,
    "patient_id": 5,
    "doctor_id": 1,
    "appointment_date": "2025-12-20",
    "appointment_time": "14:30",
    "notes": "Routine checkup"
  }]
}
```

---

## ✨ Features

✅ **Secure Authentication**
- Uses Bearer token in Authorization header
- Token retrieved from TokenManager

✅ **Complete Validation**
- All required fields validated
- Doctor selection required
- Token must be available

✅ **Error Handling**
- HTTP error responses parsed and shown
- Network failures handled gracefully
- User-friendly error messages

✅ **User Feedback**
- Success message with backend response
- Error details displayed
- Form clears after booking
- Appointment list refreshes

✅ **API Compliance**
- Exact field names matching backend
- Correct HTTP method (POST)
- Proper content type (JSON)
- Correct authentication method

---

## 🔍 Validation Errors (Backend)

| Condition | Error Code | Message |
|-----------|-----------|---------|
| doctor_id missing | 400 | `doctor_id, appointment_date and appointment_time are required` |
| date missing | 400 | `doctor_id, appointment_date and appointment_time are required` |
| time missing | 400 | `doctor_id, appointment_date and appointment_time are required` |
| Doctor not found | 404 | `Doctor not found` |
| Patient conflict | 400 | `You already have an appointment at this time` |
| Doctor not available | 400 | `Doctor is not available at this time` |
| Server error | 500 | `Internal server error` |

---

## 📋 Checklist Before Release

- [ ] Test with valid doctor selection
- [ ] Test with missing time field
- [ ] Test with invalid date format
- [ ] Verify token is sent in header
- [ ] Test when doctor doesn't exist
- [ ] Test when slot is booked
- [ ] Test network failure scenario
- [ ] Verify form clears on success
- [ ] Verify appointment list updates
- [ ] Check error messages display correctly
- [ ] Verify UI layout unchanged

---

## 🔗 Related Files

- Backend API: `routes/patient.routes.ts` → `POST /appointments`
- Database: `db/appointments.sql`
- Frontend: `BookAppointmentActivity.kt`
- API Client: `ApiService.kt`
- Documentation: See included markdown files

---

## 📚 Additional Documentation

- `APPOINTMENT_BOOKING_API_INTEGRATION.md` - Full overview and features
- `APPOINTMENT_IMPLEMENTATION_DETAILS.md` - Technical details and code changes
- `IMPLEMENTATION_VERIFICATION_CHECKLIST.md` - Complete verification checklist

---

**Status:** ✅ Implementation Complete
**Ready for:** Testing with backend
**Last Updated:** December 9, 2025
