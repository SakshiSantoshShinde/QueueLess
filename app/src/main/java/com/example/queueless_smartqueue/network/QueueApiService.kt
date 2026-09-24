package com.example.queueless_smartqueue.network

import com.example.queueless_smartqueue.model.*
import retrofit2.http.*

interface QueueApiService {

    @GET("organizations")
    suspend fun getOrganizations(): List<Organization>

    @POST("organizations")
    suspend fun registerOrganization(
        @Body request: RegisterOrgRequest
    ): Organization

    @GET("organizations/{orgId}/services")
    suspend fun getServices(
        @Path("orgId") orgId: String
    ): List<QueueService>

    @POST("organizations/{orgId}/services")
    suspend fun addService(
        @Path("orgId") orgId: String,
        @Body request: RegisterServiceRequest
    ): QueueService


    @GET("counters")
    suspend fun getCounters(
        @Query("orgId") orgId: String = "org_1"
    ): List<CounterInfo>

    @PATCH("counters/{id}/toggle")
    suspend fun toggleCounter(
        @Path("id") id: Int
    ): CounterToggleResponse

    @POST("tokens/take")
    suspend fun takeToken(
        @Body request: TakeTokenRequest
    ): TokenInfo

    @GET("tokens/active")
    suspend fun getActiveToken(
        @Query("userId") userId: String = "user_1"
    ): TokenInfo?

    @POST("tokens/{tokenNumber}/cancel")
    suspend fun cancelToken(
        @Path("tokenNumber") tokenNumber: String
    ): GenericResponse

    @GET("tokens/history")
    suspend fun getHistory(
        @Query("userId") userId: String = "user_1"
    ): List<QueueHistoryItem>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("userId") userId: String = "user_1"
    ): List<NotificationItem>

    @GET("staff/stats")
    suspend fun getStaffStats(
        @Query("orgId") orgId: String = "org_1"
    ): StaffStats

    @POST("staff/queue/next")
    suspend fun callNextToken(
        @Body request: CallNextRequest
    ): CallNextResponse
}
