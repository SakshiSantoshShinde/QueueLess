package com.example.queueless_smartqueue.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.model.Organization
import com.example.queueless_smartqueue.ui.components.OrganizationCard
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.theme.*

@Composable
fun HomeScreen(
    organizations: List<Organization>,
    onOrganizationSelected: (Organization) -> Unit,
    onNotificationsClick: () -> Unit,
    onSwitchToStaffMode: () -> Unit,
    onDemoStatesClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        CategoryItem("All", "✨"),
        CategoryItem("Hospital", "🏥"),
        CategoryItem("College", "🏫"),
        CategoryItem("Bank", "🏦"),
        CategoryItem("Government", "🏢")
    )

    val filteredOrgs = organizations.filter { org ->
        (selectedCategory == "All" || org.category.contains(selectedCategory, ignoreCase = true)) &&
                (searchQuery.isEmpty() || org.name.contains(searchQuery, ignoreCase = true) || org.category.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top Header bar
        Surface(
            color = CardWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, User 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Where would you like to go?",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ChipBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = ActionBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSwitchToStaffMode,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(StatusBackgroundAmber)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Mode",
                            tint = DeepNavy
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search organization or service...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        focusedBorderColor = ActionBlue,
                        unfocusedBorderColor = CardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick State Demo button banner
            item {
                Surface(
                    color = DeepNavy,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDemoStatesClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "⚡ Preview Special UI States",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = CardWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = "Test Loading, Empty, Error, Paused & Completed views",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CardWhite.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Surface(
                            color = ActionBlue,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Explore",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CardWhite,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Choose Organization Categories
            item {
                Column {
                    Text(
                        text = "Choose Organization",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat.name
                            Surface(
                                color = if (isSelected) ActionBlue else CardWhite,
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) ActionBlue else CardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCategory = cat.name }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(text = cat.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) CardWhite else TextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Nearby / Recent Section
            item {
                Text(
                    text = "Nearby / Recent",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
            }

            // Organization Cards List
            items(filteredOrgs) { org ->
                OrganizationCard(
                    emoji = org.iconEmoji,
                    name = org.name,
                    subtitle = org.address,
                    statusText = if (org.isOpen) "Open • ${org.activeCountersCount} counters active" else "Closed",
                    onViewServicesClick = { onOrganizationSelected(org) }
                )
            }
        }
    }
}

private data class CategoryItem(val name: String, val emoji: String)
