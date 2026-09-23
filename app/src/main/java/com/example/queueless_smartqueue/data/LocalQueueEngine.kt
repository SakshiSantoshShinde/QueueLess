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
 */
object LocalQueueEngine {

    // 1. Organizations
    private val _organizations = MutableStateFlow(
        listOf(
            Organization("org_1", "RIT College Office", "College", "🏫", "Administrative Services • Main Campus", 3, true),
            Organization("org_2", "City Central Hospital", "Hospital", "🏥", "OPD & Registration Section", 4, true),
            Organization("org_3", "National Apex Bank", "Bank", "🏦", "Customer Service & Forex", 2, true),
            Organization("org_4", "Municipal Regional Office", "Government", "🏢", "Citizens Desk & Permits", 3, true)
        )
    )
    val organizations: StateFlow<List<Organization>> = _organizations.asStateFlow()

    // 2. Services
    private val _services = MutableStateFlow(
        listOf(
            QueueService("serv_1", "org_1", "Bonafide Certificate", "A32", 15, 42, 2, "📜"),
            QueueService("serv_2", "org_1", "Scholarship Section", "B18", 8, 18, 2, "🎓"),
            QueueService("serv_3", "org_1", "Exam & Transcript", "C05", 15, 30, 3, "📝"),
            QueueService("serv_4", "org_1", "Fees & Finance Dept", "F12", 21, 42, 1, "💰")
        )
    )
    val services: StateFlow<List<QueueService>> = _services.asStateFlow()

    // 3. Counters
    private val _counters = MutableStateFlow(
        listOf(
            CounterInfo(1, "Counter 1", "A31", true),
            CounterInfo(2, "Counter 2", "A32", true),
            CounterInfo(3, "Counter 3", null, false)
        )
    )
    val counters: StateFlow<List<CounterInfo>> = _counters.asStateFlow()

    // 3.1 Global Current Serving Token
    private var currentServingNumber = 32
    private val tokenPrefix = "A"

    private val _currentServingToken = MutableStateFlow("A32")
    val currentServingToken: StateFlow<String> = _currentServingToken.asStateFlow()

    // 4. User's Active Token
    private val _userToken = MutableStateFlow<TokenInfo?>(
        TokenInfo(
            tokenNumber = "A47",
            serviceId = "serv_1",
            serviceName = "Bonafide Certificate",
            orgName = "RIT College Office",
            currentlyServingToken = "A32",
            peopleAhead = 15,
            estimatedWaitMinutes = 42,
            assignedCounter = "Counter 2",
            recommendedArrival = "11:45 AM",
            status = TokenStatus.WAITING,
            progressSteps = listOf("A32", "A35", "A39", "A43", "A47"),
            etaUpdateReason = null
        )
    )
    val userToken: StateFlow<TokenInfo?> = _userToken.asStateFlow()

    // 5. Notifications
    private val _notifications = MutableStateFlow(
        listOf(
            NotificationItem("n1", NotificationType.APPROACHING, "Your turn is approaching", "Only 3 people are ahead of you in the queue.", "2m ago"),
            NotificationItem("n2", NotificationType.UPDATED, "Queue updated", "Your estimated waiting time is now 25 minutes.", "10m ago"),
            NotificationItem("n3", NotificationType.PROCEED, "Proceed to Counter 2", "Token A32 is currently being served at Counter 2.", "15m ago")
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // 6. History
    private val _history = MutableStateFlow(
        listOf(
            QueueHistoryItem("h1", "Bonafide Certificate", "RIT College Office", "Today • A47", "A47", "Completed"),
            QueueHistoryItem("h2", "Scholarship Department", "RIT College Office", "Yesterday • B23", "B23", "Completed"),
            QueueHistoryItem("h3", "OPD Consultation", "City Central Hospital", "15 Aug • H12", "H12", "Completed")
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
        val tokenPrefix = service.currentServingToken.take(1).ifEmpty { "A" }
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
        val currentNum = service.currentServingToken.substring(1).toIntOrNull() ?: 32
        val steps = generateProgressSteps(tokenPrefix, currentNum, lastTokenSeq)

        val newToken = TokenInfo(
            tokenNumber = newTokenNumber,
            serviceId = service.id,
            serviceName = service.name,
            orgName = "RIT College Office",
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
     * Advance the queue (Staff Call Next Action).
     */
    fun callNextToken(): TokenInfo? {
        currentServingNumber++
        val nextTokenStr = "$tokenPrefix$currentServingNumber"
        _currentServingToken.value = nextTokenStr

        val activeCounters = getActiveCountersCount()

        // 1. Update services current serving token
        _services.value = _services.value.map { service ->
            if (service.id == "serv_1") {
                val newWaiting = (service.peopleWaiting - 1).coerceAtLeast(0)
                service.copy(
                    currentServingToken = nextTokenStr,
                    peopleWaiting = newWaiting,
                    estimatedWaitMinutes = calculateWaitMinutes(newWaiting, activeCounters)
                )
            } else service
        }

        // 2. Update Counter 2 serving token
        _counters.value = _counters.value.map { counter ->
            if (counter.id == 2) counter.copy(currentlyServingToken = nextTokenStr) else counter
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
            if (isNowServing) {
                addNotification(
                    NotificationType.PROCEED,
                    "Proceed to Counter 2",
                    "Token ${current.tokenNumber} is currently being served at Counter 2."
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
                    "Please proceed near Counter 2."
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
        _organizations.value = _organizations.value.map { org ->
            if (org.id == "org_1") org.copy(activeCountersCount = activeCount) else org
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
