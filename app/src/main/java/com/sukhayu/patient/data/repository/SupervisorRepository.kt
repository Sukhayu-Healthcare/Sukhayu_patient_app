package com.sukhayu.patient.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.sukhayu.patient.data.local.AppDatabase
import com.sukhayu.patient.data.local.entities.AshaWorkerEntity
import com.sukhayu.patient.data.local.entities.SupervisorProfileEntity
import com.sukhayu.patient.data.remote.AshaListResponse
import com.sukhayu.patient.data.remote.AshaWorker
import com.sukhayu.patient.data.remote.ApiClient
import com.sukhayu.patient.data.remote.SelfUpdateRequest
import com.sukhayu.patient.data.remote.SupervisorProfile
import com.sukhayu.patient.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupervisorRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val ashaWorkerDao = database.ashaWorkerDao()
    private val supervisorProfileDao = database.supervisorProfileDao()

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * NEW: Paginated ASHA fetch.
     *
     * Uses backend pagination and returns the full AshaListResponse:
     *  - total, page, limit, totalPages, ashas
     *
     * Online:
     *   - Calls backend with page + limit
     *   - When page == 1, caches that page in Room (replaces old cache)
     *
     * Offline:
     *   - Uses cached ASHAs from Room
     *   - Manually paginates the cached list
     */
    suspend fun getAshaWorkers(page: Int, limit: Int): Result<AshaListResponse> =
        withContext(Dispatchers.IO) {
            try {
                if (isNetworkAvailable()) {
                    val token = TokenManager.getToken()
                    if (token.isNullOrEmpty()) {
                        return@withContext Result.failure(
                            IllegalStateException("Auth token not found")
                        )
                    }

                    // ApiService.getAshaList is now `suspend` and returns AshaListResponse
                    val response = ApiClient.retrofit.getAshaList(
                        token = "Bearer $token",
                        page = page,
                        limit = limit
                    )

                    // Cache only first page (reasonable compromise with pagination)
                    if (page == 1) {
                        val entities = response.ashas.map { AshaWorkerEntity.fromAshaWorker(it) }
                        ashaWorkerDao.deleteAll()
                        ashaWorkerDao.insertAll(entities)
                        Log.d(
                            "SupervisorRepository",
                            "Fetched & cached ${response.ashas.size} ASHA workers from backend (page 1)"
                        )
                    } else {
                        Log.d(
                            "SupervisorRepository",
                            "Fetched ${response.ashas.size} ASHA workers from backend (page $page/${response.totalPages})"
                        )
                    }

                    Result.success(response)
                } else {
                    // Offline: use cached data and paginate locally
                    val cached = ashaWorkerDao.getAllAshaWorkers().map { it.toAshaWorker() }
                    Log.d(
                        "SupervisorRepository",
                        "No network, loaded ${cached.size} cached ASHA workers"
                    )

                    val total = cached.size
                    if (total == 0) {
                        return@withContext Result.failure(
                            Exception("No network connection and no cached data available")
                        )
                    }

                    val fromIndex = ((page - 1) * limit).coerceAtLeast(0)
                    val toIndex = (fromIndex + limit).coerceAtMost(total)

                    val pageList = if (fromIndex >= total) {
                        emptyList()
                    } else {
                        cached.subList(fromIndex, toIndex)
                    }

                    val totalPages =
                        if (total == 0) 1 else ((total + limit - 1) / limit) // ceil(total/limit)

                    val response = AshaListResponse(
                        total = total,
                        page = page,
                        limit = limit,
                        totalPages = totalPages,
                        ashas = pageList
                    )

                    Result.success(response)
                }
            } catch (e: Exception) {
                Log.e("SupervisorRepository", "Error fetching ASHA workers (paginated)", e)

                // On error, try cached data as a fallback
                val cached = ashaWorkerDao.getAllAshaWorkers().map { it.toAshaWorker() }
                return@withContext if (cached.isNotEmpty()) {
                    val total = cached.size
                    val fromIndex = ((page - 1) * limit).coerceAtLeast(0)
                    val toIndex = (fromIndex + limit).coerceAtMost(total)

                    val pageList = if (fromIndex >= total) {
                        emptyList()
                    } else {
                        cached.subList(fromIndex, toIndex)
                    }

                    val totalPages =
                        if (total == 0) 1 else ((total + limit - 1) / limit)

                    val response = AshaListResponse(
                        total = total,
                        page = page,
                        limit = limit,
                        totalPages = totalPages,
                        ashas = pageList
                    )

                    Result.success(response)
                } else {
                    Result.failure(e)
                }
            }
        }

    /**
     * OLD helper kept for backward compatibility.
     * Returns a flat list (no pagination) using the new endpoint internally.
     *
     * Anywhere else in the app still calling getAshaWorkers() will keep working.
     */
    suspend fun getAshaWorkers(): Result<List<AshaWorker>> =
        withContext(Dispatchers.IO) {
            try {
                val page = 1
                val limit = 500 // some high number if you want "almost all"
                val pagedResult = getAshaWorkers(page, limit)

                pagedResult.map { it.ashas }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getSupervisorProfile(): Result<SupervisorProfile> =
        withContext(Dispatchers.IO) {
            try {
                if (isNetworkAvailable()) {
                    val token = TokenManager.getToken()
                    val response =
                        ApiClient.retrofit.getSupervisorProfile("Bearer $token").execute()

                    if (response.isSuccessful && response.body() != null) {
                        val profile = response.body()!!
                        // Cache in local database
                        supervisorProfileDao.insert(
                            SupervisorProfileEntity.fromSupervisorProfile(
                                profile
                            )
                        )
                        Log.d("SupervisorRepository", "Fetched profile from backend")
                        Result.success(profile)
                    } else {
                        val cachedProfile =
                            supervisorProfileDao.getProfile()?.toSupervisorProfile()
                        if (cachedProfile != null) {
                            Log.d(
                                "SupervisorRepository",
                                "Backend failed, using cached profile"
                            )
                            Result.success(cachedProfile)
                        } else {
                            Result.failure(
                                Exception("Failed to fetch: ${response.message()}")
                            )
                        }
                    }
                } else {
                    val cachedProfile =
                        supervisorProfileDao.getProfile()?.toSupervisorProfile()
                    Log.d("SupervisorRepository", "No network, using cached profile")
                    if (cachedProfile != null) {
                        Result.success(cachedProfile)
                    } else {
                        Result.failure(
                            Exception("No network connection and no cached profile")
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SupervisorRepository", "Error fetching profile", e)
                val cachedProfile =
                    supervisorProfileDao.getProfile()?.toSupervisorProfile()
                if (cachedProfile != null) {
                    Result.success(cachedProfile)
                } else {
                    Result.failure(e)
                }
            }
        }

    suspend fun updateSupervisorProfile(
        updateRequest: SelfUpdateRequest
    ): Result<SupervisorProfile> =
        withContext(Dispatchers.IO) {
            try {
                if (!isNetworkAvailable()) {
                    return@withContext Result.failure(
                        Exception("No network connection. Cannot update profile.")
                    )
                }

                val token = TokenManager.getToken()
                val response = ApiClient.retrofit
                    .updateSupervisorProfile("Bearer $token", updateRequest)
                    .execute()

                if (response.isSuccessful && response.body() != null) {
                    val updatedProfile = response.body()!!.profile
                    supervisorProfileDao.insert(
                        SupervisorProfileEntity.fromSupervisorProfile(
                            updatedProfile
                        )
                    )
                    Result.success(updatedProfile)
                } else {
                    Result.failure(
                        Exception("Failed to update: ${response.message()}")
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
