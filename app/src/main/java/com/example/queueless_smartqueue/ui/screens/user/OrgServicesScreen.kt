package com.example.queueless_smartqueue.ui.screens.user

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
import com.example.queueless_smartqueue.model.Organization
import com.example.queueless_smartqueue.model.QueueService
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.components.ServiceCard
import com.example.queueless_smartqueue.ui.theme.AppBackground
import com.example.queueless_smartqueue.ui.theme.TextPrimary
import com.example.queueless_smartqueue.ui.theme.TextSecondary

@Composable
fun OrgServicesScreen(
    organization: Organization?,
    services: List<QueueService>,
    onBackClick: () -> Unit,
    onServiceSelected: (QueueService) -> Unit
) {
    val orgName = organization?.name ?: "College Office"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = orgName,
            subtitle = "Select a service",
            showBackButton = true,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Available Services (${services.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose a service to view queue status and take a digital token.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            items(services) { service ->
                ServiceCard(
                    emoji = service.categoryEmoji,
                    name = service.name,
                    currentQueue = service.peopleWaiting,
                    estimatedWaitMinutes = service.estimatedWaitMinutes,
                    onClick = { onServiceSelected(service) }
                )
            }
        }
    }
}
