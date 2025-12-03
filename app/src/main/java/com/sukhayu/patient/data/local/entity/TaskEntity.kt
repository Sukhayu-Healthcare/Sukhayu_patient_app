package com.sukhayu.patient.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val date: Long, // Timestamp in milliseconds
    val category: String, // daily, weekly, monthly, yearly, custom
    val isDone: Boolean = false,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

