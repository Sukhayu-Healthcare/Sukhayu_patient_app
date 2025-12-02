package com.sukhayu.patient.data.remote

import com.google.gson.annotations.SerializedName

data class PatientInfo(

    // New login response fields
    @SerializedName("patient_id")
    val patientId: String?,

    @SerializedName("user_id")
    val userId: String?,

    // Name from backend can be "name" or older "user_name"
    @SerializedName(value = "name", alternate = ["user_name"])
    val name: String?,

    val phone: String?,

    // Extra fields used by other APIs (keep them!)
    val gender: String? = null,
    val dob: String? = null,
    val village: String? = null,
    val district: String? = null,
    val taluka: String? = null,

    @SerializedName("weight_kg")
    val weightKg: Double? = null,

    @SerializedName("supreme_id")
    val supremeId: String? = null,

    val age: Int? = null,

    @SerializedName("registered_asha_id")
    val registeredAshaId: String? = null
)
