package com.sukhayu.patient.data.local

import android.content.Context
import com.sukhayu.patient.data.repository.PatientRepository
import com.sukhayu.patient.data.remote.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Utility class to initialize the local database with dummy data on first run.
 *
 * This ensures that the app has offline patient data available for:
 * - Pregnancy/ANC survey flows
 * - TB screening flows
 * - TB treatment follow-up flows
 *
 * Call DatabaseInitializer.initialize(context) early in the app lifecycle
 * (e.g., in MainActivity.onCreate or a custom Application class).
 */
object DatabaseInitializer {

    private var initialized = false
    private val initializationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Initialize the database with dummy patients if it's empty.
     * This is a one-time operation that runs asynchronously.
     *
     * Safe to call multiple times - will only initialize once.
     */
    fun initialize(context: Context) {
        if (initialized) return

        initialized = true

        initializationScope.launch {
            try {
                val db = AshaLocalDatabase.getInstance(context)

                // Create a dummy ApiService for the repository
                // (It won't be used during initialization, but repository requires it)
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://dummy.api/") // Placeholder URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiService = retrofit.create(ApiService::class.java)

                val patientRepository = PatientRepository(db, apiService)
                patientRepository.initializeDummyDataIfNeeded()

                // Log success (in production, you might want to use proper logging)
                println("DatabaseInitializer: Dummy data initialized successfully")
            } catch (e: Exception) {
                // Log error but don't crash the app
                e.printStackTrace()
                println("DatabaseInitializer: Failed to initialize dummy data: ${e.message}")
            }
        }
    }
}

