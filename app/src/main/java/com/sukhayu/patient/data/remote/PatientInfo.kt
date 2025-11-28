package com.sukhayu.patient.data.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PatientInfo(
    val id: String,
    val name: String,
    val phone: String,
    val supreme_id: String
) : Parcelable
