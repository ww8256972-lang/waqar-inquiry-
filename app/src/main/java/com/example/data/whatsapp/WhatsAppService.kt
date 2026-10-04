package com.example.data.whatsapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.dao.WhatsAppDao
import com.example.data.local.entity.WhatsAppLogEntity
import java.net.URLEncoder
import java.util.Calendar
import java.util.UUID

class WhatsAppService(
    private val context: Context,
    private val whatsAppDao: WhatsAppDao
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("waqar_whatsapp_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CHANNEL_ID = "waqar_inquiry_notifications"
        const val PREF_DAILY_LIMIT = "daily_msg_limit"
        const val PREF_AUTO_SEND_ENABLED = "auto_send_enabled"
        const val DEFAULT_DAILY_LIMIT = 100

        const val TEMPLATE_DEFAULT_WELCOME =
            "Congratulations! Thank you for contacting us. On behalf of Waqar, your inquiry and admission form will be sent automatically; we won't need to send it manually."

        const val TEMPLATE_VERIFICATION_COMPLETE =
            "Hello {customer_name}! Your admission verification (ID: {admission_id}) has been completed successfully by Waqar Inquiry Services. Please review your admission confirmation."

        const val TEMPLATE_PAYMENT_RECEIPT =
            "Hello {customer_name}! Thank you for your payment of {amount}. Your remaining balance is {remaining_amount}. Receipt reference: {record_id}."

        const val TEMPLATE_ORDER_STATUS =
            "Hello {customer_name}! Your order #{order_id} is now '{status}'. Please contact Waqar Website Inquiry for any questions."
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Waqar Inquiry Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Automatic inquiries, admission updates, and WhatsApp triggers"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun getDailyLimit(): Int = prefs.getInt(PREF_DAILY_LIMIT, DEFAULT_DAILY_LIMIT)
    fun setDailyLimit(limit: Int) = prefs.edit().putInt(PREF_DAILY_LIMIT, limit).apply()

    fun isAutoSendEnabled(): Boolean = prefs.getBoolean(PREF_AUTO_SEND_ENABLED, true)
    fun setAutoSendEnabled(enabled: Boolean) = prefs.edit().putBoolean(PREF_AUTO_SEND_ENABLED, enabled).apply()

    suspend fun getTodayCount(): Int {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return whatsAppDao.getTodayMessageCount(calendar.timeInMillis)
    }

    suspend fun getRemainingDailyQuota(): Int {
        val used = getTodayCount()
        val limit = getDailyLimit()
        return (limit - used).coerceAtLeast(0)
    }

    fun renderMessage(
        template: String,
        customerName: String = "",
        phone: String = "",
        amount: String = "",
        remainingAmount: String = "",
        recordId: String = "",
        orderId: String = "",
        admissionId: String = "",
        dueDate: String = "",
        status: String = ""
    ): String {
        return template
            .replace("{customer_name}", customerName)
            .replace("{phone}", phone)
            .replace("{amount}", amount)
            .replace("{remaining_amount}", remainingAmount)
            .replace("{record_id}", recordId)
            .replace("{order_id}", orderId)
            .replace("{admission_id}", admissionId)
            .replace("{due_date}", dueDate)
            .replace("{status}", status)
    }

    suspend fun triggerAutomaticNotification(
        customerName: String,
        phone: String,
        message: String,
        templateType: String = "INQUIRY_WELCOME"
    ): Boolean {
        val remaining = getRemainingDailyQuota()
        if (remaining <= 0) {
            showNotification(
                "WhatsApp Message Limit Reached",
                "Daily quota of ${getDailyLimit()} reached. Automatic message saved for retry."
            )
            val log = WhatsAppLogEntity(
                id = UUID.randomUUID().toString(),
                customerName = customerName,
                phone = phone,
                templateType = templateType,
                messageText = message,
                status = "FAILED",
                errorMessage = "Daily limit reached"
            )
            whatsAppDao.insertLog(log)
            return false
        }

        // Post high-priority system notification with the required message
        showNotification(
            "Waqar Website Inquiry Notification",
            message
        )

        val log = WhatsAppLogEntity(
            id = UUID.randomUUID().toString(),
            customerName = customerName,
            phone = phone,
            templateType = templateType,
            messageText = message,
            status = "SENT"
        )
        whatsAppDao.insertLog(log)
        return true
    }

    fun openWhatsAppIntent(phone: String, message: String) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "WhatsApp is not installed or invalid phone number", Toast.LENGTH_LONG).show()
        }
    }

    fun showNotification(title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
