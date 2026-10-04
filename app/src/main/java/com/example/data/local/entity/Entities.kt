package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["email"]),
        Index(value = ["isDeleted"]),
        Index(value = ["name"])
    ]
)
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val whatsapp: String,
    val email: String,
    val address: String,
    val notes: String = "",
    val tags: String = "",
    val registrationDate: Long = System.currentTimeMillis(),
    val lastInteractionDate: Long = System.currentTimeMillis(),
    val nextFollowUpDate: Long? = null,
    val isFavorite: Boolean = false,
    val status: String = "Active",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "inquiry_records",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["status"]),
        Index(value = ["phone"]),
        Index(value = ["isDeleted"]),
        Index(value = ["followUpDate"])
    ]
)
data class InquiryRecordEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val phone: String,
    val whatsapp: String,
    val email: String,
    val inquiryDetails: String,
    val serviceOrProject: String,
    val inquirySource: String = "Website",
    val estimatedCost: Double = 0.0,
    val finalCost: Double = 0.0,
    val amountPaid: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val status: String = "New",
    val priority: String = "MEDIUM",
    val assignedStaff: String = "Waqar",
    val followUpDate: Long? = null,
    val notes: String = "",
    val tags: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "admissions",
    indices = [
        Index(value = ["registrationNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["verificationStatus"]),
        Index(value = ["isDeleted"])
    ]
)
data class AdmissionEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val applicantName: String,
    val phone: String,
    val whatsapp: String,
    val email: String,
    val address: String,
    val courseOrService: String,
    val registrationNumber: String,
    val admissionDate: Long = System.currentTimeMillis(),
    val verificationStatus: String = "Pending",
    val admissionStatus: String = "Pending",
    val cost: Double = 0.0,
    val amountPaid: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentStatus: String = "Unpaid",
    val assignedStaff: String = "Waqar",
    val notes: String = "",
    val documentNames: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["status"]),
        Index(value = ["isDeleted"])
    ]
)
data class OrderEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val phone: String,
    val titleOrService: String,
    val orderDetails: String,
    val status: String = "Pending",
    val totalCost: Double = 0.0,
    val amountPaid: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentStatus: String = "Unpaid",
    val deadline: Long? = null,
    val assignedStaff: String = "Waqar",
    val notes: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["recordId"]),
        Index(value = ["customerId"]),
        Index(value = ["paymentDate"])
    ]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val recordType: String, // INQUIRY, ADMISSION, ORDER, INVOICE
    val recordId: String,
    val customerId: String,
    val customerName: String,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String,
    val transactionReference: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["isDeleted"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val quotationNumber: String? = null,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val invoiceDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000),
    val subtotal: Double,
    val taxRate: Double = 18.0,
    val taxAmount: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double,
    val amountPaid: Double = 0.0,
    val remainingAmount: Double,
    val paymentStatus: String = "Unpaid",
    val notes: String = "Thank you for choosing Waqar Website Inquiry & Services.",
    val terms: String = "1. Payment is due within 7 days. 2. Non-refundable after work initiation.",
    val signatureName: String = "Waqar (Authorized Signatory)",
    val itemsJson: String = "[]",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "quotations",
    indices = [
        Index(value = ["quotationNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["isDeleted"])
    ]
)
data class QuotationEntity(
    @PrimaryKey val id: String,
    val quotationNumber: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val quotationDate: Long = System.currentTimeMillis(),
    val validUntil: Long = System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000),
    val subtotal: Double,
    val taxRate: Double = 18.0,
    val taxAmount: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double,
    val notes: String = "Valid for 15 days from issue date.",
    val terms: String = "50% advance upon project commencement.",
    val signatureName: String = "Waqar (Authorized Signatory)",
    val itemsJson: String = "[]",
    val status: String = "Sent",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["dueDate"]),
        Index(value = ["isCompleted"]),
        Index(value = ["isDeleted"])
    ]
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val customerId: String? = null,
    val customerName: String? = null,
    val relatedType: String = "GENERAL",
    val relatedId: String? = null,
    val title: String,
    val description: String = "",
    val dueDate: Long,
    val priority: String = "MEDIUM",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)

@Entity(
    tableName = "whatsapp_logs",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["status"])
    ]
)
data class WhatsAppLogEntity(
    @PrimaryKey val id: String,
    val customerName: String,
    val phone: String,
    val templateType: String,
    val messageText: String,
    val status: String = "SENT", // PENDING, SENT, FAILED
    val scheduledTime: Long? = null,
    val sentTime: Long? = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["action"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val username: String,
    val action: String,
    val targetEntity: String,
    val targetId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "app_users",
    indices = [
        Index(value = ["username"], unique = true)
    ]
)
data class AppUserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val passwordHash: String,
    val role: String,
    val fullName: String,
    val isActive: Boolean = true,
    val mustChangePassword: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val operation: String, // INSERT, UPDATE, DELETE
    val payloadJson: String,
    val attempts: Int = 0,
    val lastAttemptTime: Long? = null,
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)
