package com.example.queueless_smartqueue.data

import com.example.queueless_smartqueue.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Self-contained Smart Queue Engine running 100% locally inside the Android app.
 * Provides complete backend business logic:
 * - Dynamic ETA recalculation based on active counters & queue depth
 * - Real-time queue progression (Call Next Token)
 * - Counter management & offline/online toggles
 * - Proactive smart notification triggers (Approaching, Proceed to Counter)
 * - User queue history & Admin analytics
 * - Persistent default data for organizations, services, counters, tokens & stats
 */
object LocalQueueEngine {

    // Default Organizations
    val defaultOrganizations = listOf(
        Organization("org_1", "RIT College Office", "College", "🏫", "Administrative Services • Main Campus", 3, true),
        Organization("org_2", "City Central Hospital", "Hospital", "🏥", "OPD & Registration Section • Building B", 4, true),
        Organization("org_3", "National Apex Bank", "Bank", "🏦", "Customer Service & Forex • Central Branch", 3, true),
        Organization("org_4", "Municipal Regional Office", "Government", "🏢", "Citizens Desk & Permits • City Center", 3, true)
    )

    // Default Services across all Organizations
    val defaultServices = listOf(
        // Org 1 Services (College)
        QueueService("serv_1", "org_1", "Bonafide Certificate", "A36", 14, 35, 2, "📜"),
        QueueService("serv_2", "org_1", "Scholarship Section", "B18", 8, 18, 2, "🎓"),
        QueueService("serv_3", "org_1", "Exam & Transcript", "C05", 15, 30, 3, "📝"),
        QueueService("serv_4", "org_1", "Fees & Finance Dept", "F12", 21, 42, 1, "💰"),
        QueueService("serv_admit", "org_1", "Admission & Verification", "D08", 6, 15, 2, "📂"),

        // Org 2 Services (Hospital)
        QueueService("serv_5", "org_2", "General OPD Consultation", "H14", 12, 36, 3, "🩺"),
        QueueService("serv_6", "org_2", "Laboratory & Blood Test", "L09", 6, 15, 2, "🧪"),
        QueueService("serv_rad", "org_2", "Radiology & X-Ray", "R04", 5, 25, 2, "🩻"),
        QueueService("serv_pharm", "org_2", "Pharmacy Dispensing", "P25", 9, 12, 3, "💊"),

        // Org 3 Services (Bank)
        QueueService("serv_7", "org_3", "Cash Deposit & Withdrawal", "D15", 7, 14, 2, "💵"),
        QueueService("serv_8", "org_3", "Account Opening & KYC", "N04", 5, 20, 1, "💳"),
        QueueService("serv_loan", "org_3", "Loans & Mortgages Desk", "M06", 4, 24, 1, "🏦"),
        QueueService("serv_forex", "org_3", "Forex & International Wire", "X02", 2, 10, 1, "🌐"),

        // Org 4 Services (Government)
        QueueService("serv_9", "org_4", "Property Tax & Assessment", "T22", 10, 25, 2, "🏠"),
        QueueService("serv_10", "org_4", "Birth & Death Certificates", "C11", 8, 16, 2, "📜"),
        QueueService("serv_11", "org_4", "Trade License & Permits", "P05", 4, 12, 1, "📑"),
        QueueService("serv_water", "org_4", "Water & Utilities Desk", "W07", 6, 18, 2, "💧")
    )

    // Master list of all registered services
    private val _allServices = defaultServices.toMutableList()

    // 1. Organizations (Initialized with default data and updated from database)
    private val _organizations = MutableStateFlow(defaultOrganizations)
    val organizations: StateFlow<List<Organization>> = _organizations.asStateFlow()

    // 2. Services (Filtered for currently active organization)
    private val _services = MutableStateFlow(defaultServices.filter { it.orgId == "org_1" })
    val services: StateFlow<List<QueueService>> = _services.asStateFlow()

    // 3. Counters (Loaded with operational counters)
    private val _counters = MutableStateFlow(
        listOf(
            CounterInfo(1, "Counter 1", "A31", true),
            CounterInfo(2, "Counter 2", "A36", true),
            CounterInfo(3, "Counter 3", null, false)
        )
    )
    val counters: StateFlow<List<CounterInfo>> = _counters.asStateFlow()

    // 3.1 Global Current Serving Token
    private var currentServingNumber = 36

    private val _currentServingToken = MutableStateFlow("A36")
    val currentServingToken: StateFlow<String> = _currentServingToken.asStateFlow()

