package com.sukhayu.patient.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sukhayu.patient.data.local.dao.AncVisitDao
import com.sukhayu.patient.data.local.dao.ConsultationDao
import com.sukhayu.patient.data.local.dao.PatientDao
import com.sukhayu.patient.data.local.dao.PregnancyDao
import com.sukhayu.patient.data.local.dao.PrescriptionDao
import com.sukhayu.patient.data.local.entity.AncVisitEntity
import com.sukhayu.patient.data.local.entity.ConsultationEntity
import com.sukhayu.patient.data.local.entity.PatientEntity
import com.sukhayu.patient.data.local.entity.PregnancyEntity
import com.sukhayu.patient.data.local.entity.PrescriptionItemEntity

@Database(
    entities = [
        ConsultationEntity::class,
        PrescriptionItemEntity::class,
        PatientEntity::class,
        PregnancyEntity::class,
        AncVisitEntity::class
    ],
    version = 5,                             // Incremented for ANC visits table
    exportSchema = false
)
abstract class AshaLocalDatabase : RoomDatabase() {

    abstract fun consultationDao(): ConsultationDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun patientDao(): PatientDao
    abstract fun pregnancyDao(): PregnancyDao
    abstract fun ancVisitDao(): AncVisitDao

    companion object {
        @Volatile private var INSTANCE: AshaLocalDatabase? = null

        fun getInstance(context: Context): AshaLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AshaLocalDatabase::class.java,
                    "asha_local_db"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
