package com.example.queueless_smartqueue.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.queueless_smartqueue.model.CounterInfo
import com.example.queueless_smartqueue.ui.components.CounterCard
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.theme.AppBackground
import com.example.queueless_smartqueue.ui.theme.TextPrimary
import com.example.queueless_smartqueue.ui.theme.TextSecondary

@Composable
fun CounterManagementScreen(
    counters: List<CounterInfo>,
    onToggleCounter: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "Active Counters",
            subtitle = "Manage operational status",
            showBackButton = true,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Counter Status Overview",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Toggling counters automatically updates dynamic ETA predictions for all queued users.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            items(counters) { counter ->
                CounterCard(
                    counterName = counter.name,
                    servingToken = counter.currentlyServingToken,
                    isActive = counter.isActive,
                    onToggleActive = { onToggleCounter(counter.id) }
                )
            }
        }
    }
}
