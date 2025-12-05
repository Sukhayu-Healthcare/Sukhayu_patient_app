package com.sukhayu.patient.data.repository

/**
 * Generic result wrapper for pregnancy survey operations (ANC, follow-up, etc.)
 * Each network or sync request can update UI using these states.
 */
sealed class ResultState<out T> {

    // No operation happening
    object Idle : ResultState<Nothing>()

    // Network or sync in progress
    object Loading : ResultState<Nothing>()

    // Success state with data
    data class Success<T>(val data: T) : ResultState<T>()

    // Error state with message
    data class Error(val message: String) : ResultState<Nothing>()
}
