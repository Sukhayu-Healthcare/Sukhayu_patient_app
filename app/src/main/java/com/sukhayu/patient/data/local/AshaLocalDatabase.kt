package com.sukhayu.patient.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sukhayu.patient.DummyData
import com.sukhayu.patient.data.local.dao.AncVisitDao
import com.sukhayu.patient.data.local.dao.ConsultationDao
import com.sukhayu.patient.data.local.dao.GeneralSurveyDao
import com.sukhayu.patient.data.local.dao.PatientDao
import com.sukhayu.patient.data.local.dao.PregnancyDao
import com.sukhayu.patient.data.local.dao.PrescriptionDao
import com.sukhayu.patient.data.local.dao.TaskDao
import com.sukhayu.patient.data.local.dao.TbFollowUpDao
import com.sukhayu.patient.data.local.dao.TbScreeningDao
import com.sukhayu.patient.data.local.entity.AncVisitEntity
import com.sukhayu.patient.data.local.entity.ConsultationEntity
import com.sukhayu.patient.data.local.entity.GeneralSurveyEntity
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.local.entity.PregnancyEntity
import com.sukhayu.patient.data.local.entity.PrescriptionItemEntity
import com.sukhayu.patient.data.local.entity.TaskEntity
import com.sukhayu.patient.data.local.entity.TbFollowUpEntity
import com.sukhayu.patient.data.local.entity.TbScreeningEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConsultationEntity::class,
        PrescriptionItemEntity::class,
        PatientEntity::class,
        PregnancyEntity::class,
        AncVisitEntity::class,
        TbScreeningEntity::class,
        TbFollowUpEntity::class,
        GeneralSurveyEntity::class,
        TaskEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AshaLocalDatabase : RoomDatabase() {

    abstract fun consultationDao(): ConsultationDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun patientDao(): PatientDao
    abstract fun pregnancyDao(): PregnancyDao
    abstract fun ancVisitDao(): AncVisitDao
    abstract fun tbScreeningDao(): TbScreeningDao
    abstract fun tbFollowUpDao(): TbFollowUpDao
    abstract fun generalSurveyDao(): GeneralSurveyDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var INSTANCE: AshaLocalDatabase? = null

        // Coroutine scope for database operations during initialization
        private val databaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /**
         * Database callback to seed dummy patient data when database is created for the first time.
         * This ensures offline-first architecture with test data available immediately.
         */
        private class DatabaseCallback : Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("DB_SEED", "Database onCreate triggered - starting patient seeding")

                // Get database instance and seed data in background coroutine
                INSTANCE?.let { database ->
                    databaseScope.launch {
                        try {
                            val patientDao = database.patientDao()
                            val dummyPatients = DummyData.getDummyPatients()

                            Log.d("DB_SEED", "Inserting ${dummyPatients.size} dummy patients...")

                            // Insert all dummy patients
                            patientDao.insertPatients(dummyPatients)

                            val count = patientDao.getPatientCount()
                            Log.d("DB_SEED", "✅ Successfully seeded $count dummy patients to database")

                            // Log sample patient names for verification
                            val samplePatients = patientDao.searchPatients("Sunita")
                            if (samplePatients.isNotEmpty()) {
                                Log.d("DB_SEED", "✅ Test search for 'Sunita' found: ${samplePatients[0].name}")
                            }
                        } catch (e: Exception) {
                            Log.e("DB_SEED", "❌ Error seeding dummy patients", e)
                        }
                    }
                } ?: Log.e("DB_SEED", "❌ INSTANCE is null - cannot seed data")
            }
        }

        fun getInstance(context: Context): AshaLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AshaLocalDatabase::class.java,
                    "asha_local_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())  // Add seeding callback
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
