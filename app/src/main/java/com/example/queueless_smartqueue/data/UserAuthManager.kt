package com.example.queueless_smartqueue.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppUser(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String = "USER", // "USER" or "ADMIN"
    val isVerified: Boolean = true
)

data class NotificationPreferences(
    val pushEnabled: Boolean = true,
    val smsEnabled: Boolean = true,
    val alertBeforeTokens: Int = 3,
    val soundVibrateEnabled: Boolean = true
)

sealed class AuthResult {
    data class Success(val user: AppUser, val isAdmin: Boolean) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Manages user authentication, profile data, registration,
 * and notification preferences for QueueLess with SQLite persistence.
 *
 * Rules:
 * - Admin credentials: admin@gmail.com / admin123 (strictly pre-configured, non-registerable)
 * - Users are persisted in local SQLite database (TABLE_USERS).
 * - Registration is strictly for normal users, not admin.
 */
object UserAuthManager {

    private data class UserCredential(
        var user: AppUser,
        var password: String
    )

    private var dbHelper: QueueDatabaseHelper? = null

    private fun normalizePhone(p: String): String {
        val digits = p.replace(Regex("[^0-9]"), "")
        return if (digits.length >= 10) digits.takeLast(10) else digits
    }

    private fun normalizeEmail(e: String): String = e.trim().lowercase()

    // In-memory fallback and reactive cache
    private val _registeredUsers = mutableListOf(
        UserCredential(
            user = AppUser(
                id = "usr_sakshi",
                name = "Sakshi Shinde",
                email = "user@gmail.com",
                phone = "9860194539",
                role = "USER",
                isVerified = true
            ),
            password = "user123"
        ),
        UserCredential(
            user = AppUser(
                id = "usr_student_1",
                name = "RIT Student 1",
                email = "2560002@ritindia.edu",
                phone = "9860194539",
                role = "USER",
                isVerified = true
            ),
            password = "user123"
        ),
        UserCredential(
            user = AppUser(
                id = "usr_student_2",
                name = "RIT Student 2",
                email = "2560007@ritindia.edu",
                phone = "7020417174",
                role = "USER",
                isVerified = true
            ),
            password = "user123"
        )
    )

    // Fixed Admin Account
    const val ADMIN_EMAIL = "admin@gmail.com"
    const val ADMIN_PASSWORD = "admin123"

    private val adminUser = AppUser(
        id = "admin_master",
        name = "System Administrator",
        email = ADMIN_EMAIL,
        phone = "9860194539",
        role = "ADMIN",
        isVerified = true
    )

    // Currently logged-in user
    private val _currentUser = MutableStateFlow<AppUser>(_registeredUsers.first().user)
    val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    // Notification preferences
    private val _notificationPrefs = MutableStateFlow(NotificationPreferences())
    val notificationPrefs: StateFlow<NotificationPreferences> = _notificationPrefs.asStateFlow()

    /**
     * Initialize UserAuthManager with SQLite Database Helper.
     */
    fun init(context: Context) {
        val helper = QueueDatabaseHelper.getInstance(context)
        dbHelper = helper
        val usersFromDb = helper.getAllUsers()
        if (usersFromDb.isNotEmpty()) {
            _registeredUsers.clear()
            _registeredUsers.addAll(usersFromDb.map { UserCredential(it.first, it.second) })
            _currentUser.value = _registeredUsers.first().user
        }
    }

