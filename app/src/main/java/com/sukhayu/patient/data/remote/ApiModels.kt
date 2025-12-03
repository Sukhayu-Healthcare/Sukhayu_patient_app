package com.sukhayu.patient.data.remote

import com.google.gson.annotations.SerializedName

data class RegisterAshaRequest(
    val name: String,
    val password: String,
    val phone: String,
    val village: String,
    val district: String,
    val taluka: String,
    val profilePic: String? = null
)
