package com.example.queueless_smartqueue.network

import com.example.queueless_smartqueue.model.CounterInfo
import com.example.queueless_smartqueue.model.TokenInfo

data class TakeTokenRequest(
    val serviceId: String,
    val userId: String = "user_1"
)

data class CallNextRequest(
    val serviceId: String = "serv_1",
    val counterId: Int = 2
)

data class CallNextResponse(
    val success: Boolean,
    val currentlyServingToken: String,
    val counterName: String,
    val userToken: TokenInfo?
)

data class CounterToggleResponse(
    val success: Boolean,
    val counterId: Int,
    val isActive: Boolean,
    val etaUpdateReason: String?,
    val counters: List<CounterInfo>
)

data class GenericResponse(
    val success: Boolean,
    val message: String? = null
)
