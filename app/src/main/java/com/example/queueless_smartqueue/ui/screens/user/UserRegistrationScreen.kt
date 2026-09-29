package com.example.queueless_smartqueue.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.queueless_smartqueue.data.AuthResult
import com.example.queueless_smartqueue.data.UserAuthManager
import com.example.queueless_smartqueue.ui.components.PrimaryButton
import com.example.queueless_smartqueue.ui.theme.*

@Composable
fun UserRegistrationScreen(
    onRegistrationSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Top App Bar
        Surface(
            color = CardWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToLogin) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "New User Registration",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Create an account to book & track queues",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ActionBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AppRegistration,
                            contentDescription = null,
                            tint = ActionBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Citizen & Student Registration",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "User registration only. Admin accounts are pre-configured.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Error Message Banner
            if (errorMessage != null) {
                Surface(
                    color = StatusBackgroundRed,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
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

            // Success Message Banner
            if (successMessage != null) {
                Surface(
                    color = StatusBackgroundGreen,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = successMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    errorMessage = null
                },
                label = { Text("Full Name *") },
                placeholder = { Text("e.g. Sakshi Shinde") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
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

            // Mobile Number Input
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    errorMessage = null
                },
                label = { Text("Mobile Number (10 digits) *") },
                placeholder = { Text("e.g. 9860194539") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

            // Email Address Input
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email Address *") },
                placeholder = { Text("e.g. user@gmail.com") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text("Password *") },
                placeholder = { Text("Minimum 3 characters") },
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

            // Confirm Password Input
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirm Password *") },
                placeholder = { Text("Re-enter password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = TextSecondary
                    )
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

            Spacer(modifier = Modifier.height(4.dp))

            // Submit Registration Button
            PrimaryButton(
                text = "Create User Account",
                onClick = {
                    errorMessage = null
                    if (fullName.trim().length < 2) {
                        errorMessage = "Please enter your full name."
                        return@PrimaryButton
                    }
                    val cleanDigits = phone.replace(Regex("[^0-9]"), "")
                    if (cleanDigits.length < 10) {
                        errorMessage = "Please enter a valid 10-digit mobile number."
                        return@PrimaryButton
                    }
                    if (!email.contains("@") || !email.contains(".")) {
                        errorMessage = "Please enter a valid email address."
                        return@PrimaryButton
                    }
                    if (password.trim() != confirmPassword.trim()) {
                        errorMessage = "Passwords do not match."
                        return@PrimaryButton
                    }
                    if (password.trim().length < 3) {
                        errorMessage = "Password must be at least 3 characters."
                        return@PrimaryButton
                    }

                    val result = UserAuthManager.registerUser(
                        name = fullName,
                        email = email,
                        phone = phone,
                        passwordInput = password
                    )

                    when (result) {
                        is AuthResult.Success -> {
                            successMessage = "Account registered successfully! Redirecting..."
                            onRegistrationSuccess()
                        }
                        is AuthResult.Error -> {
                            errorMessage = result.message
                        }
                    }
                },
                backgroundColor = ActionBlue
            )

            // Back to Login Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already registered? ",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ActionBlue
                    ),
                    modifier = Modifier.clickable { onBackToLogin() }
                )
            }
        }
    }
}
