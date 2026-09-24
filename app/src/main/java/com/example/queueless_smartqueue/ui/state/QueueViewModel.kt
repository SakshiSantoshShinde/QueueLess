package com.example.queueless_smartqueue.ui.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queueless_smartqueue.data.LocalQueueEngine
import com.example.queueless_smartqueue.model.*
import com.example.queueless_smartqueue.network.QueueRepository
import com.example.queueless_smartqueue.network.RegisterOrgRequest
import com.example.queueless_smartqueue.network.RegisterServiceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * QueueViewModel connects UI screens directly to QueueRepository (Database API)
 * with LocalQueueEngine as an instant in-memory reactive store & fallback.
 * Fetches all organization and queue data dynamically from the database.
 */
class QueueViewModel(
    private val repository: QueueRepository = QueueRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "QueueViewModel"
    }

    // Exposed StateFlows backed by LocalQueueEngine (single source of truth for UI)
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedOrgId = MutableStateFlow<String?>("org_1")
    val selectedOrgId: StateFlow<String?> = _selectedOrgId.asStateFlow()

    init {
        loadInitialData()
    }

    /**
     * Fetch all initial data dynamically from the database.
     */
    fun loadInitialData(userId: String = "user_1") {
        viewModelScope.launch {
            _isLoading.value = true
            fetchOrganizations()
            fetchActiveToken(userId)
            fetchNotifications(userId)
            fetchHistory(userId)
            _isLoading.value = false
        }
    }

    /**
     * Fetch all registered organizations from the database.
     */
    fun fetchOrganizations(onComplete: ((List<Organization>) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.getOrganizations()
            result.onSuccess { orgs ->
                if (orgs.isNotEmpty()) {
                    Log.d(TAG, "Fetched ${orgs.size} organizations from database")
                    LocalQueueEngine.setOrganizations(orgs)
                    val activeOrgId = _selectedOrgId.value ?: orgs.firstOrNull()?.id
                    if (activeOrgId != null) {
                        fetchServices(activeOrgId)
                        fetchCounters(activeOrgId)
                        fetchStaffStats(activeOrgId)
                    }
                }
                onComplete?.invoke(LocalQueueEngine.organizations.value)
            }.onFailure { err ->
                Log.e(TAG, "Failed to fetch organizations: ${err.message}, retaining local data")
                val activeOrgId = _selectedOrgId.value ?: "org_1"
                LocalQueueEngine.filterServicesForOrg(activeOrgId)
                onComplete?.invoke(LocalQueueEngine.organizations.value)
            }
        }
    }

    /**
     * Fetch all services for a specific organization from the database.
     */
    fun fetchServices(orgId: String) {
        _selectedOrgId.value = orgId
        viewModelScope.launch {
            val result = repository.getServices(orgId)
            result.onSuccess { servs ->
                if (servs.isNotEmpty()) {
                    Log.d(TAG, "Fetched ${servs.size} services for org $orgId from database")
                    LocalQueueEngine.setServices(servs)
                } else {
                    LocalQueueEngine.filterServicesForOrg(orgId)
                }
            }.onFailure { err ->
                Log.e(TAG, "Failed to fetch services for $orgId: ${err.message}, using local services")
                LocalQueueEngine.filterServicesForOrg(orgId)
            }
        }
    }

    /**
     * Fetch active token for the current user from the database.
     */
    fun fetchActiveToken(userId: String = "user_1") {
        viewModelScope.launch {
            val result = repository.getActiveToken(userId)
            result.onSuccess { token ->
                LocalQueueEngine.setUserToken(token)
            }
        }
    }

    /**
     * Fetch counters for the active organization from the database.
     */
    fun fetchCounters(orgId: String = "org_1") {
        viewModelScope.launch {
            val result = repository.getCounters(orgId)
            result.onSuccess { countersList ->
                if (countersList.isNotEmpty()) {
                    LocalQueueEngine.setCounters(countersList)
                }
            }
        }
    }

    /**
     * Fetch notifications from the database.
     */
    fun fetchNotifications(userId: String = "user_1") {
        viewModelScope.launch {
            val result = repository.getNotifications(userId)
            result.onSuccess { notifs ->
                LocalQueueEngine.setNotifications(notifs)
            }
        }
    }

    /**
     * Fetch queue history from the database.
     */
    fun fetchHistory(userId: String = "user_1") {
        viewModelScope.launch {
            val result = repository.getHistory(userId)
            result.onSuccess { historyList ->
                LocalQueueEngine.setHistory(historyList)
            }
        }
    }

    /**
     * Fetch staff analytics and stats from the database.
     */
    fun fetchStaffStats(orgId: String = "org_1") {
        viewModelScope.launch {
            val result = repository.getStaffStats(orgId)
            result.onSuccess { stats ->
                LocalQueueEngine.setStaffStats(stats)
            }
        }
    }

    /**
     * Register a new organization with its services directly into the database.
     * After saving, re-fetches the organizations list from the database.
     */
    fun registerOrganization(
        request: RegisterOrgRequest,
        onSuccess: (Organization) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.registerOrganization(request)
            result.onSuccess { newOrg ->
                Log.d(TAG, "Successfully registered organization: ${newOrg.name} (${newOrg.id})")
                // Re-fetch organizations directly from database to ensure fresh state
                fetchOrganizations {
                    _isLoading.value = false
                    onSuccess(newOrg)
                }
            }.onFailure { err ->
                Log.e(TAG, "Error registering organization in database: ${err.message}")
                // Fallback: register locally in LocalQueueEngine if server is unreachable
                val fallbackId = "org_${System.currentTimeMillis()}"
                val fallbackOrg = Organization(
                    id = fallbackId,
                    name = request.name,
                    category = request.category,
                    iconEmoji = request.iconEmoji,
                    address = request.address,
                    activeCountersCount = request.activeCountersCount,
                    isOpen = request.isOpen
                )
                val fallbackServices = request.services.mapIndexed { index, s ->
                    QueueService(
                        id = "serv_${System.currentTimeMillis()}_$index",
                        orgId = fallbackId,
                        name = s.name,
                        currentServingToken = "${s.tokenPrefix}01",
                        peopleWaiting = 0,
                        estimatedWaitMinutes = 0,
                        activeCounters = request.activeCountersCount,
                        categoryEmoji = s.categoryEmoji
                    )
                }
                LocalQueueEngine.registerOrganizationLocally(fallbackOrg, fallbackServices)
                _isLoading.value = false
                onSuccess(fallbackOrg)
            }
        }
    }

    /**
     * Add a service to an existing organization in the database.
     */
    fun addService(
        orgId: String,
        request: RegisterServiceRequest,
        onSuccess: (QueueService) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.addService(orgId, request)
            result.onSuccess { createdService ->
                fetchServices(orgId)
                onSuccess(createdService)
            }.onFailure { err ->
                onError(err.message ?: "Failed to add service")
            }
        }
    }

    /**
     * Issue a new token for the user, saving it to the database.
     */
    fun takeToken(service: QueueService, userId: String = "user_1") {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.takeToken(service.id, userId)
            result.onSuccess { token ->
                LocalQueueEngine.setUserToken(token)
                fetchServices(service.orgId)
                fetchHistory(userId)
                _isLoading.value = false
            }.onFailure {
                // Fallback to local queue engine
                val localToken = LocalQueueEngine.takeToken(service)
                LocalQueueEngine.setUserToken(localToken)
                _isLoading.value = false
            }
        }
    }

    /**
     * Advance queue (Staff call next token) with instant optimistic UI update
     * and asynchronous database synchronization.
     */
    fun callNextToken(serviceId: String? = null, counterId: Int? = null) {
        val activeServiceId = serviceId ?: services.value.firstOrNull()?.id ?: "serv_1"
        val activeCounterId = counterId ?: counters.value.firstOrNull { it.isActive }?.id ?: 2

        // 1. Instant optimistic update so UI is immediately responsive as earlier
        LocalQueueEngine.callNextToken(activeCounterId)

        // 2. Synchronize with database in background
        viewModelScope.launch {
            val result = repository.callNextToken(activeServiceId, activeCounterId)
            result.onSuccess { resp ->
                Log.d(TAG, "callNextToken synced with database: ${resp.currentlyServingToken} at ${resp.counterName}")
                LocalQueueEngine.setCurrentServingToken(resp.currentlyServingToken)
                if (resp.userToken != null) {
                    LocalQueueEngine.setUserToken(resp.userToken)
                }

                // Update counter serving token in current counters flow
                val updatedCounters = counters.value.map { c ->
                    if (c.id == activeCounterId) c.copy(currentlyServingToken = resp.currentlyServingToken) else c
                }
                LocalQueueEngine.setCounters(updatedCounters)

                _selectedOrgId.value?.let { orgId ->
                    fetchStaffStats(orgId)
                }
            }.onFailure { err ->
                Log.w(TAG, "Backend callNextToken failed (${err.message}), local engine already updated")
            }
        }
    }

    /**
     * Toggle counter status in the database with instant optimistic UI update.
     */
    fun toggleCounter(counterId: Int) {
        // 1. Instant optimistic update so UI switch/button changes immediately
        LocalQueueEngine.toggleCounter(counterId)

        // 2. Synchronize with database
        viewModelScope.launch {
            val result = repository.toggleCounter(counterId)
            result.onSuccess { resp ->
                Log.d(TAG, "toggleCounter succeeded for counter $counterId: isActive=${resp.isActive}")
                LocalQueueEngine.setCounters(resp.counters)
                fetchActiveToken()
                _selectedOrgId.value?.let { fetchServices(it) }
            }.onFailure { err ->
                Log.w(TAG, "Backend toggleCounter failed (${err.message}), local state retained")
            }
        }
    }


    /**
     * Cancel the user's active token in the database.
     */
    fun cancelToken(tokenNumber: String? = null) {
        val activeTokenNum = tokenNumber ?: userToken.value?.tokenNumber
        if (activeTokenNum != null) {
            viewModelScope.launch {
                val result = repository.cancelToken(activeTokenNum)
                result.onSuccess {
                    LocalQueueEngine.setUserToken(null)
                    fetchHistory()
                    _selectedOrgId.value?.let { fetchServices(it) }
                }.onFailure {
                    LocalQueueEngine.cancelToken()
                }
            }
        } else {
            LocalQueueEngine.cancelToken()
        }
    }

    fun toggleStaffMode() {
        _isStaffMode.value = !_isStaffMode.value
    }

    fun setSpecialState(state: SpecialUIState) {
        LocalQueueEngine.setSpecialState(state)
    }
}
