package com.example.queueless_smartqueue.network

import android.util.Log
import com.example.queueless_smartqueue.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QueueRepository(
    private val apiService: QueueApiService = ApiClient.apiService
) {
    companion object {
        private const val TAG = "QueueRepository"
    }

    suspend fun getOrganizations(): Result<List<Organization>> = withContext(Dispatchers.IO) {
        try {
            val orgs = apiService.getOrganizations()
            Result.success(orgs)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for getOrganizations(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun registerOrganization(request: RegisterOrgRequest): Result<Organization> = withContext(Dispatchers.IO) {
        try {
            val org = apiService.registerOrganization(request)
            Result.success(org)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for registerOrganization(): ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addService(orgId: String, request: RegisterServiceRequest): Result<QueueService> = withContext(Dispatchers.IO) {
        try {
            val service = apiService.addService(orgId, request)
            Result.success(service)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for addService(): ${e.message}")
            Result.failure(e)
        }
    }


    suspend fun getServices(orgId: String = "org_1"): Result<List<QueueService>> = withContext(Dispatchers.IO) {
        try {
            val services = apiService.getServices(orgId)
            Result.success(services)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for getServices(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getCounters(orgId: String = "org_1"): Result<List<CounterInfo>> = withContext(Dispatchers.IO) {
        try {
            val counters = apiService.getCounters(orgId)
            Result.success(counters)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for getCounters(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun toggleCounter(id: Int): Result<CounterToggleResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.toggleCounter(id)
            Result.success(response)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for toggleCounter(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun takeToken(serviceId: String, userId: String = "user_1"): Result<TokenInfo> = withContext(Dispatchers.IO) {
        try {
            val token = apiService.takeToken(TakeTokenRequest(serviceId, userId))
            Result.success(token)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for takeToken(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getActiveToken(userId: String = "user_1"): Result<TokenInfo?> = withContext(Dispatchers.IO) {
        try {
            val token = apiService.getActiveToken(userId)
            Result.success(token)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for getActiveToken(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun cancelToken(tokenNumber: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.cancelToken(tokenNumber)
            Result.success(response.success)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for cancelToken(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun callNextToken(serviceId: String = "serv_1", counterId: Int = 2): Result<CallNextResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.callNextToken(CallNextRequest(serviceId, counterId))
            Result.success(response)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for callNextToken(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getNotifications(userId: String = "user_1"): Result<List<NotificationItem>> = withContext(Dispatchers.IO) {
        try {
            val notifs = apiService.getNotifications(userId)
            Result.success(notifs)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for getNotifications(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getHistory(userId: String = "user_1"): Result<List<QueueHistoryItem>> = withContext(Dispatchers.IO) {
        try {
            val history = apiService.getHistory(userId)
            Result.success(history)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for getHistory(), falling back: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getStaffStats(orgId: String = "org_1"): Result<StaffStats> = withContext(Dispatchers.IO) {
        try {
            val stats = apiService.getStaffStats(orgId)
            Result.success(stats)
        } catch (e: Exception) {
            Log.w(TAG, "Backend unreachable for getStaffStats(), falling back: ${e.message}")
            Result.failure(e)
        }
    }
}
