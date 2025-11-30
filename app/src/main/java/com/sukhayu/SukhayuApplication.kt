package com.sukhayu

import android.app.Application
import android.util.Log
import com.sukhayu.patient.data.local.AshaLocalDatabase
import com.sukhayu.utils.MarathiTranslator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SukhayuApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Marathi translator singleton
            MarathiTranslator.getInstance(this)
            Log.d("SukhayuApplication", "MarathiTranslator initialized successfully")
        } catch (e: Exception) {
            Log.e("SukhayuApplication", "Error initializing MarathiTranslator", e)
        }

        // Verify database patient seeding
        verifyDatabaseSeeding()
    }

    /**
     * Verify that the database has been properly seeded with dummy patients.
     * This runs on app startup to confirm data is available for offline use.
     */
    private fun verifyDatabaseSeeding() {
        applicationScope.launch {
            try {
                val db = AshaLocalDatabase.getInstance(this@SukhayuApplication)
                val patientDao = db.patientDao()

                val count = patientDao.getPatientCount()
                Log.d("DB_PATIENT_COUNT", "✅ Patients in DB = $count")

                if (count == 0) {
                    Log.w("DB_PATIENT_COUNT", "⚠️ WARNING: Database is empty! Seeding may have failed.")
                } else {
                    // Test search functionality
                    val testSearch = patientDao.searchPatients("Sunita")
                    if (testSearch.isNotEmpty()) {
                        Log.d("DB_PATIENT_COUNT", "✅ Search test passed: Found '${testSearch[0].name}'")
                    } else {
                        Log.w("DB_PATIENT_COUNT", "⚠️ Search test failed: 'Sunita' not found")
                    }
                }
            } catch (e: Exception) {
                Log.e("DB_PATIENT_COUNT", "❌ Error verifying database", e)
            }
        }
    }
}