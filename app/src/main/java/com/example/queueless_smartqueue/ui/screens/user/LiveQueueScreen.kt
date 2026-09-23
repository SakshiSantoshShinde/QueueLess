package com.example.queueless_smartqueue.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.model.CounterInfo
import com.example.queueless_smartqueue.model.TokenInfo
import com.example.queueless_smartqueue.ui.components.*
import com.example.queueless_smartqueue.ui.theme.*

@Composable
fun LiveQueueScreen(
    tokenInfo: TokenInfo?,
    counters: List<CounterInfo>,
    onSimulateNextToken: () -> Unit,
    onSimulateCounterToggle: () -> Unit,
    onCancelToken: () -> Unit
) {
    if (tokenInfo == null) {
        EmptyStateView(
            title = "No active queues",
            subtitle = "Join a queue to see your real-time tracking here.",
            buttonText = "Find a Service"
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "Your Queue",
            subtitle = tokenInfo.serviceName,
            actions = {
                IconButton(onClick = onSimulateNextToken) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh / Next Token",
                        tint = ActionBlue
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Live Status Header Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(
                        text = "🟢 Queue Moving",
                        backgroundColor = StatusBackgroundGreen,
                        textColor = SuccessGreen
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Updated just now",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            // Central Token Info Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, ActionBlue, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "CURRENTLY SERVING",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                )
                                Text(
                                    text = tokenInfo.currentlyServingToken,
                                    style = QueueLessTypography.headlineLarge.copy(color = TextPrimary)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "YOUR TOKEN",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ActionBlue
                                    )
                                )
                                Text(
                                    text = tokenInfo.tokenNumber,
                                    style = QueueLessTypography.headlineLarge.copy(color = ActionBlue)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Bar calculation
                        val progressFraction = if (tokenInfo.peopleAhead > 0) {
                            (1f - (tokenInfo.peopleAhead.toFloat() / 20f)).coerceIn(0.1f, 0.95f)
                        } else 1f

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Queue Progress",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "${(progressFraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ActionBlue
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ActionBlue,
                                trackColor = ChipBackground
                            )
                        }
                    }
                }
            }

            // ETA Waiting Time Card
            item {
                ETACard(
                    estimatedWaitMinutes = tokenInfo.estimatedWaitMinutes,
                    peopleAhead = tokenInfo.peopleAhead,
                    assignedCounter = tokenInfo.assignedCounter,
                    etaUpdateReason = tokenInfo.etaUpdateReason
                )
            }

            // Dynamic ETA Simulator Bar
            item {
                Surface(
                    color = ChipBackground,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dynamic ETA Prediction",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ActionBlue
                                )
                            )
                            Text(
                                text = "Estimated time may change as counters become available.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        TextButton(onClick = onSimulateCounterToggle) {
                            Text(
                                text = "Toggle C3",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ActionBlue
                                )
                            )
                        }
                    }
                }
            }

            // Recommended Arrival Time Section (QueueLess Core Feature)
            item {
                RecommendedArrivalTimeCard(recommendedTime = tokenInfo.recommendedArrival)
            }

            // Queue Status Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Counter Live Status",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        counters.forEachIndexed { index, counter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (counter.isActive) SuccessGreen else StatusRed)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = counter.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    )
                                }

                                Text(
                                    text = if (counter.isActive) "Serving: ${counter.currentlyServingToken ?: "—"}" else "Offline",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (counter.isActive) ActionBlue else TextSecondary
                                    )
                                )
                            }
                            if (index < counters.size - 1) {
                                HorizontalDivider(color = DividerColor)
                            }
                        }
                    }
                }
            }

            // Cancel button action
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SecondaryButton(
                    text = "Cancel Token",
                    onClick = onCancelToken,
                    borderColor = StatusRed,
                    textColor = StatusRed
                )
            }
        }
    }
}
