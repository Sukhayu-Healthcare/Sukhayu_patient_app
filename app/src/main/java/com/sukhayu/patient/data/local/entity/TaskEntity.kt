package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a task/schedule item for ASHA workers.
 * Supports daily, weekly, monthly, yearly, and custom categories.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    // Date for daily tasks (timestamp in milliseconds)
    // For recurring tasks, this represents the base/start date
    val date: Long,

    // Category: "daily", "weekly", "monthly", "yearly", "custom"
    val category: String,

    val isDone: Boolean = false,

    // For recurring tasks, store which dates it applies to
    val ashaId: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

