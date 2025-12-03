package com.sukhayu.patient.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sukhayu.patient.data.local.entity.TaskEntity

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getTasksForDate(date: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getTasksForDateRange(startDate: Long, endDate: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY date ASC")
    suspend fun getTasksByCategory(category: String): List<TaskEntity>

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("SELECT * FROM tasks ORDER BY date DESC")
    suspend fun getAllTasks(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE date = :date")
    suspend fun getTaskCountForDate(date: Long): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE date = :date AND isDone = 1")
    suspend fun getCompletedTaskCountForDate(date: Long): Int
}

