package com.sukhayu.patient.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sukhayu.patient.data.local.dao.AshaWorkerDao
import com.sukhayu.patient.data.local.dao.SupervisorProfileDao
import com.sukhayu.patient.data.local.entities.AshaWorkerEntity
import com.sukhayu.patient.data.local.entities.SupervisorProfileEntity

@Database(
    entities = [AshaWorkerEntity::class, SupervisorProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ashaWorkerDao(): AshaWorkerDao
    abstract fun supervisorProfileDao(): SupervisorProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sukhayu_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
