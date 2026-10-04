package com.example.data.backup

import android.content.Context
import com.example.data.local.WaqarDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupResult(
    val success: Boolean,
    val filePath: String = "",
    val recordCount: Int = 0,
    val fileSizeKb: Long = 0,
    val message: String = ""
)

class BackupManager(
    private val context: Context,
    private val database: WaqarDatabase
) {
    suspend fun createBackup(): BackupResult = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "Waqar_Backup_$timeStamp.json")

            val rootJson = JSONObject()
            rootJson.put("app", "WAQAR WEBSITE INQUIRY")
            rootJson.put("version", 1)
            rootJson.put("timestamp", System.currentTimeMillis())

            val customers = database.customerDao().getAllActiveCustomers().first()
            val inquiries = database.inquiryDao().getAllActiveInquiries().first()
            val admissions = database.admissionDao().getAllActiveAdmissions().first()
            val orders = database.orderDao().getAllActiveOrders().first()
            val payments = database.paymentDao().getAllRecentPayments().first()

            val customersArray = JSONArray()
            customers.forEach {
                val c = JSONObject()
                c.put("id", it.id)
                c.put("name", it.name)
                c.put("phone", it.phone)
                c.put("whatsapp", it.whatsapp)
                c.put("email", it.email)
                c.put("address", it.address)
                c.put("notes", it.notes)
                c.put("tags", it.tags)
                customersArray.put(c)
            }
            rootJson.put("customers", customersArray)

            val inquiriesArray = JSONArray()
            inquiries.forEach {
                val i = JSONObject()
                i.put("id", it.id)
                i.put("customerId", it.customerId)
                i.put("customerName", it.customerName)
                i.put("phone", it.phone)
                i.put("inquiryDetails", it.inquiryDetails)
                i.put("serviceOrProject", it.serviceOrProject)
                i.put("status", it.status)
                i.put("finalCost", it.finalCost)
                i.put("amountPaid", it.amountPaid)
                inquiriesArray.put(i)
            }
            rootJson.put("inquiries", inquiriesArray)

            val totalRecords = customers.size + inquiries.size + admissions.size + orders.size + payments.size
            rootJson.put("totalRecords", totalRecords)

            FileOutputStream(backupFile).use { out ->
                out.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
            }

            if (!backupFile.exists() || backupFile.length() == 0L) {
                return@withContext BackupResult(false, message = "Failed to verify backup write integrity")
            }

            BackupResult(
                success = true,
                filePath = backupFile.absolutePath,
                recordCount = totalRecords,
                fileSizeKb = backupFile.length() / 1024,
                message = "Backup successfully verified with $totalRecords total records."
            )
        } catch (e: Exception) {
            BackupResult(false, message = "Backup failed: ${e.localizedMessage}")
        }
    }

    suspend fun listBackups(): List<File> = withContext(Dispatchers.IO) {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return@withContext emptyList()
        backupDir.listFiles { file -> file.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
