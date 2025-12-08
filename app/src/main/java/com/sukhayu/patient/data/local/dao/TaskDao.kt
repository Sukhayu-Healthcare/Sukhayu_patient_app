package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sukhayu.patient.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): TaskEntity?

    // Get daily tasks for a specific date
    @Query("""
        SELECT * FROM tasks 
        WHERE ashaId = :ashaId 
        AND category = 'daily'
        AND date = :dateInMillis
        ORDER BY title ASC
    """)
    fun getDailyTasksForDate(ashaId: String, dateInMillis: Long): Flow<List<TaskEntity>>

    // Get weekly tasks (show for all days, user can mark done per day)
    @Query("""
        SELECT * FROM tasks 
        WHERE ashaId = :ashaId 
        AND category = 'weekly'
        ORDER BY title ASC
    """)
    fun getWeeklyTasks(ashaId: String): Flow<List<TaskEntity>>

    // Get monthly tasks
    @Query("""
        SELECT * FROM tasks 
        WHERE ashaId = :ashaId 
        AND category = 'monthly'
        ORDER BY title ASC
    """)
    fun getMonthlyTasks(ashaId: String): Flow<List<TaskEntity>>

    // Get yearly tasks
    @Query("""
        SELECT * FROM tasks 
        WHERE ashaId = :ashaId 
        AND category = 'yearly'
        ORDER BY title ASC
    """)
    fun getYearlyTasks(ashaId: String): Flow<List<TaskEntity>>

    // Get all tasks for a date (daily + recurring that apply to that date)
    @Query("""
        SELECT * FROM tasks 
        WHERE ashaId = :ashaId 
        AND (
            (category = 'daily' AND date = :dateInMillis) OR
            (category = 'weekly') OR
            (category = 'monthly') OR
            (category = 'yearly')
        )
        ORDER BY category DESC, title ASC
    """)
    fun getAllTasksForDate(ashaId: String, dateInMillis: Long): Flow<List<TaskEntity>>

    // Get all tasks for ASHA
    @Query("SELECT * FROM tasks WHERE ashaId = :ashaId ORDER BY date DESC, title ASC")
    fun getAllTasks(ashaId: String): Flow<List<TaskEntity>>

    // Delete all tasks for ASHA
    @Query("DELETE FROM tasks WHERE ashaId = :ashaId")
    suspend fun deleteAllTasksForAsha(ashaId: String)

    // Count tasks by category
    @Query("SELECT COUNT(*) FROM tasks WHERE ashaId = :ashaId AND category = :category")
    suspend fun countTasksByCategory(ashaId: String, category: String): Int
}

