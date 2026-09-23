package com.example.queueless_smartqueue.ui.state

import androidx.lifecycle.ViewModel
import com.example.queueless_smartqueue.data.LocalQueueEngine
import com.example.queueless_smartqueue.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * QueueViewModel connects UI screens directly to the self-contained LocalQueueEngine.
 * Enables 100% self-contained execution inside Android Studio with zero external dependencies.
 */
class QueueViewModel : ViewModel() {

    val organizations: StateFlow<List<Organization>> = LocalQueueEngine.organizations
    val services: StateFlow<List<QueueService>> = LocalQueueEngine.services
    val userToken: StateFlow<TokenInfo?> = LocalQueueEngine.userToken
    val counters: StateFlow<List<CounterInfo>> = LocalQueueEngine.counters
    val staffStats: StateFlow<StaffStats> = LocalQueueEngine.staffStats
    val notifications: StateFlow<List<NotificationItem>> = LocalQueueEngine.notifications
    val history: StateFlow<List<QueueHistoryItem>> = LocalQueueEngine.history
    val specialState: StateFlow<SpecialUIState> = LocalQueueEngine.specialState
    val currentServingToken: StateFlow<String> = LocalQueueEngine.currentServingToken

    private val _isStaffMode = MutableStateFlow(false)
    val isStaffMode: StateFlow<Boolean> = _isStaffMode.asStateFlow()

    fun toggleStaffMode() {
        _isStaffMode.value = !_isStaffMode.value
    }

    fun setSpecialState(state: SpecialUIState) {
        LocalQueueEngine.setSpecialState(state)
    }

    fun callNextToken() {
        LocalQueueEngine.callNextToken()
    }

    fun toggleCounter(counterId: Int) {
        LocalQueueEngine.toggleCounter(counterId)
    }

    fun takeToken(service: QueueService): TokenInfo {
        return LocalQueueEngine.takeToken(service)
    }

    fun cancelToken() {
        LocalQueueEngine.cancelToken()
    }
}
