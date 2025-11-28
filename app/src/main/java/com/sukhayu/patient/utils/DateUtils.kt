package com.sukhayu.patient.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatDate(timestamp: Long?): String {
    return try {
        if (timestamp == null || timestamp == 0L) return "Unknown Date"

        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        sdf.format(date)
    } catch (e: Exception) {
        "Invalid Date"
    }
}
