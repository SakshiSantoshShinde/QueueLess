package com.example.queueless_smartqueue.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Utility for opening native Android SMS app, phone dialer, and email client.
 */
object SmsHelper {

    /**
     * Opens the device's native SMS application with pre-filled recipient and message.
     */
    fun openSmsApp(context: Context, phoneNumber: String = "", message: String) {
        val cleanPhone = phoneNumber.trim().replace(" ", "").replace("-", "")
        try {
            val uri = if (cleanPhone.isNotEmpty()) {
                Uri.parse("smsto:$cleanPhone")
            } else {
                Uri.parse("smsto:")
            }
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback for devices where ACTION_SENDTO smsto: isn't handled directly
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(if (cleanPhone.isNotEmpty()) "sms:$cleanPhone" else "sms:")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            } catch (fallbackError: Exception) {
                Toast.makeText(
                    context,
                    "Unable to open SMS app: ${fallbackError.localizedMessage ?: "No SMS client available"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Launch phone dialer with pre-filled phone number
     */
    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        val cleanPhone = phoneNumber.trim().replace(" ", "").replace("-", "")
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open dialer: ${e.localizedMessage ?: "No dialer available"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Launch default email application with pre-filled email address and subject
     */
    fun sendEmail(context: Context, emailAddress: String, subject: String, body: String = "") {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${emailAddress.trim()}")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                if (body.isNotEmpty()) {
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open email client: ${e.localizedMessage ?: "No email app found"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
