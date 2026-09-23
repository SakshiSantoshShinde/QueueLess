package com.example.queueless_smartqueue.model

data class Organization(
    val id: String,
    val name: String,
    val category: String,
    val iconEmoji: String,
    val address: String,
    val activeCountersCount: Int,
    val isOpen: Boolean = true
)

data class QueueService(
    val id: String,
    val orgId: String,
    val name: String,
    val currentServingToken: String,
    val peopleWaiting: Int,
    val estimatedWaitMinutes: Int,
    val activeCounters: Int,
    val categoryEmoji: String = "📄"
)

enum class TokenStatus {
    WAITING,
    SERVING,
    COMPLETED,
    CANCELLED
}

data class TokenInfo(
    val tokenNumber: String,
    val serviceId: String,
    val serviceName: String,
    val orgName: String,
    val currentlyServingToken: String,
    val peopleAhead: Int,
    val estimatedWaitMinutes: Int,
    val assignedCounter: String,
    val recommendedArrival: String,
    val status: TokenStatus,
    val progressSteps: List<String>,
    val etaUpdateReason: String? = null
)

data class CounterInfo(
    val id: Int,
    val name: String,
    val currentlyServingToken: String?,
    val isActive: Boolean
)

enum class NotificationType {
    APPROACHING,
    UPDATED,
    PROCEED,
    INFO
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val timeAgo: String
)

data class QueueHistoryItem(
    val id: String,
    val serviceName: String,
    val orgName: String,
    val dateText: String,
    val tokenNumber: String,
    val status: String
)

data class StaffStats(
    val totalTokens: Int,
    val completed: Int,
    val waiting: Int,
    val avgWaitMinutes: Int
)

enum class SpecialUIState {
    NORMAL,
    LOADING,
    EMPTY,
    ERROR,
    QUEUE_PAUSED,
    COUNTER_UNAVAILABLE,
    TOKEN_COMPLETED
}
