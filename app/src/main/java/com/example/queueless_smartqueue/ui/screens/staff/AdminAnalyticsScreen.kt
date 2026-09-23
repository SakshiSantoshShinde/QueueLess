package com.example.queueless_smartqueue.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.components.StatisticsCard
import com.example.queueless_smartqueue.ui.theme.*

@Composable
fun AdminAnalyticsScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "Analytics",
            subtitle = "Today's queue performance metrics",
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
            // Metrics Overview
            item {
                Text(
                    text = "Key Performance Metrics",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatisticsCard(
                            title = "Tokens Served",
                            value = "198",
                            icon = Icons.Default.BarChart,
                            iconColor = SuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatisticsCard(
                            title = "Avg Wait Time",
                            value = "18 min",
                            icon = Icons.Default.Schedule,
                            iconColor = ActionBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatisticsCard(
                            title = "Avg Service Time",
                            value = "4.2 min",
                            icon = Icons.Default.Speed,
                            iconColor = DeepNavy,
                            modifier = Modifier.weight(1f)
                        )
                        StatisticsCard(
                            title = "Peak Hour",
                            value = "11:00 AM",
                            icon = Icons.Default.HourglassTop,
                            iconColor = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Visual Charts Section
            item {
                Text(
                    text = "Visual Queue Analytics",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            // Chart 1: Queue Length Over Time
            item {
                SimpleBarChartCard(
                    title = "Queue Length Over Time",
                    subtitle = "Peak traffic recorded between 10 AM – 12 PM",
                    data = listOf("9 AM" to 12, "10 AM" to 28, "11 AM" to 42, "12 PM" to 35, "1 PM" to 18, "2 PM" to 22)
                )
            }

            // Chart 2: Tokens Served Per Hour
            item {
                SimpleBarChartCard(
                    title = "Tokens Served Per Hour",
                    subtitle = "Average output: 35 tokens/hr across active counters",
                    data = listOf("9 AM" to 20, "10 AM" to 38, "11 AM" to 45, "12 PM" to 40, "1 PM" to 25, "2 PM" to 30),
                    barColor = SuccessGreen
                )
            }

            // Counter Efficiency Summary
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Counter Efficiency Rate",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        CounterEfficiencyProgress("Counter 1", 0.92f, "92% Efficiency")
                        Spacer(modifier = Modifier.height(8.dp))
                        CounterEfficiencyProgress("Counter 2", 0.88f, "88% Efficiency")
                        Spacer(modifier = Modifier.height(8.dp))
                        CounterEfficiencyProgress("Counter 3", 0.45f, "45% Efficiency (Offline 2h)")
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleBarChartCard(
    title: String,
    subtitle: String,
    data: List<Pair<String, Int>>,
    barColor: Color = ActionBlue
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(20.dp))

            val maxVal = data.maxOf { it.second }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (label, value) ->
                    val heightFraction = (value.toFloat() / maxVal.toFloat()).coerceIn(0.1f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$value",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(heightFraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(barColor)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterEfficiencyProgress(name: String, progress: Float, label: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(label, style = MaterialTheme.typography.bodySmall.copy(color = ActionBlue, fontWeight = FontWeight.Bold))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = ActionBlue,
            trackColor = ChipBackground
        )
    }
}
