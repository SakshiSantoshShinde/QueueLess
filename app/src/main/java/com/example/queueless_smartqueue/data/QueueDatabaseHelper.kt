package com.example.queueless_smartqueue.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.queueless_smartqueue.model.QueueHistoryItem
import com.example.queueless_smartqueue.model.TokenInfo
import com.example.queueless_smartqueue.model.TokenStatus

/**
 * SQLite Database Helper for QueuelessSmartQueue.
 * Manages local SQLite storage for tokens, queue history, and user data.
 */
class QueueDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "queueless_smartqueue.db"
        private const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_TOKENS = "tokens"
        const val TABLE_HISTORY = "history"

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

        // History Columns
        const val COLUMN_HISTORY_ID = "id"
        const val COLUMN_DATE_TEXT = "date_text"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTokensTable = """
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
                $COLUMN_STATUS TEXT
            )
        """.trimIndent()

        val createHistoryTable = """
            CREATE TABLE $TABLE_HISTORY (
                $COLUMN_HISTORY_ID TEXT PRIMARY KEY,
                $COLUMN_SERVICE_NAME TEXT,
                $COLUMN_ORG_NAME TEXT,
                $COLUMN_DATE_TEXT TEXT,
                $COLUMN_TOKEN_NUMBER TEXT,
                $COLUMN_STATUS TEXT
            )
        """.trimIndent()

        db.execSQL(createTokensTable)
        db.execSQL(createHistoryTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TOKENS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    /**
     * Insert or replace an active user token.
     */
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
        }
        return db.insertWithOnConflict(TABLE_TOKENS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Fetch active token from local database.
     */
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
                    progressSteps = listOf(currentlyServing, tokenNum)
                )
            }
        }
        return null
    }

    /**
     * Save queue history item.
     */
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

    /**
     * Retrieve queue history items.
     */
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

    /**
     * Delete active token.
     */
    fun deleteActiveToken(tokenNumber: String) {
        val db = writableDatabase
        db.delete(TABLE_TOKENS, "$COLUMN_TOKEN_NUMBER = ?", arrayOf(tokenNumber))
    }
}
