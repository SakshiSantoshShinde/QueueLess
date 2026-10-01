package com.example.queueless_smartqueue.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.queueless_smartqueue.model.*

/**
 * SQLite Database Helper for QueueLess Smart Queue Management.
 * Manages full local SQLite database storage for:
 * - Users & Authentication credentials
 * - Organizations
 * - Queue Services
 * - Counters
 * - Active Tokens
 * - Queue History
 * - Notifications
 */
class QueueDatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "queueless_smartqueue.db"
        const val DATABASE_VERSION = 2

        @Volatile
        private var instance: QueueDatabaseHelper? = null

        fun getInstance(context: Context): QueueDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: QueueDatabaseHelper(context.applicationContext).also { instance = it }
            }
        }

        // Table Names
        const val TABLE_USERS = "users"
        const val TABLE_ORGANIZATIONS = "organizations"
        const val TABLE_SERVICES = "services"
        const val TABLE_COUNTERS = "counters"
        const val TABLE_TOKENS = "tokens"
        const val TABLE_HISTORY = "history"
        const val TABLE_NOTIFICATIONS = "notifications"

        // Users Columns
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PHONE = "phone"
        const val COL_USER_PASSWORD = "password"
        const val COL_USER_ROLE = "role"
        const val COL_USER_IS_VERIFIED = "is_verified"

        // Organizations Columns
        const val COL_ORG_ID = "id"
        const val COL_ORG_NAME = "name"
        const val COL_ORG_CATEGORY = "category"
        const val COL_ORG_ICON_EMOJI = "icon_emoji"
        const val COL_ORG_ADDRESS = "address"
        const val COL_ORG_ACTIVE_COUNTERS = "active_counters"
        const val COL_ORG_IS_OPEN = "is_open"

        // Services Columns
        const val COL_SERV_ID = "id"
        const val COL_SERV_ORG_ID = "org_id"
        const val COL_SERV_NAME = "name"
        const val COL_SERV_CURRENT_TOKEN = "current_serving_token"
        const val COL_SERV_WAITING = "people_waiting"
        const val COL_SERV_EST_WAIT = "estimated_wait_minutes"
        const val COL_SERV_COUNTERS = "active_counters"
        const val COL_SERV_EMOJI = "category_emoji"

        // Counters Columns
        const val COL_COUNTER_ID = "id"
        const val COL_COUNTER_NAME = "name"
        const val COL_COUNTER_SERVING_TOKEN = "currently_serving_token"
        const val COL_COUNTER_IS_ACTIVE = "is_active"

        // Tokens Columns
        const val COLUMN_TOKEN_NUMBER = "token_number"
        const val COLUMN_SERVICE_ID = "service_id"
        const val COLUMN_SERVICE_NAME = "service_name"
        const val COLUMN_ORG_NAME = "org_name"
        const val COLUMN_CURRENTLY_SERVING = "currently_serving"
        const val COLUMN_PEOPLE_AHEAD = "people_ahead"
        const val COLUMN_EST_WAIT_MINS = "est_wait_mins"
        const val COLUMN_ASSIGNED_COUNTER = "assigned_counter"
        const val COLUMN_RECOMMENDED_ARRIVAL = "recommended_arrival"
        const val COLUMN_STATUS = "status"
        const val COLUMN_PROGRESS_STEPS = "progress_steps"
        const val COLUMN_ETA_REASON = "eta_reason"

        // History Columns
        const val COLUMN_HISTORY_ID = "id"
        const val COLUMN_DATE_TEXT = "date_text"

        // Notifications Columns
        const val COL_NOTIF_ID = "id"
        const val COL_NOTIF_TYPE = "type"
        const val COL_NOTIF_TITLE = "title"
        const val COL_NOTIF_MESSAGE = "message"
        const val COL_NOTIF_TIME_AGO = "time_ago"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Create Users Table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID TEXT PRIMARY KEY,
                $COL_USER_NAME TEXT NOT NULL,
                $COL_USER_EMAIL TEXT NOT NULL UNIQUE,
                $COL_USER_PHONE TEXT NOT NULL,
                $COL_USER_PASSWORD TEXT NOT NULL,
                $COL_USER_ROLE TEXT NOT NULL DEFAULT 'USER',
                $COL_USER_IS_VERIFIED INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        // 2. Create Organizations Table
        db.execSQL("""
            CREATE TABLE $TABLE_ORGANIZATIONS (
                $COL_ORG_ID TEXT PRIMARY KEY,
                $COL_ORG_NAME TEXT NOT NULL,
                $COL_ORG_CATEGORY TEXT NOT NULL,
                $COL_ORG_ICON_EMOJI TEXT NOT NULL,
                $COL_ORG_ADDRESS TEXT NOT NULL,
                $COL_ORG_ACTIVE_COUNTERS INTEGER NOT NULL DEFAULT 1,
                $COL_ORG_IS_OPEN INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        // 3. Create Services Table
        db.execSQL("""
            CREATE TABLE $TABLE_SERVICES (
                $COL_SERV_ID TEXT PRIMARY KEY,
                $COL_SERV_ORG_ID TEXT NOT NULL,
                $COL_SERV_NAME TEXT NOT NULL,
                $COL_SERV_CURRENT_TOKEN TEXT NOT NULL,
                $COL_SERV_WAITING INTEGER NOT NULL DEFAULT 0,
                $COL_SERV_EST_WAIT INTEGER NOT NULL DEFAULT 10,
                $COL_SERV_COUNTERS INTEGER NOT NULL DEFAULT 1,
                $COL_SERV_EMOJI TEXT NOT NULL DEFAULT '📄'
            )
        """.trimIndent())

        // 4. Create Counters Table
        db.execSQL("""
            CREATE TABLE $TABLE_COUNTERS (
                $COL_COUNTER_ID INTEGER PRIMARY KEY,
                $COL_COUNTER_NAME TEXT NOT NULL,
                $COL_COUNTER_SERVING_TOKEN TEXT,
                $COL_COUNTER_IS_ACTIVE INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())

        // 5. Create Tokens Table
        db.execSQL("""
            CREATE TABLE $TABLE_TOKENS (
                $COLUMN_TOKEN_NUMBER TEXT PRIMARY KEY,
                $COLUMN_SERVICE_ID TEXT,
                $COLUMN_SERVICE_NAME TEXT,
                $COLUMN_ORG_NAME TEXT,
                $COLUMN_CURRENTLY_SERVING TEXT,
                $COLUMN_PEOPLE_AHEAD INTEGER,
                $COLUMN_EST_WAIT_MINS INTEGER,
                $COLUMN_ASSIGNED_COUNTER TEXT,
                $COLUMN_RECOMMENDED_ARRIVAL TEXT,
                $COLUMN_STATUS TEXT,
                $COLUMN_PROGRESS_STEPS TEXT,
                $COLUMN_ETA_REASON TEXT
            )
        """.trimIndent())

        // 6. Create History Table
        db.execSQL("""
            CREATE TABLE $TABLE_HISTORY (
                $COLUMN_HISTORY_ID TEXT PRIMARY KEY,
                $COLUMN_SERVICE_NAME TEXT,
                $COLUMN_ORG_NAME TEXT,
                $COLUMN_DATE_TEXT TEXT,
                $COLUMN_TOKEN_NUMBER TEXT,
                $COLUMN_STATUS TEXT
            )
        """.trimIndent())

        // 7. Create Notifications Table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTIFICATIONS (
                $COL_NOTIF_ID TEXT PRIMARY KEY,
                $COL_NOTIF_TYPE TEXT,
                $COL_NOTIF_TITLE TEXT,
                $COL_NOTIF_MESSAGE TEXT,
                $COL_NOTIF_TIME_AGO TEXT
            )
        """.trimIndent())

        // Seed initial data
        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORGANIZATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SERVICES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COUNTERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TOKENS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        // Seed default users
        val users = listOf(
            Triple(AppUser("usr_sakshi", "Sakshi Shinde", "user@gmail.com", "9860194539", "USER", true), "user123", Unit),
            Triple(AppUser("usr_student_1", "RIT Student 1", "2560002@ritindia.edu", "9860194539", "USER", true), "user123", Unit),
            Triple(AppUser("usr_student_2", "RIT Student 2", "2560007@ritindia.edu", "7020417174", "USER", true), "user123", Unit)
        )
        for ((u, pwd, _) in users) {
            val cv = ContentValues().apply {
                put(COL_USER_ID, u.id)
                put(COL_USER_NAME, u.name)
                put(COL_USER_EMAIL, u.email.lowercase())
                put(COL_USER_PHONE, u.phone)
                put(COL_USER_PASSWORD, pwd)
                put(COL_USER_ROLE, u.role)
                put(COL_USER_IS_VERIFIED, if (u.isVerified) 1 else 0)
            }
            db.insert(TABLE_USERS, null, cv)
        }

        // Seed default organizations
        val orgs = listOf(
            Organization("org_1", "RIT College Office", "College", "🏫", "Administrative Services • Main Campus", 3, true),
            Organization("org_2", "City Central Hospital", "Hospital", "🏥", "OPD & Registration Section • Building B", 4, true),
            Organization("org_3", "National Apex Bank", "Bank", "🏦", "Customer Service & Forex • Central Branch", 3, true),
            Organization("org_4", "Municipal Regional Office", "Government", "🏢", "Citizens Desk & Permits • City Center", 3, true)
        )
        for (org in orgs) {
            val cv = ContentValues().apply {
                put(COL_ORG_ID, org.id)
                put(COL_ORG_NAME, org.name)
                put(COL_ORG_CATEGORY, org.category)
                put(COL_ORG_ICON_EMOJI, org.iconEmoji)
                put(COL_ORG_ADDRESS, org.address)
                put(COL_ORG_ACTIVE_COUNTERS, org.activeCountersCount)
                put(COL_ORG_IS_OPEN, if (org.isOpen) 1 else 0)
            }
            db.insert(TABLE_ORGANIZATIONS, null, cv)
        }

        // Seed default services
        val services = listOf(
            QueueService("serv_1", "org_1", "Bonafide Certificate", "A36", 14, 35, 2, "📜"),
            QueueService("serv_2", "org_1", "Scholarship Section", "B18", 8, 18, 2, "🎓"),
            QueueService("serv_3", "org_1", "Exam & Transcript", "C05", 15, 30, 3, "📝"),
            QueueService("serv_4", "org_1", "Fees & Finance Dept", "F12", 21, 42, 1, "💰"),
            QueueService("serv_admit", "org_1", "Admission & Verification", "D08", 6, 15, 2, "📂"),
            QueueService("serv_5", "org_2", "General OPD Consultation", "H14", 12, 36, 3, "🩺"),
            QueueService("serv_6", "org_2", "Laboratory & Blood Test", "L09", 6, 15, 2, "🧪"),
            QueueService("serv_rad", "org_2", "Radiology & X-Ray", "R04", 5, 25, 2, "🩻"),
            QueueService("serv_pharm", "org_2", "Pharmacy Dispensing", "P25", 9, 12, 3, "💊"),
            QueueService("serv_7", "org_3", "Cash Deposit & Withdrawal", "D15", 7, 14, 2, "💵"),
            QueueService("serv_8", "org_3", "Account Opening & KYC", "N04", 5, 20, 1, "💳"),
            QueueService("serv_loan", "org_3", "Loans & Mortgages Desk", "M06", 4, 24, 1, "🏦"),
            QueueService("serv_forex", "org_3", "Forex & International Wire", "X02", 2, 10, 1, "🌐"),
            QueueService("serv_9", "org_4", "Property Tax & Assessment", "T22", 10, 25, 2, "🏠"),
            QueueService("serv_10", "org_4", "Birth & Death Certificates", "C11", 8, 16, 2, "📜"),
            QueueService("serv_11", "org_4", "Trade License & Permits", "P05", 4, 12, 1, "📑"),
            QueueService("serv_water", "org_4", "Water & Utilities Desk", "W07", 6, 18, 2, "💧")
        )
        for (s in services) {
            val cv = ContentValues().apply {
                put(COL_SERV_ID, s.id)
                put(COL_SERV_ORG_ID, s.orgId)
                put(COL_SERV_NAME, s.name)
                put(COL_SERV_CURRENT_TOKEN, s.currentServingToken)
                put(COL_SERV_WAITING, s.peopleWaiting)
                put(COL_SERV_EST_WAIT, s.estimatedWaitMinutes)
                put(COL_SERV_COUNTERS, s.activeCounters)
                put(COL_SERV_EMOJI, s.categoryEmoji)
            }
            db.insert(TABLE_SERVICES, null, cv)
        }

        // Seed default counters
        val counters = listOf(
            CounterInfo(1, "Counter 1", "A31", true),
            CounterInfo(2, "Counter 2", "A36", true),
            CounterInfo(3, "Counter 3", null, false)
        )
        for (c in counters) {
            val cv = ContentValues().apply {
                put(COL_COUNTER_ID, c.id)
                put(COL_COUNTER_NAME, c.name)
                put(COL_COUNTER_SERVING_TOKEN, c.currentlyServingToken)
                put(COL_COUNTER_IS_ACTIVE, if (c.isActive) 1 else 0)
            }
            db.insert(TABLE_COUNTERS, null, cv)
        }

        // Seed default history
        val historyList = listOf(
            QueueHistoryItem("h1", "Bonafide Certificate", "RIT College Office", "Today • A47", "A47", "Completed"),
            QueueHistoryItem("h2", "Scholarship Department", "RIT College Office", "Yesterday • B23", "B23", "Completed"),
            QueueHistoryItem("h3", "General OPD Consultation", "City Central Hospital", "15 Aug • H12", "H12", "Completed"),
            QueueHistoryItem("h4", "Cash Deposit & Withdrawal", "National Apex Bank", "10 Aug • D05", "D05", "Completed")
        )
        for (h in historyList) {
            val cv = ContentValues().apply {
                put(COLUMN_HISTORY_ID, h.id)
                put(COLUMN_SERVICE_NAME, h.serviceName)
                put(COLUMN_ORG_NAME, h.orgName)
                put(COLUMN_DATE_TEXT, h.dateText)
                put(COLUMN_TOKEN_NUMBER, h.tokenNumber)
                put(COLUMN_STATUS, h.status)
            }
            db.insert(TABLE_HISTORY, null, cv)
        }

        // Seed default notifications
        val notifs = listOf(
            NotificationItem("n1", NotificationType.APPROACHING, "Your turn is approaching", "Only 3 people are ahead of you in the queue.", "2m ago"),
            NotificationItem("n2", NotificationType.UPDATED, "Queue updated", "Your estimated waiting time is now 25 minutes.", "10m ago"),
            NotificationItem("n3", NotificationType.PROCEED, "Proceed to Counter 2", "Token A36 is currently being served at Counter 2.", "15m ago")
        )
        for (n in notifs) {
            val cv = ContentValues().apply {
                put(COL_NOTIF_ID, n.id)
                put(COL_NOTIF_TYPE, n.type.name)
                put(COL_NOTIF_TITLE, n.title)
                put(COL_NOTIF_MESSAGE, n.message)
                put(COL_NOTIF_TIME_AGO, n.timeAgo)
            }
            db.insert(TABLE_NOTIFICATIONS, null, cv)
        }
    }

    // ==========================================
    // USERS OPERATIONS
    // ==========================================

    fun insertUser(user: AppUser, password: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USER_ID, user.id)
            put(COL_USER_NAME, user.name)
            put(COL_USER_EMAIL, user.email.trim().lowercase())
            put(COL_USER_PHONE, user.phone.trim())
            put(COL_USER_PASSWORD, password.trim())
            put(COL_USER_ROLE, user.role)
            put(COL_USER_IS_VERIFIED, if (user.isVerified) 1 else 0)
        }
        return db.insertWithOnConflict(TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllUsers(): List<Pair<AppUser, String>> {
        val list = mutableListOf<Pair<AppUser, String>>()
        val db = readableDatabase
        val cursor = db.query(TABLE_USERS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow(COL_USER_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COL_USER_NAME))
                val email = it.getString(it.getColumnIndexOrThrow(COL_USER_EMAIL))
                val phone = it.getString(it.getColumnIndexOrThrow(COL_USER_PHONE))
                val pwd = it.getString(it.getColumnIndexOrThrow(COL_USER_PASSWORD))
                val role = it.getString(it.getColumnIndexOrThrow(COL_USER_ROLE))
                val isVerified = it.getInt(it.getColumnIndexOrThrow(COL_USER_IS_VERIFIED)) == 1
                list.add(Pair(AppUser(id, name, email, phone, role, isVerified), pwd))
            }
        }
        return list
    }

    fun getUserByEmailOrPhone(identifier: String): Pair<AppUser, String>? {
        val db = readableDatabase
        val cleanIdent = identifier.trim().lowercase()
        val cleanPhone = identifier.replace(Regex("[^0-9]"), "").takeLast(10)
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COL_USER_EMAIL = ? OR ($COL_USER_PHONE = ? AND ? != '')",
            arrayOf(cleanIdent, cleanPhone, cleanPhone),
            null,
            null,
            null,
            "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                val id = it.getString(it.getColumnIndexOrThrow(COL_USER_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COL_USER_NAME))
                val email = it.getString(it.getColumnIndexOrThrow(COL_USER_EMAIL))
                val phone = it.getString(it.getColumnIndexOrThrow(COL_USER_PHONE))
                val pwd = it.getString(it.getColumnIndexOrThrow(COL_USER_PASSWORD))
                val role = it.getString(it.getColumnIndexOrThrow(COL_USER_ROLE))
                val isVerified = it.getInt(it.getColumnIndexOrThrow(COL_USER_IS_VERIFIED)) == 1
                return Pair(AppUser(id, name, email, phone, role, isVerified), pwd)
            }
        }
        return null
    }

    fun updateUser(user: AppUser, newPassword: String? = null): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USER_NAME, user.name)
            put(COL_USER_PHONE, user.phone)
            put(COL_USER_ROLE, user.role)
            put(COL_USER_IS_VERIFIED, if (user.isVerified) 1 else 0)
            if (!newPassword.isNullOrBlank()) {
                put(COL_USER_PASSWORD, newPassword.trim())
            }
        }
        return db.update(TABLE_USERS, cv, "$COL_USER_ID = ?", arrayOf(user.id))
    }

    // ==========================================
    // ORGANIZATIONS OPERATIONS
    // ==========================================

    fun insertOrganization(org: Organization): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_ORG_ID, org.id)
            put(COL_ORG_NAME, org.name)
            put(COL_ORG_CATEGORY, org.category)
            put(COL_ORG_ICON_EMOJI, org.iconEmoji)
            put(COL_ORG_ADDRESS, org.address)
            put(COL_ORG_ACTIVE_COUNTERS, org.activeCountersCount)
            put(COL_ORG_IS_OPEN, if (org.isOpen) 1 else 0)
        }
        return db.insertWithOnConflict(TABLE_ORGANIZATIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllOrganizations(): List<Organization> {
        val list = mutableListOf<Organization>()
        val db = readableDatabase
        val cursor = db.query(TABLE_ORGANIZATIONS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    Organization(
                        id = it.getString(it.getColumnIndexOrThrow(COL_ORG_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_ORG_NAME)),
                        category = it.getString(it.getColumnIndexOrThrow(COL_ORG_CATEGORY)),
                        iconEmoji = it.getString(it.getColumnIndexOrThrow(COL_ORG_ICON_EMOJI)),
                        address = it.getString(it.getColumnIndexOrThrow(COL_ORG_ADDRESS)),
                        activeCountersCount = it.getInt(it.getColumnIndexOrThrow(COL_ORG_ACTIVE_COUNTERS)),
                        isOpen = it.getInt(it.getColumnIndexOrThrow(COL_ORG_IS_OPEN)) == 1
                    )
                )
            }
        }
        return list
    }

    // ==========================================
    // SERVICES OPERATIONS
    // ==========================================

    fun insertService(service: QueueService): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_SERV_ID, service.id)
            put(COL_SERV_ORG_ID, service.orgId)
            put(COL_SERV_NAME, service.name)
            put(COL_SERV_CURRENT_TOKEN, service.currentServingToken)
            put(COL_SERV_WAITING, service.peopleWaiting)
            put(COL_SERV_EST_WAIT, service.estimatedWaitMinutes)
            put(COL_SERV_COUNTERS, service.activeCounters)
            put(COL_SERV_EMOJI, service.categoryEmoji)
        }
        return db.insertWithOnConflict(TABLE_SERVICES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllServices(): List<QueueService> {
        val list = mutableListOf<QueueService>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SERVICES, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    QueueService(
                        id = it.getString(it.getColumnIndexOrThrow(COL_SERV_ID)),
                        orgId = it.getString(it.getColumnIndexOrThrow(COL_SERV_ORG_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_SERV_NAME)),
                        currentServingToken = it.getString(it.getColumnIndexOrThrow(COL_SERV_CURRENT_TOKEN)),
                        peopleWaiting = it.getInt(it.getColumnIndexOrThrow(COL_SERV_WAITING)),
                        estimatedWaitMinutes = it.getInt(it.getColumnIndexOrThrow(COL_SERV_EST_WAIT)),
                        activeCounters = it.getInt(it.getColumnIndexOrThrow(COL_SERV_COUNTERS)),
                        categoryEmoji = it.getString(it.getColumnIndexOrThrow(COL_SERV_EMOJI))
                    )
                )
            }
        }
        return list
    }

    fun updateService(service: QueueService): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_SERV_CURRENT_TOKEN, service.currentServingToken)
            put(COL_SERV_WAITING, service.peopleWaiting)
            put(COL_SERV_EST_WAIT, service.estimatedWaitMinutes)
            put(COL_SERV_COUNTERS, service.activeCounters)
        }
        return db.update(TABLE_SERVICES, cv, "$COL_SERV_ID = ?", arrayOf(service.id))
    }

    // ==========================================
    // COUNTERS OPERATIONS
    // ==========================================

    fun insertOrUpdateCounter(counter: CounterInfo): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_COUNTER_ID, counter.id)
            put(COL_COUNTER_NAME, counter.name)
            put(COL_COUNTER_SERVING_TOKEN, counter.currentlyServingToken)
            put(COL_COUNTER_IS_ACTIVE, if (counter.isActive) 1 else 0)
        }
        return db.insertWithOnConflict(TABLE_COUNTERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllCounters(): List<CounterInfo> {
        val list = mutableListOf<CounterInfo>()
        val db = readableDatabase
        val cursor = db.query(TABLE_COUNTERS, null, null, null, null, null, "$COL_COUNTER_ID ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    CounterInfo(
                        id = it.getInt(it.getColumnIndexOrThrow(COL_COUNTER_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_COUNTER_NAME)),
                        currentlyServingToken = it.getString(it.getColumnIndexOrThrow(COL_COUNTER_SERVING_TOKEN)),
                        isActive = it.getInt(it.getColumnIndexOrThrow(COL_COUNTER_IS_ACTIVE)) == 1
                    )
                )
            }
        }
        return list
    }

    // ==========================================
    // TOKENS OPERATIONS
    // ==========================================

    fun saveToken(token: TokenInfo): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TOKEN_NUMBER, token.tokenNumber)
            put(COLUMN_SERVICE_ID, token.serviceId)
            put(COLUMN_SERVICE_NAME, token.serviceName)
            put(COLUMN_ORG_NAME, token.orgName)
            put(COLUMN_CURRENTLY_SERVING, token.currentlyServingToken)
            put(COLUMN_PEOPLE_AHEAD, token.peopleAhead)
            put(COLUMN_EST_WAIT_MINS, token.estimatedWaitMinutes)
            put(COLUMN_ASSIGNED_COUNTER, token.assignedCounter)
            put(COLUMN_RECOMMENDED_ARRIVAL, token.recommendedArrival)
            put(COLUMN_STATUS, token.status.name)
            put(COLUMN_PROGRESS_STEPS, token.progressSteps.joinToString(","))
            put(COLUMN_ETA_REASON, token.etaUpdateReason)
        }
        return db.insertWithOnConflict(TABLE_TOKENS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getActiveToken(): TokenInfo? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TOKENS,
            null,
            "$COLUMN_STATUS != ?",
            arrayOf(TokenStatus.COMPLETED.name),
            null,
            null,
            null,
            "1"
        )

        cursor.use {
            if (it.moveToFirst()) {
                val tokenNum = it.getString(it.getColumnIndexOrThrow(COLUMN_TOKEN_NUMBER))
                val serviceId = it.getString(it.getColumnIndexOrThrow(COLUMN_SERVICE_ID))
                val serviceName = it.getString(it.getColumnIndexOrThrow(COLUMN_SERVICE_NAME))
                val orgName = it.getString(it.getColumnIndexOrThrow(COLUMN_ORG_NAME))
                val currentlyServing = it.getString(it.getColumnIndexOrThrow(COLUMN_CURRENTLY_SERVING))
                val peopleAhead = it.getInt(it.getColumnIndexOrThrow(COLUMN_PEOPLE_AHEAD))
                val estWait = it.getInt(it.getColumnIndexOrThrow(COLUMN_EST_WAIT_MINS))
                val counter = it.getString(it.getColumnIndexOrThrow(COLUMN_ASSIGNED_COUNTER))
                val recArrival = it.getString(it.getColumnIndexOrThrow(COLUMN_RECOMMENDED_ARRIVAL))
                val statusStr = it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS))
                val status = try { TokenStatus.valueOf(statusStr) } catch (_: Exception) { TokenStatus.WAITING }
                val stepsStr = it.getString(it.getColumnIndexOrThrow(COLUMN_PROGRESS_STEPS)) ?: ""
                val steps = if (stepsStr.isNotBlank()) stepsStr.split(",") else listOf(currentlyServing, tokenNum)
                val etaReason = it.getString(it.getColumnIndexOrThrow(COLUMN_ETA_REASON))

                return TokenInfo(
                    tokenNumber = tokenNum,
                    serviceId = serviceId,
                    serviceName = serviceName,
                    orgName = orgName,
                    currentlyServingToken = currentlyServing,
                    peopleAhead = peopleAhead,
                    estimatedWaitMinutes = estWait,
                    assignedCounter = counter,
                    recommendedArrival = recArrival,
                    status = status,
                    progressSteps = steps,
                    etaUpdateReason = etaReason
                )
            }
        }
        return null
    }

    fun deleteActiveToken(tokenNumber: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_TOKENS, "$COLUMN_TOKEN_NUMBER = ?", arrayOf(tokenNumber))
    }

    fun deleteAllTokens(): Int {
        val db = writableDatabase
        return db.delete(TABLE_TOKENS, null, null)
    }

    // ==========================================
    // HISTORY OPERATIONS
    // ==========================================

    fun insertHistory(item: QueueHistoryItem): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_HISTORY_ID, item.id)
            put(COLUMN_SERVICE_NAME, item.serviceName)
            put(COLUMN_ORG_NAME, item.orgName)
            put(COLUMN_DATE_TEXT, item.dateText)
            put(COLUMN_TOKEN_NUMBER, item.tokenNumber)
            put(COLUMN_STATUS, item.status)
        }
        return db.insertWithOnConflict(TABLE_HISTORY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllHistory(): List<QueueHistoryItem> {
        val list = mutableListOf<QueueHistoryItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_HISTORY, null, null, null, null, null, "$COLUMN_HISTORY_ID DESC")

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow(COLUMN_HISTORY_ID))
                val serviceName = it.getString(it.getColumnIndexOrThrow(COLUMN_SERVICE_NAME))
                val orgName = it.getString(it.getColumnIndexOrThrow(COLUMN_ORG_NAME))
                val dateText = it.getString(it.getColumnIndexOrThrow(COLUMN_DATE_TEXT))
                val tokenNumber = it.getString(it.getColumnIndexOrThrow(COLUMN_TOKEN_NUMBER))
                val status = it.getString(it.getColumnIndexOrThrow(COLUMN_STATUS))

                list.add(
                    QueueHistoryItem(
                        id = id,
                        serviceName = serviceName,
                        orgName = orgName,
                        dateText = dateText,
                        tokenNumber = tokenNumber,
                        status = status
                    )
                )
            }
        }
        return list
    }

    // ==========================================
    // NOTIFICATIONS OPERATIONS
    // ==========================================

    fun insertNotification(item: NotificationItem): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_NOTIF_ID, item.id)
            put(COL_NOTIF_TYPE, item.type.name)
            put(COL_NOTIF_TITLE, item.title)
            put(COL_NOTIF_MESSAGE, item.message)
            put(COL_NOTIF_TIME_AGO, item.timeAgo)
        }
        return db.insertWithOnConflict(TABLE_NOTIFICATIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllNotifications(): List<NotificationItem> {
        val list = mutableListOf<NotificationItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NOTIFICATIONS, null, null, null, null, null, "$COL_NOTIF_ID DESC")
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow(COL_NOTIF_ID))
                val typeStr = it.getString(it.getColumnIndexOrThrow(COL_NOTIF_TYPE))
                val type = try { NotificationType.valueOf(typeStr) } catch (_: Exception) { NotificationType.INFO }
                val title = it.getString(it.getColumnIndexOrThrow(COL_NOTIF_TITLE))
                val message = it.getString(it.getColumnIndexOrThrow(COL_NOTIF_MESSAGE))
                val timeAgo = it.getString(it.getColumnIndexOrThrow(COL_NOTIF_TIME_AGO))
                list.add(NotificationItem(id, type, title, message, timeAgo))
            }
        }
        return list
    }
}
