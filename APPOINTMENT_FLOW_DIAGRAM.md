# Appointment Booking Flow Diagram

## 📊 User Flow

```
┌─────────────────────────────────────────────────────────────────┐
│         BookAppointmentActivity - User Interaction              │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │ Select Doctor Type    │
                    │ (CHO or MO)           │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │ Choose Doctor         │
                    │ (from list)           │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │ Enter Date & Time     │
                    │ (Date picker/picker)  │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │ Add Notes (Optional)  │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │ Click Save Button     │
                    └───────────┬───────────┘
                                │
                                ▼
          ┌─────────────────────────────────────────┐
          │    saveAppointment() Method             │
          └─────────────────┬───────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        │                                       │
        ▼                                       ▼
   ┌─────────────────┐                ┌──────────────────┐
   │ Validate Fields │                │ Get Auth Token   │
   │ - All required  │                │ from TokenMgr    │
   │ - Doctor select │                └────────┬─────────┘
   └────────┬────────┘                         │
            │                                  │
            └──────────────┬───────────────────┘
                           │
                           ▼
            ┌──────────────────────────────┐
            │ Create API Request           │
            │ - doctor_id                  │
            │ - appointment_date           │
            │ - appointment_time           │
            │ - notes (optional)           │
            └────────────┬─────────────────┘
                         │
                         ▼
            ┌──────────────────────────────┐
            │ Make HTTP POST Request       │
            │ /appointments                │
            │ Header: Authorization Token  │
            │ Body: BookAppointmentRequest │
            └────────────┬─────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
    ┌────────┐     ┌───────────┐   ┌─────────┐
    │ Success│     │  Error    │   │ Failure │
    │ (200)  │     │ (400/404) │   │(Network)│
    └────┬───┘     └─────┬─────┘   └────┬────┘
         │               │              │
         ▼               ▼              ▼
    ┌─────────────────────────────────────────┐
    │ Display Toast Message to User           │
    │ Success: Backend response message       │
    │ Error: HTTP error + details             │
    │ Failure: Network error details          │
    └────────────────┬────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
         ▼                       ▼
    ┌────────────┐         ┌──────────┐
    │ Clear Form │         │ Return   │
    │ Reload List│         │ (Error)  │
    └────────────┘         └──────────┘
         │
         ▼
    ┌──────────────────┐
    │ Back to Normal   │
    │ State            │
    └──────────────────┘
```

---

## 🔄 API Communication Flow

```
Android App                          Backend API
───────────────────────────────────────────────────────────────

┌──────────────────────┐
│ BookAppointmentActivity
│ - Selected doctor    │
│ - Date & time       │
│ - Notes             │
└──────────┬───────────┘
           │
           │ 1. Validation
           │    ✓ All fields
           │    ✓ Doctor selected
           │    ✓ Token available
           │
           ▼
┌──────────────────────────────┐
│ Create BookAppointmentRequest │
│ {                             │
│   "doctor_id": 1,             │
│   "appointment_date": "...",   │
│   "appointment_time": "...",   │
│   "notes": "optional"         │
│ }                             │
└──────────┬────────────────────┘
           │
           │ 2. HTTP POST Request
           │    POST /appointments
           │    Authorization: Bearer {token}
           │    Content-Type: application/json
           │
           ├─────────────────────────────────────────────────┐
           │                                                 │
           │ Request Body:                                   │
           │ {                                               │
           │   "doctor_id": 1,                               │
           │   "appointment_date": "2025-12-20",             │
           │   "appointment_time": "14:30",                  │
           │   "notes": null                                 │
           │ }                                               │
           │                                                 │
           ▼                                                 │
       ┌──────────────────────────────────────────┐         │
       │ Backend Validation                       │         │
       │ 1. Check if doctor_id exists            │         │
       │ 2. Check patient conflict               │         │
       │ 3. Check doctor availability            │         │
       └──────────┬───────────────────────────────┘         │
                  │                                          │
                  ├─ All valid?                              │
                  │    YES ↓                                 │
                  │    ┌──────────────────────────┐          │
                  │    │ Insert into appointments │          │
                  │    │ Database Entry Created   │          │
                  │    └──────────┬───────────────┘          │
                  │               │                          │
                  │               ▼                          │
                  │    ┌──────────────────────────────────┐  │
                  │    │ Return 201 Success              │  │
                  │    │ {                                │  │
                  │    │   "message": "Appointment....",  │  │
                  │    │   "appointment": [{...}]         │  │
                  │    │ }                                │  │
                  │    └──────────┬─────────────────────┘   │
                  │               │                         │
                  │               └────────────┐            │
                  │                            │            │
                  │    NO (Invalid) ↓          │            │
                  │    ┌──────────────────────┐│            │
                  │    │ Return Error (400/404)││            │
                  │    │ {                     ││            │
                  │    │   "error": "..."      ││            │
                  │    │ }                     ││            │
                  │    └──────────┬────────────┘│            │
                  │               │             │            │
                  │               └─────┬───────┘            │
                  │                     │                    │
                  └─────────────────────┼────────────────────┘
                                        │
                  3. Handle Response    │
                       ▼
┌──────────────────────────────────────────────┐
│ Callback: onResponse() or onFailure()        │
│                                              │
│ Success:                                     │
│ - Parse AppointmentResponse                  │
│ - Show success message from backend          │
│ - Clear form                                 │
│ - Reload appointments list                   │
│                                              │
│ Error (400/404):                             │
│ - Parse error body                           │
│ - Show error message + details               │
│ - Keep form data                             │
│                                              │
│ Failure (Network):                           │
│ - Show network error message                 │
│ - Keep form data                             │
│ - Allow retry                                │
└──────────────────────────────────────────────┘
```

