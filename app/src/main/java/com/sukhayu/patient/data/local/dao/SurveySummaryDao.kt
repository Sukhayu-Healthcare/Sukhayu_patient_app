package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sukhayu.patient.data.local.entity.SurveySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveySummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(summary: SurveySummaryEntity)

    @Query("""
        SELECT * FROM survey_summary
        WHERE ashaId = :ashaId
        ORDER BY surveyDate DESC
    """)
    fun getAllForAsha(ashaId: String): Flow<List<SurveySummaryEntity>>

    @Query("""
        SELECT COUNT(*) FROM survey_summary
        WHERE ashaId = :ashaId AND isSynced = 1
    """)
    fun countSyncedForAsha(ashaId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM survey_summary
        WHERE ashaId = :ashaId AND isSynced = 0
    """)
    fun countPendingForAsha(ashaId: String): Flow<Int>

    /**
     * Mark a survey summary as synced by its surveyLocalId and ashaId
     */
    @Query("""
        UPDATE survey_summary
        SET isSynced = 1
        WHERE surveyLocalId = :surveyLocalId AND ashaId = :ashaId
    """)
    suspend fun markSummaryAsSynced(surveyLocalId: String, ashaId: String)
}
