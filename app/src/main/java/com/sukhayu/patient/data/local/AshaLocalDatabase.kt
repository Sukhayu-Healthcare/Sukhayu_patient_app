package com.sukhayu.patient.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sukhayu.patient.data.local.dao.*
import com.sukhayu.patient.data.local.entity.*

@Database(
    entities = [
        ConsultationEntity::class,
        PrescriptionItemEntity::class,
        PatientEntity::class
    ],
    version = 3,                             // Increment version when adding new tables
    exportSchema = false
)
abstract class AshaLocalDatabase : RoomDatabase() {

    abstract fun consultationDao(): ConsultationDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun patientDao(): PatientDao

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
