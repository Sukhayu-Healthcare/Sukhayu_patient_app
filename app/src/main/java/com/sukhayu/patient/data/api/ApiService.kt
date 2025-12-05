package com.sukhayu.patient.data.api

import com.sukhayu.patient.data.model.ConsultationResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("patient/consultations")
    suspend fun getPatientConsultations(
        @Header("Authorization") token: String
    ): ConsultationResponse

    // ...existing endpoints...
}