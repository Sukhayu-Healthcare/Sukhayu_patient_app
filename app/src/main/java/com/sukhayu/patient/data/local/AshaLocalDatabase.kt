package com.sukhayu.patient.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sukhayu.patient.DummyData
import com.sukhayu.patient.data.local.dao.*
import com.sukhayu.patient.data.local.entity.*
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
        SurveySummaryEntity::class,
        TaskEntity::class                  // ✅ Added for Task/Schedule
    ],
    version = 11,                          // 🚨 INCREMENTED for TaskEntity
    exportSchema = false
)
abstract class AshaLocalDatabase : RoomDatabase() {

    // --- DAO declarations ---
    abstract fun consultationDao(): ConsultationDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun patientDao(): PatientDao
    abstract fun pregnancyDao(): PregnancyDao
    abstract fun ancVisitDao(): AncVisitDao
    abstract fun tbScreeningDao(): TbScreeningDao
    abstract fun tbFollowUpDao(): TbFollowUpDao
    abstract fun taskDao(): TaskDao
    abstract fun generalSurveyDao(): GeneralSurveyDao

    // ✅ Newly added
    abstract fun surveySummaryDao(): SurveySummaryDao


    companion object {
        @Volatile private var INSTANCE: AshaLocalDatabase? = null

        private val databaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /**
         * Callback only seeds PATIENTS table (unchanged).
         */
        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d("DB_SEED", "Database created — seeding dummy patients")

                INSTANCE?.let { database ->
                    databaseScope.launch {
                        try {
                            val patientDao = database.patientDao()
                            val dummyPatients = DummyData.getDummyPatients()

                            Log.d("DB_SEED", "Inserting ${dummyPatients.size} dummy patients...")
                            patientDao.insertPatients(dummyPatients)

                            val count = patientDao.getPatientCount()
                            Log.d("DB_SEED", "Seed complete — total patients: $count")

                        } catch (e: Exception) {
                            Log.e("DB_SEED", "Error during patient seeding", e)
                        }
                    }
                } ?: Log.e("DB_SEED", "INSTANCE NULL — cannot seed database")
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
                    .addCallback(DatabaseCallback())
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
