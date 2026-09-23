package com.example.queueless_smartqueue.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.model.TokenInfo
import com.example.queueless_smartqueue.ui.components.PrimaryButton
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.components.SecondaryButton
import com.example.queueless_smartqueue.ui.components.StatusBadge
import com.example.queueless_smartqueue.ui.theme.*

@Composable
fun StaffQueueManagementScreen(
    currentServingToken: String = "A32",
    onCallNext: () -> Unit,
    onPauseQueue: () -> Unit,
    onBackClick: () -> Unit
) {
    val currentTokenStr = currentServingToken
    val nextTokenNum = (currentTokenStr.substring(1).toIntOrNull() ?: 32) + 1
    val nextTokenStr = "A$nextTokenNum"
    val upcomingList = (nextTokenNum..(nextTokenNum + 5)).map { "A$it" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "Queue Management",
            subtitle = "Bonafide Certificate • Counter 2",
            showBackButton = true,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Main Serving & Next Display Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(
                                text = "🟢 Counter 2 Active",
                                backgroundColor = StatusBackgroundGreen,
                                textColor = SuccessGreen
                            )
                            Text(
                                text = "Queue Speed: Normal",
                                style = MaterialTheme.typography.bodySmall.copy(color = CardWhite.copy(alpha = 0.8f))
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CURRENT TOKEN",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CardWhite.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = currentTokenStr,
                                    style = QueueLessTypography.displayLarge.copy(
                                        color = CardWhite,
                                        fontSize = 48.sp
                                    )
                                )
                            }

                            Text(
                                text = "→",
                                style = MaterialTheme.typography.headlineLarge.copy(color = ActionBlue)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "NEXT TOKEN",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ActionBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = nextTokenStr,
                                    style = QueueLessTypography.displayLarge.copy(
                                        color = ActionBlue,
                                        fontSize = 48.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Prominent CALL NEXT Primary Action Button
            item {
                PrimaryButton(
                    text = "CALL NEXT ($nextTokenStr)",
                    onClick = onCallNext,
                    icon = Icons.Default.Campaign,
                    backgroundColor = ActionBlue,
                    modifier = Modifier.height(56.dp)
                )
            }

            // Secondary Controls (Skip, Cancel, Pause Queue)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecondaryButton(
                        text = "Skip",
                        onClick = onCallNext,
                        icon = Icons.Default.SkipNext,
                        modifier = Modifier.weight(1f)
                    )

                    SecondaryButton(
                        text = "Pause Queue",
                        onClick = onPauseQueue,
                        borderColor = WarningAmber,
                        textColor = WarningAmber,
                        icon = Icons.Default.PauseCircle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Upcoming Queue List Section
            item {
                Text(
                    text = "Upcoming Queue List (${upcomingList.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            items(upcomingList) { token ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = token,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Waiting",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        Text(
                            text = "Est call: ~${(upcomingList.indexOf(token) + 1) * 3} min",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ActionBlue
                            )
                        )
                    }
                }
            }
        }
    }
}
