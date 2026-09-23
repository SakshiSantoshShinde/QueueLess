package com.example.queueless_smartqueue.ui.screens.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.queueless_smartqueue.model.SpecialUIState
import com.example.queueless_smartqueue.ui.components.*
import com.example.queueless_smartqueue.ui.theme.*

@Composable
fun UIStateDemoScreen(
    currentState: SpecialUIState,
    onStateSelected: (SpecialUIState) -> Unit,
    onBackClick: () -> Unit
) {
    val stateList = listOf(
        SpecialUIState.NORMAL to "Normal Flow",
        SpecialUIState.LOADING to "Loading Skeleton",
        SpecialUIState.EMPTY to "Empty State",
        SpecialUIState.ERROR to "Error State",
        SpecialUIState.QUEUE_PAUSED to "Queue Paused",
        SpecialUIState.COUNTER_UNAVAILABLE to "Counter Unavailable",
        SpecialUIState.TOKEN_COMPLETED to "Token Completed"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "UI State Showcase",
            subtitle = "Preview application states",
            showBackButton = true,
            onBackClick = onBackClick
        )

        // State Selector Filter Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "Select UI State to Test:",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stateList) { (state, label) ->
                    val isSelected = currentState == state
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStateSelected(state) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ActionBlue,
                            selectedLabelColor = CardWhite,
                            containerColor = CardWhite,
                            labelColor = TextPrimary
                        )
                    )
                }
            }
        }

        HorizontalDivider(color = DividerColor)

        // State View Renderer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (currentState) {
                SpecialUIState.LOADING -> {
                    Column {
                        Text(
                            text = "Skeleton Loading Preview",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        SkeletonLoadingView()
                    }
                }
                SpecialUIState.EMPTY -> {
                    EmptyStateView(
                        title = "No Active Queue",
                        subtitle = "You are not currently in any queue. Browse services to take a token.",
                        buttonText = "Browse Organizations",
                        onButtonClick = { onStateSelected(SpecialUIState.NORMAL) }
                    )
                }
                SpecialUIState.ERROR -> {
                    ErrorStateView(
                        errorMessage = "Something went wrong while updating live queue telemetry.",
                        onRetryClick = { onStateSelected(SpecialUIState.NORMAL) }
                    )
                }
                SpecialUIState.QUEUE_PAUSED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StateBanner(
                            title = "🟠 Queue Paused",
                            description = "Staff has temporarily paused this queue. Your position (A47) is saved.",
                            backgroundColor = StatusBackgroundAmber,
                            textColor = TextPrimary,
                            icon = Icons.Default.PauseCircle
                        )
                        ETACard(
                            estimatedWaitMinutes = 42,
                            peopleAhead = 15,
                            assignedCounter = "Counter 2",
                            etaUpdateReason = "Queue paused by staff. ETA frozen temporarily."
                        )
                    }
                }
                SpecialUIState.COUNTER_UNAVAILABLE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StateBanner(
                            title = "Counter 2 Temporarily Unavailable",
                            description = "Counter 2 is temporarily offline for maintenance. Serving redirected to Counter 1.",
                            backgroundColor = StatusBackgroundRed,
                            textColor = StatusRed,
                            icon = Icons.Default.Warning
                        )
                        ETACard(
                            estimatedWaitMinutes = 48,
                            peopleAhead = 15,
                            assignedCounter = "Counter 1 (Reassigned)",
                            etaUpdateReason = "ETA adjusted due to Counter 2 offline."
                        )
                    }
                }
                SpecialUIState.TOKEN_COMPLETED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StateBanner(
                            title = "🟢 Service Completed",
                            description = "Your service has been completed successfully at Counter 2. Thank you for using QueueLess!",
                            backgroundColor = StatusBackgroundGreen,
                            textColor = SuccessGreen,
                            icon = Icons.Default.CheckCircle
                        )
                        TokenCard(tokenNumber = "A47", label = "Completed Token", highlight = false)
                        PrimaryButton(
                            text = "Back to Home",
                            onClick = { onStateSelected(SpecialUIState.NORMAL) },
                            backgroundColor = ActionBlue
                        )
                    }
                }
                SpecialUIState.NORMAL -> {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Normal UI State Active",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Use the filter chips above to preview how QueueLess gracefully handles loading skeletons, empty queues, errors, queue pause events, counter offline states, and completed tokens.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PrimaryButton(
                            text = "Return to User Flow",
                            onClick = onBackClick,
                            backgroundColor = ActionBlue
                        )
                    }
                }
            }
        }
    }
}
