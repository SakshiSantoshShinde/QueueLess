package com.example.queueless_smartqueue.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.model.TokenInfo
import com.example.queueless_smartqueue.ui.components.*
import com.example.queueless_smartqueue.ui.theme.*

@Composable
fun MyQueueScreen(
    userToken: TokenInfo?,
    onTrackQueueClick: () -> Unit,
    onFindServiceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "My Queue",
            subtitle = "Active Token Status"
        )

        if (userToken == null) {
            EmptyStateView(
                title = "No active queues",
                subtitle = "Join a queue to see it here.",
                buttonText = "Find a Service",
                onButtonClick = onFindServiceClick
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Active Queue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = userToken.serviceName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = userToken.orgName,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }

                            StatusBadge(
                                text = "Almost your turn",
                                backgroundColor = StatusBackgroundGreen,
                                textColor = SuccessGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = DividerColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOKEN",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ActionBlue
                                    )
                                )
                                Text(
                                    text = userToken.tokenNumber,
                                    style = QueueLessTypography.displayMedium.copy(
                                        color = DeepNavy,
                                        fontSize = 36.sp
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "People Ahead: ${userToken.peopleAhead}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Est Wait: ${userToken.estimatedWaitMinutes} min",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = ActionBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Counter: ${userToken.assignedCounter}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        PrimaryButton(
                            text = "Track Queue",
                            onClick = onTrackQueueClick,
                            icon = Icons.Default.Visibility,
                            backgroundColor = ActionBlue
                        )
                    }
                }
            }
        }
    }
}
