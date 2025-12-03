package com.sukhayu.patient.data.remote


import com.sukhayu.patient.data.local.entity.PatientEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * DTO returned by GET /patient/all
 */
data class AllPatientsResponse(
    val message: String?,
    val patients: List<PatientFromServer>
)

data class PatientFromServer(
    val patient_id: Int?,
    val gender: String?,
    val dob: String?,
    val phone: Long?,
    val profile_pic: String?,
    val village: String?,
    val taluka: String?,
    val district: String?,
    val supreme_id: Int?,
    val name: String?
)

fun PatientFromServer.toEntity(): PatientEntity {
    return PatientEntity(
        id = patient_id?.toString() ?: name ?: phone?.toString() ?: System.currentTimeMillis().toString(),
        name = name ?: "Unknown",
        phone = phone?.toString(),
        gender = gender,
        weightKg = null,
        supremeId = supreme_id?.toString(),
        age = dob?.let { calculateAgeFromDob(it) }
    )
}

private fun calculateAgeFromDob(dob: String): Int? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDate = formatter.parse(dob) ?: return null
        val now = Calendar.getInstance()
        val dobCal = Calendar.getInstance().apply { time = birthDate }
        var age = now.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
            age -= 1
        }
        age
    } catch (e: Exception) {
        null
    }
}


