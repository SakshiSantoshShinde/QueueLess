package com.example.queueless_smartqueue.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.data.AuthResult
import com.example.queueless_smartqueue.data.UserAuthManager
import com.example.queueless_smartqueue.ui.components.PrimaryButton
import com.example.queueless_smartqueue.ui.components.SecondaryButton
import com.example.queueless_smartqueue.ui.theme.*

@Composable
fun LoginScreen(
    onUserLoginSuccess: () -> Unit,
    onAdminLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // QueueLess Logo Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(DeepNavy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = "QueueLess",
                    tint = ActionBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to QueueLess",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Smart queue management for Colleges, Hospitals & Offices",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Error Message Banner
            if (errorMessage != null) {
                Surface(
                    color = StatusBackgroundRed,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = StatusRed,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Identifier Input (Single clean example as requested: "e.g. user@gmail.com")
            OutlinedTextField(
                value = identifier,
                onValueChange = {
                    identifier = it
                    errorMessage = null
                },
                label = { Text("Email or Mobile Number") },
                placeholder = { Text("e.g. user@gmail.com") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
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

            Spacer(modifier = Modifier.height(14.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Button
            PrimaryButton(
                text = "Sign In",
                onClick = {
                    errorMessage = null
                    val result = UserAuthManager.login(identifier, password)
                    when (result) {
                        is AuthResult.Success -> {
                            if (result.isAdmin) {
                                onAdminLoginSuccess()
                            } else {
                                onUserLoginSuccess()
                            }
                        }
                        is AuthResult.Error -> {
                            errorMessage = result.message
                        }
                    }
                },
                backgroundColor = ActionBlue
            )

            Spacer(modifier = Modifier.height(20.dp))

            // New User Registration Link
            SecondaryButton(
                text = "Register as New User",
                onClick = onNavigateToRegister,
                icon = Icons.Default.PersonAdd,
                borderColor = ActionBlue,
                textColor = ActionBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "User registration is strictly for regular users. Admin access requires pre-configured credentials.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