    // 4. User's Active Token
    private val _userToken = MutableStateFlow<TokenInfo?>(
        TokenInfo(
            tokenNumber = "A47",
            serviceId = "serv_1",
            serviceName = "Bonafide Certificate",
            orgName = "RIT College Office",
            currentlyServingToken = "A36",
            peopleAhead = 11,
            estimatedWaitMinutes = 28,
            assignedCounter = "Counter 2",
            recommendedArrival = "11:45 AM",
            status = TokenStatus.WAITING,
            progressSteps = listOf("A36", "A39", "A42", "A45", "A47"),
            etaUpdateReason = null
        )
    )
    val userToken: StateFlow<TokenInfo?> = _userToken.asStateFlow()

    // 5. Notifications
    private val _notifications = MutableStateFlow(
        listOf(
            NotificationItem("n1", NotificationType.APPROACHING, "Your turn is approaching", "Only 3 people are ahead of you in the queue.", "2m ago"),
            NotificationItem("n2", NotificationType.UPDATED, "Queue updated", "Your estimated waiting time is now 25 minutes.", "10m ago"),
            NotificationItem("n3", NotificationType.PROCEED, "Proceed to Counter 2", "Token A36 is currently being served at Counter 2.", "15m ago")
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // 6. History
    private val _history = MutableStateFlow(
        listOf(
            QueueHistoryItem("h1", "Bonafide Certificate", "RIT College Office", "Today • A47", "A47", "Completed"),
            QueueHistoryItem("h2", "Scholarship Department", "RIT College Office", "Yesterday • B23", "B23", "Completed"),
            QueueHistoryItem("h3", "General OPD Consultation", "City Central Hospital", "15 Aug • H12", "H12", "Completed"),
            QueueHistoryItem("h4", "Cash Deposit & Withdrawal", "National Apex Bank", "10 Aug • D05", "D05", "Completed")
        )
    )
    val history: StateFlow<List<QueueHistoryItem>> = _history.asStateFlow()

    // 7. Staff Statistics
    private val _staffStats = MutableStateFlow(
        StaffStats(
            totalTokens = 247,
            completed = 198,
            waiting = 28,
            avgWaitMinutes = 18
        )
    )
    val staffStats: StateFlow<StaffStats> = _staffStats.asStateFlow()

    // 8. Special UI State
    private val _specialState = MutableStateFlow(SpecialUIState.NORMAL)
    val specialState: StateFlow<SpecialUIState> = _specialState.asStateFlow()

    // Setters for syncing with Database
    fun setOrganizations(list: List<Organization>) {
        if (list.isNotEmpty()) {
            _organizations.value = list
        }
    }

    fun setServices(list: List<QueueService>) {
        if (list.isNotEmpty()) {
            _services.value = list
            // Update master list with newly fetched/updated services
            list.forEach { updated ->
                val index = _allServices.indexOfFirst { it.id == updated.id }
                if (index != -1) {
                    _allServices[index] = updated
                } else {
                    _allServices.add(updated)
                }
            }
        }
    }

    fun filterServicesForOrg(orgId: String) {
        val filtered = _allServices.filter { it.orgId == orgId }
        _services.value = if (filtered.isNotEmpty()) filtered else _allServices.filter { it.orgId == "org_1" }
    }

    fun setCounters(list: List<CounterInfo>) {
        if (list.isNotEmpty()) {
            _counters.value = list
        }
    }

    fun setUserToken(token: TokenInfo?) {
        _userToken.value = token
    }

    fun setNotifications(list: List<NotificationItem>) {
        if (list.isNotEmpty()) {
            _notifications.value = list
        }
    }

    fun setHistory(list: List<QueueHistoryItem>) {
        if (list.isNotEmpty()) {
            _history.value = list
        }
    }

    fun setStaffStats(stats: StaffStats) {
        _staffStats.value = stats
    }

    fun setCurrentServingToken(token: String) {
        _currentServingToken.value = token
        val digits = token.filter { it.isDigit() }.toIntOrNull()
        if (digits != null) {
            currentServingNumber = digits
        }
    }

    fun registerOrganizationLocally(org: Organization, servicesList: List<QueueService>) {
        _organizations.value = _organizations.value + org
        _allServices.addAll(servicesList)
        _services.value = servicesList
    }

    // Internal sequence counter for tokens
    private var lastTokenSeq = 47

    /**
     * Calculate dynamic ETA based on people ahead and active counters.
     */
    private fun calculateWaitMinutes(peopleAhead: Int, activeCounters: Int, avgMinutesPerToken: Double = 2.5): Int {
        if (peopleAhead <= 0) return 0
        val countersCount = activeCounters.coerceAtLeast(1)
        return Math.max(1, ((peopleAhead * avgMinutesPerToken) / countersCount).toInt())
    }

    private fun getActiveCountersCount(): Int {
        return _counters.value.count { it.isActive }.coerceAtLeast(1)
    }

    /**
     * Issue a new token for the specified service.
     */
    fun takeToken(service: QueueService): TokenInfo {
        lastTokenSeq++
        val tokenPrefix = service.currentServingToken.takeWhile { !it.isDigit() }.ifEmpty { "A" }
        val newTokenNumber = "$tokenPrefix$lastTokenSeq"

        val activeCounters = getActiveCountersCount()
        val peopleAhead = service.peopleWaiting
        val estWait = calculateWaitMinutes(peopleAhead, activeCounters)

        // Recommended arrival time formatting (current time + estWait - 5 mins)
        val arrivalCalendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, (estWait - 5).coerceAtLeast(5))
        }
        val arrivalTimeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(arrivalCalendar.time)

        // Generate milestone steps between currently serving and new token
        val currentNum = service.currentServingToken.filter { it.isDigit() }.toIntOrNull() ?: 36
        val steps = generateProgressSteps(tokenPrefix, currentNum, lastTokenSeq)

        val newToken = TokenInfo(
            tokenNumber = newTokenNumber,
            serviceId = service.id,
            serviceName = service.name,
            orgName = _organizations.value.find { it.id == service.orgId }?.name ?: "Organization",
            currentlyServingToken = service.currentServingToken,
            peopleAhead = peopleAhead,
            estimatedWaitMinutes = estWait,
            assignedCounter = "Counter 2",
            recommendedArrival = arrivalTimeStr,
            status = TokenStatus.WAITING,
            progressSteps = steps,
            etaUpdateReason = null
        )

        _userToken.value = newToken
        _specialState.value = SpecialUIState.NORMAL

        // Increment waiting count in service
        _services.value = _services.value.map {
            if (it.id == service.id) it.copy(peopleWaiting = it.peopleWaiting + 1) else it
        }

        // Update Staff stats
        val stats = _staffStats.value
        _staffStats.value = stats.copy(
            totalTokens = stats.totalTokens + 1,
            waiting = stats.waiting + 1
        )

        return newToken
    }