    /**
     * Authenticate user or admin against SQLite database.
     */
    fun login(identifier: String, passwordInput: String): AuthResult {
        val trimmedIdentifier = identifier.trim()
        val trimmedPassword = passwordInput.trim()

        if (trimmedIdentifier.isEmpty()) {
            return AuthResult.Error("Please enter your email or mobile number.")
        }
        if (trimmedPassword.isEmpty()) {
            return AuthResult.Error("Please enter your password.")
        }

        val normIdent = normalizeEmail(trimmedIdentifier)

        // 1. Check Admin Credentials
        if (normIdent == normalizeEmail(ADMIN_EMAIL)) {
            return if (trimmedPassword == ADMIN_PASSWORD) {
                _currentUser.value = adminUser
                AuthResult.Success(adminUser, isAdmin = true)
            } else {
                AuthResult.Error("Incorrect password for Admin.")
            }
        }

        // 2. Query SQLite Database for User Credential
        val dbUser = dbHelper?.getUserByEmailOrPhone(trimmedIdentifier)
        if (dbUser != null) {
            val (user, password) = dbUser
            if (password != trimmedPassword) {
                return AuthResult.Error("Incorrect password. Please try again.")
            }
            _currentUser.value = user
            return AuthResult.Success(user, isAdmin = false)
        }

        // 3. Fallback to in-memory store
        val cleanPhone = normalizePhone(trimmedIdentifier)
        val matchedCredential = _registeredUsers.find { cred ->
            normalizeEmail(cred.user.email) == normIdent ||
                    (cleanPhone.isNotEmpty() && normalizePhone(cred.user.phone) == cleanPhone)
        }

        if (matchedCredential == null) {
            return AuthResult.Error("User not registered. Please register your account first.")
        }

        if (matchedCredential.password != trimmedPassword) {
            return AuthResult.Error("Incorrect password. Please try again.")
        }

        _currentUser.value = matchedCredential.user
        return AuthResult.Success(matchedCredential.user, isAdmin = false)
    }

    /**
     * Register a new user and persist directly to SQLite database.
     */
    fun registerUser(
        name: String,
        email: String,
        phone: String,
        passwordInput: String
    ): AuthResult {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val trimmedPassword = passwordInput.trim()

        if (trimmedName.length < 2) {
            return AuthResult.Error("Please enter your full name.")
        }
        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            return AuthResult.Error("Please enter a valid email address.")
        }
        val cleanPhone = normalizePhone(trimmedPhone)
        if (cleanPhone.length < 10) {
            return AuthResult.Error("Please enter a valid 10-digit mobile number.")
        }
        if (trimmedPassword.length < 3) {
            return AuthResult.Error("Password must be at least 3 characters.")
        }

        val normEmail = normalizeEmail(trimmedEmail)

        // Prohibit registering admin email
        if (normEmail == normalizeEmail(ADMIN_EMAIL)) {
            return AuthResult.Error("Admin credentials cannot be created via user registration.")
        }

        // Check if user already exists in SQLite
        val existingDbUser = dbHelper?.getUserByEmailOrPhone(trimmedEmail)
        if (existingDbUser != null) {
            val updatedUser = existingDbUser.first.copy(
                name = trimmedName,
                phone = cleanPhone
            )
            dbHelper?.updateUser(updatedUser, trimmedPassword)
            _currentUser.value = updatedUser
            return AuthResult.Success(updatedUser, isAdmin = false)
        }

        // Create and register brand new user in SQLite
        val newUser = AppUser(
            id = "usr_${System.currentTimeMillis()}",
            name = trimmedName,
            email = trimmedEmail,
            phone = cleanPhone,
            role = "USER",
            isVerified = true
        )

        dbHelper?.insertUser(newUser, trimmedPassword)

        _registeredUsers.add(UserCredential(newUser, trimmedPassword))
        _currentUser.value = newUser

        return AuthResult.Success(newUser, isAdmin = false)
    }

    /**
     * Update user profile information in SQLite database.
     */
    fun updateProfile(name: String, phone: String): Boolean {
        val current = _currentUser.value
        val cleanPhone = normalizePhone(phone).ifEmpty { current.phone }
        val updated = current.copy(
            name = name.trim().ifEmpty { current.name },
            phone = cleanPhone
        )
        _currentUser.value = updated

        // Update in SQLite
        dbHelper?.updateUser(updated)

        val index = _registeredUsers.indexOfFirst { it.user.id == current.id }
        if (index != -1) {
            _registeredUsers[index].user = updated
        }
        return true
    }

    /**
     * Update notification preferences
     */
    fun updateNotificationPreferences(
        pushEnabled: Boolean,
        smsEnabled: Boolean,
        alertBeforeTokens: Int,
        soundVibrateEnabled: Boolean
    ) {
        _notificationPrefs.value = NotificationPreferences(
            pushEnabled = pushEnabled,
            smsEnabled = smsEnabled,
            alertBeforeTokens = alertBeforeTokens,
            soundVibrateEnabled = soundVibrateEnabled
        )
    }

    /**
     * Log out current user
     */
    fun logout() {
        _currentUser.value = _registeredUsers.firstOrNull()?.user ?: adminUser
    }
}