---

## 🗂️ Data Structure Flow

```
UI Layer (BookAppointmentActivity)
│
├─ DoctorAppointment
│  ├─ id: Int              ──────┐
│  ├─ name: String              │
│  ├─ phone: String             │
│  ├─ specialization: String    │
│  └─ availableDays: String     │
│                               │
├─ Form Data                    │
│  ├─ appointmentDate ──────────┤
│  ├─ appointmentTime ──────────┤
│  ├─ notes ────────────────────┤
│  └─ token (from TokenManager) │
│                               │
└─ API Layer                    │
   │                            │
   └─ BookAppointmentRequest ◄──┘
      ├─ doctor_id: Int
      ├─ appointment_date: String (yyyy-MM-dd)
      ├─ appointment_time: String (HH:mm)
      └─ notes: String?

           │
           │ HTTP POST /appointments
           │ Authorization: Bearer {token}
           │
           ▼

   Backend Response
   │
   └─ AppointmentResponse
      ├─ message: String
      └─ appointment: List<AppointmentData>
         └─ AppointmentData
            ├─ appointment_id: Int
            ├─ patient_id: Int (from token)
            ├─ doctor_id: Int
            ├─ appointment_date: String
            ├─ appointment_time: String
            └─ notes: String?
```

---

## 🔌 Component Integration

```
┌────────────────────────────────────────────────────────┐
│           BookAppointmentActivity                      │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │ UI Elements                                      │  │
│  │ - Doctor Type Buttons                            │  │
│  │ - Doctor Selector                                │  │
│  │ - Date/Time Pickers                              │  │
│  │ - Notes Field                                    │  │
│  │ - Save Button                                    │  │
│  │ - Appointment List (RecyclerView)                │  │
│  └──────────────────────────────────────────────────┘  │
│                      │                                  │
│                      ▼                                  │
│  ┌──────────────────────────────────────────────────┐  │
│  │ saveAppointment()                                │  │
│  │ 1. Validate inputs                               │  │
│  │ 2. Get token from TokenManager                   │  │
│  │ 3. Create API request                            │  │
│  │ 4. Call ApiService.bookAppointment()             │  │
│  │ 5. Handle response/error                         │  │
│  └────────────┬─────────────────────────────────────┘  │
│               │                                         │
│               └──────────────────────┐                  │
└────────────────────────────────────────┼──────────────┘
                                         │
                          ┌──────────────▼──────────────┐
                          │       ApiClient            │
                          │                            │
                          │ - BASE_URL: String         │
                          │ - retrofit: Retrofit       │
                          │ - httpClient: OkHttpClient │
                          └──────────────┬──────────────┘
                                         │
                          ┌──────────────▼──────────────┐
                          │      ApiService            │
                          │                            │
                          │ @POST("appointments")      │
                          │ fun bookAppointment(...)   │
                          └──────────────┬──────────────┘
                                         │
                          ┌──────────────▼──────────────┐
                          │    TokenManager            │
                          │                            │
                          │ fun getToken(context)      │
                          │ Returns: Bearer Token      │
                          └────────────────────────────┘
```

---

## 📍 Error Flow

```
User Action
│
▼
saveAppointment()
│
├─ Validation Check
│  │
│  ├─ Field empty?
│  │  └─ Toast: "Please fill all required fields"
│  │     Return (Stop)
│  │
│  ├─ Doctor not selected?
│  │  └─ Toast: "Please select a doctor"
│  │     Return (Stop)
│  │
│  └─ Token not available?
│     └─ Toast: "Authentication token not found"
│        Return (Stop)
│
├─ If validation passes → Create API request
│
└─ API Call
   │
   ├─ SUCCESS (201)
   │  ├─ Toast: Backend message
   │  ├─ clearForm()
   │  └─ loadSavedAppointments()
   │
   ├─ ERROR (400/404/500)
   │  ├─ Parse error body
   │  └─ Toast: "Error: {code} - {details}"
   │
   └─ FAILURE (Network)
      └─ Toast: "Failed to book appointment: {error}"
```

---

**Visual Flow Generated:** December 9, 2025