    /**
     * Advance the queue (Staff Call Next Action) with immediate local state update.
     */
    fun callNextToken(targetCounterId: Int = 2): TokenInfo? {
        val currentStr = _currentServingToken.value
        val prefix = currentStr.takeWhile { !it.isDigit() }.ifEmpty { "A" }
        val currentNum = currentStr.filter { it.isDigit() }.toIntOrNull() ?: currentServingNumber
        currentServingNumber = currentNum + 1
        val hasLeadingZero = currentStr.length > prefix.length && currentStr[prefix.length] == '0' && currentServingNumber < 10
        val nextTokenStr = if (hasLeadingZero) String.format("%s%02d", prefix, currentServingNumber) else "$prefix$currentServingNumber"
        _currentServingToken.value = nextTokenStr

        val activeCounters = getActiveCountersCount()

        // 1. Update services current serving token
        _services.value = _services.value.mapIndexed { idx, service ->
            if (idx == 0 || service.currentServingToken == currentStr || service.id == "serv_1") {
                val newWaiting = (service.peopleWaiting - 1).coerceAtLeast(0)
                service.copy(
                    currentServingToken = nextTokenStr,
                    peopleWaiting = newWaiting,
                    estimatedWaitMinutes = calculateWaitMinutes(newWaiting, activeCounters)
                )
            } else service
        }

        // 2. Update target counter (defaults to Counter 2) serving token
        _counters.value = _counters.value.map { counter ->
            if (counter.id == targetCounterId) {
                counter.copy(currentlyServingToken = nextTokenStr)
            } else counter
        }

        // 3. Update User Token if user has an active token
        val current = _userToken.value
        if (current != null) {
            val isNowServing = (current.tokenNumber == nextTokenStr)
            val newStatus = if (isNowServing) TokenStatus.SERVING else TokenStatus.WAITING
            val newPeopleAhead = if (isNowServing) 0 else (current.peopleAhead - 1).coerceAtLeast(0)
            val newWaitTime = if (isNowServing) 0 else calculateWaitMinutes(newPeopleAhead, activeCounters)

            val updatedToken = current.copy(
                currentlyServingToken = nextTokenStr,
                peopleAhead = newPeopleAhead,
                estimatedWaitMinutes = newWaitTime,
                status = newStatus,
                etaUpdateReason = "ETA updated based on current queue speed."
            )
            _userToken.value = updatedToken

            // Trigger smart proactive notifications
            val counterName = _counters.value.find { it.id == targetCounterId }?.name ?: "Counter $targetCounterId"
            if (isNowServing) {
                addNotification(
                    NotificationType.PROCEED,
                    "Proceed to $counterName",
                    "Token ${current.tokenNumber} is currently being served at $counterName."
                )
            } else if (newPeopleAhead == 3) {
                addNotification(
                    NotificationType.APPROACHING,
                    "Your turn is approaching",
                    "Only 3 people are ahead of you in the queue."
                )
            } else if (newPeopleAhead == 1) {
                addNotification(
                    NotificationType.APPROACHING,
                    "You're next in line!",
                    "Please proceed near $counterName."
                )
            }
        }

        // 4. Update Staff Stats
        val stats = _staffStats.value
        _staffStats.value = stats.copy(
            completed = stats.completed + 1,
            waiting = (stats.waiting - 1).coerceAtLeast(0)
        )

        return _userToken.value
    }

