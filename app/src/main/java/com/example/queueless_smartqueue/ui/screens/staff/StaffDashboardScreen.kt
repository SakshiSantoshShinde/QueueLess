package com.example.queueless_smartqueue.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.model.StaffStats
import com.example.queueless_smartqueue.ui.components.PrimaryButton
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.components.StatisticsCard
import com.example.queueless_smartqueue.ui.theme.*

@Composable
fun StaffDashboardScreen(
    stats: StaffStats,
    onNavigateToQueueManagement: () -> Unit,
    onNavigateToCounters: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToRegisterOrg: () -> Unit,
    onBackToUserMode: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "Good morning, Staff",
            subtitle = "College Office • Admin Console",
            showBackButton = true,
            onBackClick = onBackToUserMode
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Today Summary Header
            item {
                Text(
                    text = "Today's Queue Summary",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            // Metric Cards Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatisticsCard(
                            title = "Total Tokens",
                            value = "${stats.totalTokens}",
                            icon = Icons.Outlined.ConfirmationNumber,
                            iconColor = ActionBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatisticsCard(
                            title = "Completed",
                            value = "${stats.completed}",
                            icon = Icons.Outlined.CheckCircle,
                            iconColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatisticsCard(
                            title = "Waiting Queue",
                            value = "${stats.waiting}",
                            icon = Icons.Outlined.HourglassEmpty,
                            iconColor = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatisticsCard(
                            title = "Average Wait",
                            value = "${stats.avgWaitMinutes} min",
                            icon = Icons.Outlined.Schedule,
                            iconColor = ActionBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Actions Section
            item {
                Text(
                    text = "Quick Staff Modules",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            // Action Cards
            item {
                StaffActionCard(
                    title = "Queue Management",
                    subtitle = "Call next token, skip, or pause current queue",
                    icon = Icons.Default.ConfirmationNumber,
                    accentColor = ActionBlue,
                    onClick = onNavigateToQueueManagement
                )
            }

            item {
                StaffActionCard(
                    title = "Counter Management",
                    subtitle = "Manage 3 counters, view active tokens & statuses",
                    icon = Icons.Default.DesktopWindows,
                    accentColor = DeepNavy,
                    onClick = onNavigateToCounters
                )
            }

            item {
                StaffActionCard(
                    title = "Admin Analytics",
                    subtitle = "View queue trends, peak hours & counter efficiency",
                    icon = Icons.Default.BarChart,
                    accentColor = SuccessGreen,
                    onClick = onNavigateToAnalytics
                )
            }

            item {
                StaffActionCard(
                    title = "Register New Organization",
                    subtitle = "Add organization, configure services & save to database",
                    icon = Icons.Default.Add,
                    accentColor = ActionBlue,
                    onClick = onNavigateToRegisterOrg
                )
            }
        }
    }
}


@Composable
private fun StaffActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
