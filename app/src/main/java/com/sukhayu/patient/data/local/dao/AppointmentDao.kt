package com.sukhayu.patient.data.local.dao

import androidx.room.*
import com.sukhayu.patient.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    @Query("SELECT * FROM appointments WHERE patient_id = :patientId ORDER BY appointment_date DESC")
    fun getPatientAppointments(patientId: Int): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE appointment_id = :appointmentId")
    suspend fun getAppointmentById(appointmentId: Int): AppointmentEntity?

    @Query("SELECT * FROM appointments WHERE synced = 0 ORDER BY created_at ASC")
    fun getUnsyncedAppointments(): Flow<List<AppointmentEntity>>

    @Query("UPDATE appointments SET synced = 1, sync_status = 'synced' WHERE appointment_id = :appointmentId")
    suspend fun markAsSynced(appointmentId: Int)

    @Query("UPDATE appointments SET sync_status = 'failed' WHERE appointment_id = :appointmentId")
    suspend fun markAsSyncFailed(appointmentId: Int)

    @Query("DELETE FROM appointments WHERE patient_id = :patientId")
    suspend fun deletePatientAppointments(patientId: Int)
}
