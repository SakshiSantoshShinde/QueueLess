package com.example.queueless_smartqueue.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.network.RegisterOrgRequest
import com.example.queueless_smartqueue.network.RegisterServiceRequest
import com.example.queueless_smartqueue.ui.components.PrimaryButton
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.theme.*

class ServiceFormState(
    initialName: String = "",
    initialCategoryEmoji: String = "📄",
    initialTokenPrefix: String = "A",
    initialAvgServiceTimeMinutes: String = "2.5"
) {
    var name by mutableStateOf(initialName)
    var categoryEmoji by mutableStateOf(initialCategoryEmoji)
    var tokenPrefix by mutableStateOf(initialTokenPrefix)
    var avgServiceTimeMinutes by mutableStateOf(initialAvgServiceTimeMinutes)
}

@Composable
fun RegisterOrganizationScreen(
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onRegisterSubmit: (RegisterOrgRequest) -> Unit
) {
    var orgName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Hospital") }
    var selectedEmoji by remember { mutableStateOf("🏥") }
    var address by remember { mutableStateOf("") }
    var activeCountersCount by remember { mutableStateOf("2") }

    val categories = listOf(
        Pair("Hospital", "🏥"),
        Pair("College", "🏫"),
        Pair("Bank", "🏦"),
        Pair("Government", "🏢"),
        Pair("Clinic", "🩺"),
        Pair("Other", "🏛️")
    )

    val emojiOptions = listOf("🏥", "🏫", "🏦", "🏢", "🩺", "🎓", "📜", "💼", "🏛️")

    val services = remember {
        mutableStateListOf(
            ServiceFormState("General Registration", "📄", "A", "2.5")
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "Register Organization",
            subtitle = "Save new organization & services to database",
            showBackButton = true,
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Error banner if any
            if (errorMessage != null) {
                item {
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            // Section 1: Basic Organization Info
            item {
                Text(
                    text = "Organization Information",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = orgName,
                            onValueChange = {
                                orgName = it
                                errorMessage = null
                            },
                            label = { Text("Organization Name *") },
                            placeholder = { Text("e.g. Apollo Hospital, MIT Campus") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Category Selection
                        Column {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(categories) { (cat, emoji) ->
                                    val isSelected = selectedCategory == cat
                                    Surface(
                                        color = if (isSelected) ActionBlue else ChipBackground,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.clickable {
                                            selectedCategory = cat
                                            selectedEmoji = emoji
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(emoji, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                cat,
                                                color = if (isSelected) CardWhite else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Icon Emoji Selector
                        Column {
                            Text(
                                text = "Icon Emoji",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(emojiOptions) { emoji ->
                                    val isSelected = selectedEmoji == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) ActionBlue.copy(alpha = 0.2f) else ChipBackground)
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) ActionBlue else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedEmoji = emoji },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address / Location") },
                            placeholder = { Text("e.g. Main Gate, Sector 4, Pune") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = activeCountersCount,
                            onValueChange = { activeCountersCount = it.filter { char -> char.isDigit() } },
                            label = { Text("Initial Active Counters (e.g. 2)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Section 2: Services
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Organization Services (${services.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Add services that visitors can take tokens for",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            val nextPrefix = String.format("%c", 'A' + (services.size % 26))
                            services.add(ServiceFormState("", "📄", nextPrefix, "2.5"))
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Service", fontSize = 13.sp)
                    }
                }
            }

            // Service Cards List
            itemsIndexed(services) { index, srv ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Service #${index + 1}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ActionBlue
                                )
                            )

                            if (services.size > 1) {
                                IconButton(
                                    onClick = { services.removeAt(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove Service",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = srv.name,
                            onValueChange = { srv.name = it },
                            label = { Text("Service Name *") },
                            placeholder = { Text("e.g. OPD Consultation, Certificate") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = srv.tokenPrefix,
                                onValueChange = { srv.tokenPrefix = it.take(2).uppercase() },
                                label = { Text("Token Prefix") },
                                placeholder = { Text("A") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = srv.categoryEmoji,
                                onValueChange = { srv.categoryEmoji = it },
                                label = { Text("Emoji") },
                                placeholder = { Text("📄") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = srv.avgServiceTimeMinutes,
                                onValueChange = { srv.avgServiceTimeMinutes = it },
                                label = { Text("Avg Mins") },
                                placeholder = { Text("2.5") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Submit Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                PrimaryButton(
                    text = if (isLoading) "Registering in Database..." else "Register Organization & Services",
                    enabled = !isLoading,
                    onClick = {
                        if (orgName.trim().isEmpty()) {
                            errorMessage = "Please enter an Organization Name."
                            return@PrimaryButton
                        }

                        val validServices = services.filter { it.name.trim().isNotEmpty() }
                        if (validServices.isEmpty()) {
                            errorMessage = "Please add at least one service with a name."
                            return@PrimaryButton
                        }

                        val serviceRequests = validServices.map { s ->
                            RegisterServiceRequest(
                                name = s.name.trim(),
                                categoryEmoji = s.categoryEmoji.ifBlank { "📄" },
                                tokenPrefix = s.tokenPrefix.ifBlank { "A" },
                                avgServiceTimeMinutes = s.avgServiceTimeMinutes.toDoubleOrNull() ?: 2.5
                            )
                        }

                        val request = RegisterOrgRequest(
                            name = orgName.trim(),
                            category = selectedCategory,
                            iconEmoji = selectedEmoji,
                            address = address.trim(),
                            activeCountersCount = activeCountersCount.toIntOrNull() ?: 2,
                            isOpen = true,
                            services = serviceRequests
                        )

                        onRegisterSubmit(request)
                    }
                )
            }
        }
    }
}
