package com.sukhayu.patient.data.local.entity

import java.util.Calendar

/**
 * Helper to generate predefined ASHA tasks based on category
 */
object PredefinedAshaTasksHelper {

    fun getWeeklyTasks(ashaId: String): List<TaskEntity> = listOf(
        TaskEntity(
            title = "Weekly Health Camp",
            date = System.currentTimeMillis(),
            category = "weekly",
            ashaId = ashaId
        ),
        TaskEntity(
            title = "Patient Follow-ups",
            date = System.currentTimeMillis(),
            category = "weekly",
            ashaId = ashaId
        ),
        TaskEntity(
            title = "Village Health Survey",
            date = System.currentTimeMillis(),
            category = "weekly",
            ashaId = ashaId
        ),
        TaskEntity(
            title = "Vaccination Clinic Assistance",
            date = System.currentTimeMillis(),
            category = "weekly",
            ashaId = ashaId
        )
    )

    fun getMonthlyTasks(ashaId: String): List<TaskEntity> = listOf(
        TaskEntity(
            title = "Monthly Health Report Submission",
            date = System.currentTimeMillis(),
            category = "monthly",
            ashaId = ashaId
        ),
        TaskEntity(
            title = "Maternal & Child Health Review",
            date = System.currentTimeMillis(),
            category = "monthly",
            ashaId = ashaId
        ),
        TaskEntity(
            title = "Nutrition Assessment",
            date = System.currentTimeMillis(),
            category = "monthly",
            ashaId = ashaId
        ),
        TaskEntity(
            title = "Community Health Education",
            date = System.currentTimeMillis(),
            category = "monthly",
            ashaId = ashaId
        )
    )

    fun getYearlyTasks(ashaId: String): List<TaskEntity> {
        val tasks = mutableListOf<TaskEntity>()

        // Fixed yearly events
        val now = Calendar.getInstance()

        // Leprosy Awareness Day - January 30
        val jan30 = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        tasks.add(
            TaskEntity(
                title = "Leprosy Awareness Campaign (30 Jan)",
                date = jan30.timeInMillis,
                category = "yearly",
                ashaId = ashaId
            )
        )

        // Leprosy Day - October 2
        val oct2 = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.OCTOBER)
            set(Calendar.DAY_OF_MONTH, 2)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        tasks.add(
            TaskEntity(
                title = "World Leprosy Eradication Day (2 Oct)",
                date = oct2.timeInMillis,
                category = "yearly",
                ashaId = ashaId
            )
        )

        // Other important yearly events
        tasks.addAll(
            listOf(
                TaskEntity(
                    title = "Annual Health Program Review",
                    date = System.currentTimeMillis(),
                    category = "yearly",
                    ashaId = ashaId
                ),
                TaskEntity(
                    title = "ASHA Training & Development",
                    date = System.currentTimeMillis(),
                    category = "yearly",
                    ashaId = ashaId
                ),
                TaskEntity(
                    title = "Community Health Census",
                    date = System.currentTimeMillis(),
                    category = "yearly",
                    ashaId = ashaId
                )
            )
        )

        return tasks
    }

    fun getDailyTasks(ashaId: String, dateInMillis: Long): List<TaskEntity> {
        // Daily tasks are user-created, this is empty by default
        // Users add daily tasks via the UI
        return emptyList()
    }

    fun getAllPredefinedTasks(ashaId: String): List<TaskEntity> {
        return getWeeklyTasks(ashaId) + getMonthlyTasks(ashaId) + getYearlyTasks(ashaId)
    }
}

