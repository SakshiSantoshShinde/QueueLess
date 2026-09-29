package com.example.queueless_smartqueue.ui.screens.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.queueless_smartqueue.data.UserAuthManager
import com.example.queueless_smartqueue.ui.components.PrimaryButton
import com.example.queueless_smartqueue.ui.components.QueueLessTopBar
import com.example.queueless_smartqueue.ui.components.SecondaryButton
import com.example.queueless_smartqueue.ui.theme.*
import com.example.queueless_smartqueue.util.SmsHelper

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onWatchVideoGuideClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser by UserAuthManager.currentUser.collectAsState()
    val notificationPrefs by UserAuthManager.notificationPrefs.collectAsState()

    var showPersonalInfoDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val options = listOf(
        ProfileMenuOption(
            title = "Personal Information",
            subtitle = "View & edit your profile details",
            icon = Icons.Outlined.Person,
            onClick = { showPersonalInfoDialog = true }
        ),
        ProfileMenuOption(
            title = "Notification Settings",
            subtitle = "Push, SMS app alerts & reminder triggers",
            icon = Icons.Outlined.Notifications,
            onClick = { showNotificationSettingsDialog = true }
        ),
        ProfileMenuOption(
            title = "App Video Guide",
            subtitle = "Watch 1-minute video on how QueueLess works",
            icon = Icons.Outlined.OndemandVideo,
            onClick = onWatchVideoGuideClick
        ),
        ProfileMenuOption(
            title = "Help & Support",
            subtitle = "Call, email our team & view FAQs",
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            onClick = { showHelpSupportDialog = true }
        ),
        ProfileMenuOption(
            title = "About QueueLess",
            subtitle = "How the app works & step-by-step guide",
            icon = Icons.Outlined.Info,
            onClick = { showAboutDialog = true }
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        QueueLessTopBar(
            title = "Profile & Settings",
            subtitle = "Manage account, notifications & support"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 96.dp)
        ) {
            // User Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Initials Circle
                        val initials = currentUser.name
                            .split(" ")
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .take(2)
                            .joinToString("")
                            .ifEmpty { "U" }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(DeepNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = CardWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = currentUser.email,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Text(
                                text = "📱 ${currentUser.phone}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = ChipBackground,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Verified Citizen / Student",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ActionBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Profile Settings List
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        options.forEachIndexed { index, option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { option.onClick() }
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ChipBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = option.icon,
                                            contentDescription = null,
                                            tint = ActionBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = option.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = option.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (index < options.size - 1) {
                                HorizontalDivider(
                                    color = DividerColor,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Log Out Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryButton(
                    text = "Log Out",
                    onClick = {
                        UserAuthManager.logout()
                        onLogoutClick()
                    },
                    borderColor = StatusRed,
                    textColor = StatusRed,
                    icon = Icons.AutoMirrored.Filled.Logout
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Logging out returns to the login screen where users or staff/admin can sign in.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // --- 1. PERSONAL INFORMATION DIALOG ---
    if (showPersonalInfoDialog) {
        var editName by remember { mutableStateOf(currentUser.name) }
        var editPhone by remember { mutableStateOf(currentUser.phone) }

        Dialog(onDismissRequest = { showPersonalInfoDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Personal Information",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        IconButton(onClick = { showPersonalInfoDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentUser.email,
                        onValueChange = {},
                        label = { Text("Email Address (Registered)") },
                        enabled = false,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentUser.id,
                        onValueChange = {},
                        label = { Text("User Identification ID") },
                        enabled = false,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    PrimaryButton(
                        text = "Save Profile Changes",
                        onClick = {
                            UserAuthManager.updateProfile(editName, editPhone)
                            Toast.makeText(context, "Profile details updated!", Toast.LENGTH_SHORT).show()
                            showPersonalInfoDialog = false
                        },
                        backgroundColor = ActionBlue
                    )
                }
            }
        }
    }

    // --- 2. NOTIFICATION SETTINGS DIALOG ---
    if (showNotificationSettingsDialog) {
        var pushEnabled by remember { mutableStateOf(notificationPrefs.pushEnabled) }
        var smsEnabled by remember { mutableStateOf(notificationPrefs.smsEnabled) }
        var soundEnabled by remember { mutableStateOf(notificationPrefs.soundVibrateEnabled) }
        var alertBeforeTokens by remember { mutableIntStateOf(notificationPrefs.alertBeforeTokens) }

        Dialog(onDismissRequest = { showNotificationSettingsDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notification Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        IconButton(onClick = { showNotificationSettingsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Push notifications toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Push Notifications", fontWeight = FontWeight.SemiBold)
                            Text(text = "Receive in-app popups & status changes", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = pushEnabled,
                            onCheckedChange = { pushEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ActionBlue)
                        )
                    }

                    HorizontalDivider(color = DividerColor)

                    // SMS notifications toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "SMS App Notifications", fontWeight = FontWeight.SemiBold)
                            Text(text = "Launch SMS app for queue alerts & arrival reminders", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = smsEnabled,
                            onCheckedChange = { smsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ActionBlue)
                        )
                    }

                    HorizontalDivider(color = DividerColor)

                    // Approaching queue threshold
                    Column {
                        Text(text = "Alert Threshold", fontWeight = FontWeight.SemiBold)
                        Text(text = "Notify me when queue reaches:", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2, 3, 5).forEach { tokensAhead ->
                                val selected = alertBeforeTokens == tokensAhead
                                Surface(
                                    color = if (selected) ActionBlue else ChipBackground,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable { alertBeforeTokens = tokensAhead }
                                ) {
                                    Text(
                                        text = "$tokensAhead tokens ahead",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (selected) CardWhite else TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = DividerColor)

                    // Sound & Vibration toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Sound & Vibration", fontWeight = FontWeight.SemiBold)
                            Text(text = "Play audible alert when your turn is called", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ActionBlue)
                        )
                    }

                    // Test SMS button
                    SecondaryButton(
                        text = "📲 Test Native SMS Notification",
                        onClick = {
                            val testMessage = "QueueLess Alert: Your Token A47 for Bonafide Certificate is approaching Counter 2! Please proceed now to avoid queue expiry."
                            SmsHelper.openSmsApp(context, currentUser.phone, testMessage)
                        },
                        borderColor = ActionBlue,
                        textColor = ActionBlue
                    )

                    PrimaryButton(
                        text = "Save Preferences",
                        onClick = {
                            UserAuthManager.updateNotificationPreferences(
                                pushEnabled = pushEnabled,
                                smsEnabled = smsEnabled,
                                alertBeforeTokens = alertBeforeTokens,
                                soundVibrateEnabled = soundEnabled
                            )
                            Toast.makeText(context, "Notification preferences saved!", Toast.LENGTH_SHORT).show()
                            showNotificationSettingsDialog = false
                        },
                        backgroundColor = ActionBlue
                    )
                }
            }
        }
    }

    // --- 3. HELP & SUPPORT DIALOG ---
    if (showHelpSupportDialog) {
        Dialog(onDismissRequest = { showHelpSupportDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Help & Support",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            IconButton(onClick = { showHelpSupportDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                        Text(
                            text = "Need help or have questions? Contact our dedicated QueueLess support team directly:",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    // Phone Support Section
                    item {
                        Text(
                            text = "📞 Phone Support",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Phone 1: 9860194539
                        ContactActionRow(
                            label = "Primary Support:",
                            value = "9860194539",
                            onCall = { SmsHelper.dialPhoneNumber(context, "9860194539") },
                            onCopy = {
                                copyToClipboard(context, "9860194539", "Phone number copied!")
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Phone 2: 7020417174
                        ContactActionRow(
                            label = "Alternate Support:",
                            value = "7020417174",
                            onCall = { SmsHelper.dialPhoneNumber(context, "7020417174") },
                            onCopy = {
                                copyToClipboard(context, "7020417174", "Phone number copied!")
                            }
                        )
                    }

                    // Email Support Section
                    item {
                        Text(
                            text = "✉️ Email Support",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Email 1: 2560002@ritindia.edu
                        ContactActionRow(
                            label = "Student Support 1:",
                            value = "2560002@ritindia.edu",
                            onCall = { SmsHelper.sendEmail(context, "2560002@ritindia.edu", "QueueLess App Support Request") },
                            onCopy = {
                                copyToClipboard(context, "2560002@ritindia.edu", "Email copied to clipboard!")
                            },
                            callButtonText = "Email"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Email 2: 2560007@ritindia.edu
                        ContactActionRow(
                            label = "Student Support 2:",
                            value = "2560007@ritindia.edu",
                            onCall = { SmsHelper.sendEmail(context, "2560007@ritindia.edu", "QueueLess App Support Request") },
                            onCopy = {
                                copyToClipboard(context, "2560007@ritindia.edu", "Email copied to clipboard!")
                            },
                            callButtonText = "Email"
                        )
                    }

                    // Timings & FAQs
                    item {
                        Surface(
                            color = ChipBackground,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🕒 Working Hours: Mon - Sat (9:00 AM - 6:00 PM)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Campus Location: Rajarambapu Institute of Technology (RIT), Islampur",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                    }

                    item {
                        PrimaryButton(
                            text = "Close Support",
                            onClick = { showHelpSupportDialog = false },
                            backgroundColor = ActionBlue
                        )
                    }
                }
            }
        }
    }

    // --- 4. ABOUT QUEUELESS DIALOG ---
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "About QueueLess",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            IconButton(onClick = { showAboutDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }

                    // What is QueueLess
                    item {
                        Surface(
                            color = ChipBackground,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🚀 What is QueueLess?",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ActionBlue
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "QueueLess is a next-generation Smart Virtual Queueing and crowd management system designed to eliminate long physical waiting lines in colleges, hospitals, banks, and government offices.\n\nInstead of standing in stressful queues for hours, QueueLess lets citizens and students take virtual tokens remotely, track live counter progress, and arrive right on time.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimary,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }

                    // How to Operate the App Guide
                    item {
                        Text(
                            text = "📖 How to Operate the App (User Guide)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val steps = listOf(
                            "1. Select Organization" to "Choose your institution (e.g. RIT College Office, City Hospital, Apex Bank).",
                            "2. Choose Service" to "Pick the exact service you need (e.g. Bonafide, Exam Fees, OPD, Cash Deposit).",
                            "3. Take Virtual Token" to "Click 'Take Token' to receive your instant digital token with estimated wait time & recommended arrival time.",
                            "4. Real-Time Tracking" to "Watch live counter movements, people ahead, and current serving tokens in the 'Live Queue' screen.",
                            "5. Smart Alerts & SMS" to "Receive native notifications and SMS alerts when your turn is approaching so you never miss your slot.",
                            "6. Reach the Counter" to "Walk directly to your assigned counter when called without having spent time standing in a line!"
                        )

                        steps.forEach { (stepTitle, stepDesc) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = stepTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                )
                                Text(
                                    text = stepDesc,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }

                    // Architecture & Features
                    item {
                        Surface(
                            color = CardWhite,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "✨ Key Technologies & Features",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Broadcast Offline Fallback: Pages and tokens load automatically even when network is disconnected.\n• Native SMS App Integration: Queue tokens and alerts sent directly through your phone's SMS app.\n• Dynamic Multi-Counter Balancing: Auto-recalculates estimated wait time based on counter status.\n• Built by RIT B.Tech Engineering Team.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                    }

                    item {
                        PrimaryButton(
                            text = "Got It!",
                            onClick = { showAboutDialog = false },
                            backgroundColor = ActionBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactActionRow(
    label: String,
    value: String,
    onCall: () -> Unit,
    onCopy: () -> Unit,
    callButtonText: String = "Call"
) {
    Surface(
        color = CardWhite,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = ActionBlue,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onCall() }
                ) {
                    Text(
                        text = callButtonText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CardWhite,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = ChipBackground,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onCopy() }
                ) {
                    Text(
                        text = "Copy",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ActionBlue,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, toastMsg: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("QueueLess Contact", text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
}

private data class ProfileMenuOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
