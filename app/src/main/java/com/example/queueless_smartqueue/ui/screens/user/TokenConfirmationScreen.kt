package com.example.queueless_smartqueue.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun TokenConfirmationScreen(
    tokenInfo: TokenInfo?,
    onViewLiveQueueClick: () -> Unit,
    onCancelTokenClick: () -> Unit
) {
    val tokenNumber = tokenInfo?.tokenNumber ?: "A47"
    val serviceName = tokenInfo?.serviceName ?: "Bonafide Certificate"
    val peopleAhead = tokenInfo?.peopleAhead ?: 15
    val estimatedWaitMinutes = tokenInfo?.estimatedWaitMinutes ?: 42
    val assignedCounter = tokenInfo?.assignedCounter ?: "Counter 2"
    val progressSteps = tokenInfo?.progressSteps ?: listOf("A32", "A35", "A39", "A43", "A47")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "Token Issued",
            subtitle = serviceName
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(StatusBackgroundGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "You're in the queue!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Token Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, ActionBlue, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "YOUR TOKEN NUMBER",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ActionBlue,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tokenNumber,
                            style = QueueLessTypography.displayLarge.copy(color = DeepNavy)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("People Ahead", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "$peopleAhead",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Est Wait Time", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "$estimatedWaitMinutes min",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ActionBlue
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Assigned", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = assignedCounter,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Queue Step Timeline Indicator (A32 → A35 → A39 → A43 → A47)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ChipBackground),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Queue Steps Overview",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ActionBlue
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            progressSteps.forEachIndexed { index, step ->
                                val isUserToken = step == tokenNumber
                                Surface(
                                    color = if (isUserToken) ActionBlue else CardWhite,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.border(
                                        1.dp,
                                        if (isUserToken) ActionBlue else CardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                ) {
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isUserToken) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isUserToken) CardWhite else TextPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                if (index < progressSteps.size - 1) {
                                    Text(
                                        text = "→",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryButton(
                    text = "View Live Queue",
                    onClick = onViewLiveQueueClick,
                    icon = Icons.Default.Visibility,
                    backgroundColor = ActionBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                SecondaryButton(
                    text = "Cancel Token",
                    onClick = onCancelTokenClick,
                    borderColor = StatusRed,
                    textColor = StatusRed
                )
            }
        }
    }
}