    /**
     * Toggle a counter active/offline with instant dynamic ETA recalculation across queue.
     */
    fun toggleCounter(counterId: Int) {
        val updatedCounters = _counters.value.map { counter ->
            if (counter.id == counterId) {
                counter.copy(isActive = !counter.isActive)
            } else counter
        }
        _counters.value = updatedCounters

        val toggledCounter = updatedCounters.find { it.id == counterId }
        val isNowActive = toggledCounter?.isActive == true
        val activeCount = updatedCounters.count { it.isActive }.coerceAtLeast(1)

        val reason = if (isNowActive) {
            "ETA updated because ${toggledCounter?.name ?: "Counter $counterId"} is now active."
        } else {
            "ETA updated: ${toggledCounter?.name ?: "Counter $counterId"} is offline."
        }

        // Recalculate ETA for user token
        val current = _userToken.value
        if (current != null && current.status == TokenStatus.WAITING) {
            val newWaitTime = calculateWaitMinutes(current.peopleAhead, activeCount)
            _userToken.value = current.copy(
                estimatedWaitMinutes = newWaitTime,
                etaUpdateReason = reason
            )
        }

        // Update active counters count in organizations
        val activeOrgId = _services.value.firstOrNull()?.orgId ?: "org_1"
        _organizations.value = _organizations.value.map { org ->
            if (org.id == activeOrgId) org.copy(activeCountersCount = activeCount) else org
        }

        // Add notification for counter status change
        addNotification(NotificationType.UPDATED, "Queue Speed Adjusted", reason)
    }

    /**
     * Cancel the user's active token.
     */
    fun cancelToken() {
        val current = _userToken.value
        if (current != null) {
            // Add to history as Cancelled
            val historyItem = QueueHistoryItem(
                id = "h_${System.currentTimeMillis()}",
                serviceName = current.serviceName,
                orgName = current.orgName,
                dateText = "Today • ${current.tokenNumber}",
                tokenNumber = current.tokenNumber,
                status = "Cancelled"
            )
            _history.value = listOf(historyItem) + _history.value

            // Decrement waiting count on service
            _services.value = _services.value.map { s ->
                if (s.id == current.serviceId) s.copy(peopleWaiting = (s.peopleWaiting - 1).coerceAtLeast(0)) else s
            }
        }

        _userToken.value = null
        _specialState.value = SpecialUIState.EMPTY
    }

    fun setSpecialState(state: SpecialUIState) {
        _specialState.value = state
    }

    private fun addNotification(type: NotificationType, title: String, message: String) {
        val newNotif = NotificationItem(
            id = "n_${System.currentTimeMillis()}",
            type = type,
            title = title,
            message = message,
            timeAgo = "Just now"
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    private fun generateProgressSteps(prefix: String, currentNum: Int, targetNum: Int): List<String> {
        if (targetNum <= currentNum) return listOf("$prefix$currentNum", "$prefix$targetNum")
        val stepCount = 5
        val interval = (targetNum - currentNum).toDouble() / (stepCount - 1)
        val steps = mutableListOf<String>()
        for (i in 0 until stepCount) {
            val num = Math.round(currentNum + interval * i).toInt()
            steps.add("$prefix$num")
        }
        steps[0] = "$prefix$currentNum"
        steps[steps.size - 1] = "$prefix$targetNum"
        return steps.distinct()
    }
}
