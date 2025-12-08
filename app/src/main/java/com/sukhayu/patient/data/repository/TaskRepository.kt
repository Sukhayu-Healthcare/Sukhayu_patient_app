package com.sukhayu.patient.data.repository

import com.sukhayu.patient.data.local.dao.TaskDao
import com.sukhayu.patient.data.local.entity.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(private val taskDao: TaskDao) {

    suspend fun addTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
    }

    suspend fun addMultipleTasks(tasks: List<TaskEntity>) = withContext(Dispatchers.IO) {
        taskDao.insertTasks(tasks)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.deleteTask(task)
    }

    suspend fun toggleTaskDone(task: TaskEntity) = withContext(Dispatchers.IO) {
        val updated = task.copy(isDone = !task.isDone, updatedAt = System.currentTimeMillis())
        taskDao.updateTask(updated)
    }

    fun getDailyTasksForDate(ashaId: String, dateInMillis: Long) =
        taskDao.getDailyTasksForDate(ashaId, dateInMillis)

    fun getWeeklyTasks(ashaId: String) = taskDao.getWeeklyTasks(ashaId)

    fun getMonthlyTasks(ashaId: String) = taskDao.getMonthlyTasks(ashaId)

    fun getYearlyTasks(ashaId: String) = taskDao.getYearlyTasks(ashaId)

    fun getAllTasksForDate(ashaId: String, dateInMillis: Long) =
        taskDao.getAllTasksForDate(ashaId, dateInMillis)

    fun getAllTasks(ashaId: String) = taskDao.getAllTasks(ashaId)

    suspend fun deleteAllTasksForAsha(ashaId: String) = withContext(Dispatchers.IO) {
        taskDao.deleteAllTasksForAsha(ashaId)
    }

    suspend fun countTasksByCategory(ashaId: String, category: String): Int =
        withContext(Dispatchers.IO) {
            taskDao.countTasksByCategory(ashaId, category)
        }
}

