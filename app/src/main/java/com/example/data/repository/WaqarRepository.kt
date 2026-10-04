package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.WaqarDatabase
import com.example.data.local.entity.AdmissionEntity
import com.example.data.local.entity.AppUserEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.InquiryRecordEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.QuotationEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.model.UserRole
import com.example.data.security.SecurityUtils
import com.example.data.security.SessionManager
import com.example.data.sync.SyncManager
import com.example.data.whatsapp.WhatsAppService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class WaqarRepository(
    private val context: Context,
    val database: WaqarDatabase,
    val sessionManager: SessionManager,
    val whatsAppService: WhatsAppService,
    val syncManager: SyncManager,
    val backupManager: com.example.data.backup.BackupManager
) {
    private val customerDao = database.customerDao()
    private val inquiryDao = database.inquiryDao()
    private val admissionDao = database.admissionDao()
    private val orderDao = database.orderDao()
    private val paymentDao = database.paymentDao()
    private val invoiceDao = database.invoiceQuotationDao()
    private val reminderDao = database.reminderDao()
    private val auditDao = database.auditLogDao()
    private val userDao = database.userDao()

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        val existingAdmin = userDao.getUserByUsername("waqar") ?: userDao.getUserByUsername("Waqar")
        if (existingAdmin == null) {
            val defaultAdmin = AppUserEntity(
                id = UUID.randomUUID().toString(),
                username = "waqar",
                passwordHash = SecurityUtils.hashPassword("waqar"),
                role = UserRole.SUPER_ADMIN.name,
                fullName = "Waqar Administrator",
                isActive = true,
                mustChangePassword = false,
                createdAt = System.currentTimeMillis()
            )
            userDao.insertUser(defaultAdmin)
            auditDao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    username = "SYSTEM",
                    action = "INITIAL_SEED",
                    targetEntity = "USER",
                    targetId = defaultAdmin.id,
                    details = "Initialized default administrative account for waqar with password waqar."
                )
            )
        } else {
            // Ensure password hash for waqar is updated to waqar
            userDao.updatePassword(existingAdmin.id, SecurityUtils.hashPassword("waqar"))
        }
    }

    // --- Customer Operations ---
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllActiveCustomers()
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    fun getDeletedCustomers(): Flow<List<CustomerEntity>> = customerDao.getDeletedCustomers()
    suspend fun getCustomerById(id: String): CustomerEntity? = customerDao.getCustomerById(id)

    suspend fun saveCustomer(customer: CustomerEntity): CustomerEntity = withContext(Dispatchers.IO) {
        val existing = customerDao.getCustomerById(customer.id)
        if (existing == null) {
            customerDao.insertCustomer(customer)
            logAudit("CREATE_CUSTOMER", "CUSTOMER", customer.id, "Created customer ${customer.name} (${customer.phone})")
            syncManager.queueChange("CUSTOMER", customer.id, "INSERT")
        } else {
            val updated = customer.copy(updatedAt = System.currentTimeMillis(), version = customer.version + 1)
            customerDao.updateCustomer(updated)
            logAudit("UPDATE_CUSTOMER", "CUSTOMER", customer.id, "Updated customer details for ${customer.name}")
            syncManager.queueChange("CUSTOMER", customer.id, "UPDATE")
        }
        customer
    }

    suspend fun softDeleteCustomer(id: String) = withContext(Dispatchers.IO) {
        val user = sessionManager.getUsername()
        customerDao.softDeleteCustomer(id, deletedBy = user)
        logAudit("DELETE_CUSTOMER", "CUSTOMER", id, "Moved customer to recycle bin by $user")
        syncManager.queueChange("CUSTOMER", id, "DELETE")
    }

    suspend fun restoreCustomer(id: String) = withContext(Dispatchers.IO) {
        customerDao.restoreCustomer(id)
        logAudit("RESTORE_CUSTOMER", "CUSTOMER", id, "Restored customer from recycle bin")
    }

    suspend fun permanentDeleteCustomer(id: String) = withContext(Dispatchers.IO) {
        customerDao.permanentlyDeleteCustomer(id)
        logAudit("PERMANENT_DELETE", "CUSTOMER", id, "Permanently purged customer record from database")
    }

    // --- Inquiry Operations ---
    fun getAllInquiries(): Flow<List<InquiryRecordEntity>> = inquiryDao.getAllActiveInquiries()
    fun searchInquiries(query: String): Flow<List<InquiryRecordEntity>> = inquiryDao.searchInquiries(query)
    fun getInquiriesByCustomer(customerId: String): Flow<List<InquiryRecordEntity>> = inquiryDao.getInquiriesByCustomer(customerId)
    fun getDeletedInquiries(): Flow<List<InquiryRecordEntity>> = inquiryDao.getDeletedInquiries()
    suspend fun getInquiryById(id: String): InquiryRecordEntity? = inquiryDao.getInquiryById(id)

    suspend fun saveInquiry(inquiry: InquiryRecordEntity, autoNotify: Boolean = true): InquiryRecordEntity = withContext(Dispatchers.IO) {
        database.withTransaction {
            val existing = inquiryDao.getInquiryById(inquiry.id)
            var customer = customerDao.getCustomerByPhone(inquiry.phone)
            if (customer == null) {
                customer = CustomerEntity(
                    id = inquiry.customerId.ifBlank { UUID.randomUUID().toString() },
                    name = inquiry.customerName,
                    phone = inquiry.phone,
                    whatsapp = inquiry.whatsapp.ifBlank { inquiry.phone },
                    email = inquiry.email,
                    address = "",
                    notes = inquiry.notes
                )
                customerDao.insertCustomer(customer)
            }

            val finalRecord = inquiry.copy(
                customerId = customer.id,
                remainingAmount = (inquiry.finalCost - inquiry.amountPaid).coerceAtLeast(0.0),
                updatedAt = System.currentTimeMillis()
            )

            if (existing == null) {
                inquiryDao.insertInquiry(finalRecord)
                logAudit("CREATE_INQUIRY", "INQUIRY", finalRecord.id, "Registered new inquiry for ${finalRecord.customerName} - ${finalRecord.serviceOrProject}")
                syncManager.queueChange("INQUIRY", finalRecord.id, "INSERT")

                if (autoNotify) {
                    whatsAppService.triggerAutomaticNotification(
                        customerName = finalRecord.customerName,
                        phone = finalRecord.phone,
                        message = WhatsAppService.TEMPLATE_DEFAULT_WELCOME,
                        templateType = "INQUIRY_WELCOME"
                    )
                }
            } else {
                inquiryDao.updateInquiry(finalRecord.copy(version = existing.version + 1))
                logAudit("UPDATE_INQUIRY", "INQUIRY", finalRecord.id, "Updated inquiry status: ${finalRecord.status}")
                syncManager.queueChange("INQUIRY", finalRecord.id, "UPDATE")
            }
            finalRecord
        }
    }

    suspend fun softDeleteInquiry(id: String) = withContext(Dispatchers.IO) {
        val user = sessionManager.getUsername()
        inquiryDao.softDeleteInquiry(id, deletedBy = user)
        logAudit("DELETE_INQUIRY", "INQUIRY", id, "Moved inquiry to recycle bin by $user")
        syncManager.queueChange("INQUIRY", id, "DELETE")
    }

    suspend fun restoreInquiry(id: String) = withContext(Dispatchers.IO) {
        inquiryDao.restoreInquiry(id)
        logAudit("RESTORE_INQUIRY", "INQUIRY", id, "Restored inquiry from recycle bin")
    }

    suspend fun permanentDeleteInquiry(id: String) = withContext(Dispatchers.IO) {
        inquiryDao.permanentlyDeleteInquiry(id)
        logAudit("PERMANENT_DELETE", "INQUIRY", id, "Permanently purged inquiry")
    }

    // --- Admission Operations ---
    fun getAllAdmissions(): Flow<List<AdmissionEntity>> = admissionDao.getAllActiveAdmissions()
    fun getAdmissionsByCustomer(customerId: String): Flow<List<AdmissionEntity>> = admissionDao.getAdmissionsByCustomer(customerId)
    fun getDeletedAdmissions(): Flow<List<AdmissionEntity>> = admissionDao.getDeletedAdmissions()
    suspend fun getAdmissionById(id: String): AdmissionEntity? = admissionDao.getAdmissionById(id)

    suspend fun saveAdmission(admission: AdmissionEntity, notifyCandidate: Boolean = false): AdmissionEntity = withContext(Dispatchers.IO) {
        val existing = admissionDao.getAdmissionById(admission.id)
        val remaining = (admission.cost - admission.amountPaid).coerceAtLeast(0.0)
        val pStatus = when {
            remaining <= 0.0 && admission.cost > 0 -> "Paid"
            admission.amountPaid > 0 -> "Partial"
            else -> "Unpaid"
        }
        val record = admission.copy(remainingAmount = remaining, paymentStatus = pStatus, updatedAt = System.currentTimeMillis())

        if (existing == null) {
            admissionDao.insertAdmission(record)
            logAudit("CREATE_ADMISSION", "ADMISSION", record.id, "Created admission form: ${record.applicantName} (${record.registrationNumber})")
            syncManager.queueChange("ADMISSION", record.id, "INSERT")
        } else {
            admissionDao.updateAdmission(record.copy(version = existing.version + 1))
            logAudit("UPDATE_ADMISSION", "ADMISSION", record.id, "Updated admission verification: ${record.verificationStatus}")
            syncManager.queueChange("ADMISSION", record.id, "UPDATE")

            if (notifyCandidate && record.verificationStatus == "Verified") {
                val msg = whatsAppService.renderMessage(
                    WhatsAppService.TEMPLATE_VERIFICATION_COMPLETE,
                    customerName = record.applicantName,
                    admissionId = record.registrationNumber
                )
                whatsAppService.triggerAutomaticNotification(
                    customerName = record.applicantName,
                    phone = record.phone,
                    message = msg,
                    templateType = "VERIFICATION_COMPLETE"
                )
            }
        }
        record
    }

    suspend fun softDeleteAdmission(id: String) = withContext(Dispatchers.IO) {
        val user = sessionManager.getUsername()
        admissionDao.softDeleteAdmission(id, deletedBy = user)
        logAudit("DELETE_ADMISSION", "ADMISSION", id, "Moved admission to recycle bin")
    }

    suspend fun restoreAdmission(id: String) = withContext(Dispatchers.IO) {
        admissionDao.restoreAdmission(id)
        logAudit("RESTORE_ADMISSION", "ADMISSION", id, "Restored admission from recycle bin")
    }

    // --- Order Operations ---
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllActiveOrders()
    fun getOrdersByCustomer(customerId: String): Flow<List<OrderEntity>> = orderDao.getOrdersByCustomer(customerId)
    fun getDeletedOrders(): Flow<List<OrderEntity>> = orderDao.getDeletedOrders()
    suspend fun getOrderById(id: String): OrderEntity? = orderDao.getOrderById(id)

    suspend fun saveOrder(order: OrderEntity): OrderEntity = withContext(Dispatchers.IO) {
        val existing = orderDao.getOrderById(order.id)
        val remaining = (order.totalCost - order.amountPaid).coerceAtLeast(0.0)
        val pStatus = when {
            remaining <= 0.0 && order.totalCost > 0 -> "Paid"
            order.amountPaid > 0 -> "Partial"
            else -> "Unpaid"
        }
        val record = order.copy(remainingAmount = remaining, paymentStatus = pStatus, updatedAt = System.currentTimeMillis())

        if (existing == null) {
            orderDao.insertOrder(record)
            logAudit("CREATE_ORDER", "ORDER", record.id, "Placed order: ${record.titleOrService} for ${record.customerName}")
            syncManager.queueChange("ORDER", record.id, "INSERT")
        } else {
            orderDao.updateOrder(record.copy(version = existing.version + 1))
            logAudit("UPDATE_ORDER", "ORDER", record.id, "Updated order status: ${record.status}")
            syncManager.queueChange("ORDER", record.id, "UPDATE")
        }
        record
    }

    suspend fun softDeleteOrder(id: String) = withContext(Dispatchers.IO) {
        val user = sessionManager.getUsername()
        orderDao.softDeleteOrder(id, deletedBy = user)
        logAudit("DELETE_ORDER", "ORDER", id, "Moved order to recycle bin")
    }

    suspend fun restoreOrder(id: String) = withContext(Dispatchers.IO) {
        orderDao.restoreOrder(id)
        logAudit("RESTORE_ORDER", "ORDER", id, "Restored order from recycle bin")
    }

    // --- Payments Operations ---
    fun getPaymentsByCustomer(customerId: String): Flow<List<PaymentEntity>> = paymentDao.getPaymentsByCustomer(customerId)
    fun getAllPayments(): Flow<List<PaymentEntity>> = paymentDao.getAllRecentPayments()
    fun getTotalRevenue(): Flow<Double?> = paymentDao.getTotalRevenue()

    suspend fun recordPayment(payment: PaymentEntity): PaymentEntity = withContext(Dispatchers.IO) {
        database.withTransaction {
            paymentDao.insertPayment(payment)
            logAudit("RECORD_PAYMENT", "PAYMENT", payment.id, "Recorded payment ₹${payment.amount} via ${payment.paymentMethod} from ${payment.customerName}")

            // Update associated record if Inquiry or Order or Admission
            when (payment.recordType) {
                "INQUIRY" -> {
                    val inq = inquiryDao.getInquiryById(payment.recordId)
                    if (inq != null) {
                        val newPaid = inq.amountPaid + payment.amount
                        val newRem = (inq.finalCost - newPaid).coerceAtLeast(0.0)
                        val pStatus = if (newRem <= 0.0) "Payment Completed" else "Payment Pending"
                        inquiryDao.updateInquiry(inq.copy(amountPaid = newPaid, remainingAmount = newRem, status = pStatus))
                    }
                }
                "ORDER" -> {
                    val ord = orderDao.getOrderById(payment.recordId)
                    if (ord != null) {
                        val newPaid = ord.amountPaid + payment.amount
                        val newRem = (ord.totalCost - newPaid).coerceAtLeast(0.0)
                        val pStatus = if (newRem <= 0.0) "Paid" else "Partial"
                        orderDao.updateOrder(ord.copy(amountPaid = newPaid, remainingAmount = newRem, paymentStatus = pStatus))
                    }
                }
                "ADMISSION" -> {
                    val adm = admissionDao.getAdmissionById(payment.recordId)
                    if (adm != null) {
                        val newPaid = adm.amountPaid + payment.amount
                        val newRem = (adm.cost - newPaid).coerceAtLeast(0.0)
                        val pStatus = if (newRem <= 0.0) "Paid" else "Partial"
                        admissionDao.updateAdmission(adm.copy(amountPaid = newPaid, remainingAmount = newRem, paymentStatus = pStatus))
                    }
                }
            }
            payment
        }
    }

    // --- Invoices & Quotations ---
    fun getAllInvoices(): Flow<List<InvoiceEntity>> = invoiceDao.getAllActiveInvoices()
    fun getAllQuotations(): Flow<List<QuotationEntity>> = invoiceDao.getAllActiveQuotations()

    suspend fun saveInvoice(invoice: InvoiceEntity): InvoiceEntity = withContext(Dispatchers.IO) {
        val existing = invoiceDao.getInvoiceById(invoice.id)
        if (existing == null) {
            invoiceDao.insertInvoice(invoice)
            logAudit("CREATE_INVOICE", "INVOICE", invoice.id, "Generated Invoice ${invoice.invoiceNumber} for ₹${invoice.grandTotal}")
        } else {
            invoiceDao.updateInvoice(invoice.copy(updatedAt = System.currentTimeMillis()))
            logAudit("UPDATE_INVOICE", "INVOICE", invoice.id, "Updated Invoice ${invoice.invoiceNumber}")
        }
        invoice
    }

    suspend fun saveQuotation(quotation: QuotationEntity): QuotationEntity = withContext(Dispatchers.IO) {
        val existing = invoiceDao.getQuotationById(quotation.id)
        if (existing == null) {
            invoiceDao.insertQuotation(quotation)
            logAudit("CREATE_QUOTATION", "QUOTATION", quotation.id, "Generated Quotation ${quotation.quotationNumber} for ₹${quotation.grandTotal}")
        } else {
            invoiceDao.updateQuotation(quotation.copy(updatedAt = System.currentTimeMillis()))
            logAudit("UPDATE_QUOTATION", "QUOTATION", quotation.id, "Updated Quotation ${quotation.quotationNumber}")
        }
        quotation
    }

    // --- Reminders & Calendar ---
    fun getActiveReminders(): Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun saveReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun completeReminder(id: String) = withContext(Dispatchers.IO) {
        reminderDao.markCompleted(id)
    }

    // --- Audit Logs ---
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>> = auditDao.getRecentAuditLogs()

    suspend fun logAudit(action: String, targetEntity: String, targetId: String, details: String) {
        val user = sessionManager.getUsername()
        auditDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                username = user,
                action = action,
                targetEntity = targetEntity,
                targetId = targetId,
                details = details,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // --- Synthetic 15,000 Records Benchmark Generator (Section 37) ---
    suspend fun seedSynthetic15kRecords(onProgress: (Int) -> Unit = {}): Int = withContext(Dispatchers.IO) {
        val batchSize = 1000
        val total = 15000
        val services = listOf("Website Development", "Admission Consultancy", "Digital Marketing", "E-Commerce Setup", "SEO & Cloud Hosting")
        val cities = listOf("Delhi", "Mumbai", "Bangalore", "Hyderabad", "Kolkata", "Chennai", "Pune", "Lucknow")

        for (i in 0 until total step batchSize) {
            val list = mutableListOf<CustomerEntity>()
            for (j in 0 until batchSize) {
                val index = i + j + 1
                val padIndex = index.toString().padStart(5, '0')
                list.add(
                    CustomerEntity(
                        id = UUID.randomUUID().toString(),
                        name = "Customer $padIndex",
                        phone = "98" + (10000000 + index).toString(),
                        whatsapp = "98" + (10000000 + index).toString(),
                        email = "customer$padIndex@waqarinquiry.com",
                        address = "${cities[index % cities.size]}, India",
                        notes = "Synthetic record #$padIndex for large-scale database indexing verification.",
                        tags = "${services[index % services.size]}, High-Value",
                        registrationDate = System.currentTimeMillis() - (index * 60000L),
                        status = if (index % 10 == 0) "Archived" else "Active"
                    )
                )
            }
            customerDao.insertCustomers(list)
            onProgress(i + batchSize)
        }
        logAudit("BENCHMARK_SEED", "DATABASE", "SYSTEM", "Seeded 15,000 synthetic customer records for high-load verification.")
        total
    }
}
