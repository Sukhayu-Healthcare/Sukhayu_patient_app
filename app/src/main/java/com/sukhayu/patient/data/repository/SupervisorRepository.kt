package com.sukhayu.patient.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.sukhayu.patient.data.local.AppDatabase
import com.sukhayu.patient.data.local.entities.AshaWorkerEntity
import com.sukhayu.patient.data.local.entities.SupervisorProfileEntity
import com.sukhayu.patient.data.remote.*
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class SupervisorRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val ashaWorkerDao = database.ashaWorkerDao()
    private val supervisorProfileDao = database.supervisorProfileDao()

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getAshaWorkers(): Result<List<AshaWorker>> = withContext(Dispatchers.IO) {
        try {
            if (isNetworkAvailable()) {
                // Fetch from backend
                val token = TokenManager.getToken()
                val response = ApiClient.retrofit.getAshaList("Bearer $token").execute()
                
                if (response.isSuccessful && response.body() != null) {
                    val ashaWorkers = response.body()!!.ashas
                    // Cache in local database
                    val entities = ashaWorkers.map { AshaWorkerEntity.fromAshaWorker(it) }
                    ashaWorkerDao.deleteAll()
                    ashaWorkerDao.insertAll(entities)
                    Log.d("Repository", "Fetched ${ashaWorkers.size} ASHA workers from backend")
                    Result.success(ashaWorkers)
                } else {
                    // If backend fails, try local cache
                    val cachedData = ashaWorkerDao.getAllAshaWorkers().map { it.toAshaWorker() }
                    if (cachedData.isNotEmpty()) {
                        Log.d("Repository", "Backend failed, using ${cachedData.size} cached ASHA workers")
                        Result.success(cachedData)
                    } else {
                        Result.failure(Exception("Failed to fetch: ${response.message()}"))
                    }
                }
            } else {
                // No network, use cached data
                val cachedData = ashaWorkerDao.getAllAshaWorkers().map { it.toAshaWorker() }
                Log.d("Repository", "No network, loaded ${cachedData.size} cached ASHA workers")
                if (cachedData.isNotEmpty()) {
                    Result.success(cachedData)
                } else {
                    Result.failure(Exception("No network connection and no cached data available"))
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching ASHA workers", e)
            // Try cache on exception
            val cachedData = ashaWorkerDao.getAllAshaWorkers().map { it.toAshaWorker() }
            if (cachedData.isNotEmpty()) {
                Result.success(cachedData)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getSupervisorProfile(): Result<SupervisorProfile> = withContext(Dispatchers.IO) {
        try {
            if (isNetworkAvailable()) {
                val token = TokenManager.getToken()
                val response = ApiClient.retrofit.getSupervisorProfile("Bearer $token").execute()
                
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!
                    // Cache in local database
                    supervisorProfileDao.insert(SupervisorProfileEntity.fromSupervisorProfile(profile))
                    Log.d("Repository", "Fetched profile from backend")
                    Result.success(profile)
                } else {
                    val cachedProfile = supervisorProfileDao.getProfile()?.toSupervisorProfile()
                    if (cachedProfile != null) {
                        Log.d("Repository", "Backend failed, using cached profile")
                        Result.success(cachedProfile)
                    } else {
                        Result.failure(Exception("Failed to fetch: ${response.message()}"))
                    }
                }
            } else {
                val cachedProfile = supervisorProfileDao.getProfile()?.toSupervisorProfile()
                Log.d("Repository", "No network, using cached profile")
                if (cachedProfile != null) {
                    Result.success(cachedProfile)
                } else {
                    Result.failure(Exception("No network connection and no cached profile"))
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error fetching profile", e)
            val cachedProfile = supervisorProfileDao.getProfile()?.toSupervisorProfile()
            if (cachedProfile != null) {
                Result.success(cachedProfile)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun updateSupervisorProfile(updateRequest: SelfUpdateRequest): Result<SupervisorProfile> = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                return@withContext Result.failure(Exception("No network connection. Cannot update profile."))
            }

            val token = TokenManager.getToken()
            val response = ApiClient.retrofit.updateSupervisorProfile("Bearer $token", updateRequest).execute()
            
            if (response.isSuccessful && response.body() != null) {
                val updatedProfile = response.body()!!.profile
                supervisorProfileDao.insert(SupervisorProfileEntity.fromSupervisorProfile(updatedProfile))
                Result.success(updatedProfile)
            } else {
                Result.failure(Exception("Failed to update: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
