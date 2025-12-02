package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sukhayu.patient.data.local.entity.PatientEntity

@Dao
interface PatientDao {

    /**
     * Search patients by name or phone with flexible LIKE pattern matching.
     * The query parameter should be passed as-is (e.g., "Sunita" or "9876543210")
     * and the query will automatically wrap it with wildcards.
     */
    @Query("SELECT * FROM patients WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' LIMIT 10")
    suspend fun searchPatients(query: String): List<PatientEntity>

    @Query("SELECT * FROM patients WHERE id = :patientId LIMIT 1")
    suspend fun getPatientById(patientId: String): PatientEntity?

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<PatientEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(patient: PatientEntity)

    @Query("SELECT * FROM patients ORDER BY name ASC")
    suspend fun getAllPatients(): List<PatientEntity>

    @Query("DELETE FROM patients")
    suspend fun deleteAll()
}
