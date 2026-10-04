package com.example.data.model

enum class UserRole {
    SUPER_ADMIN,
    ADMIN,
    MANAGER,
    STAFF
}

enum class RecordStatus(val label: String) {
    NEW("New"),
    PENDING("Pending"),
    UNDER_REVIEW("Under Review"),
    VERIFICATION_PENDING("Verification Pending"),
    VERIFICATION_COMPLETE("Verification Complete"),
    ADMISSION_PENDING("Admission Pending"),
    ADMISSION_COMPLETE("Admission Complete"),
    ORDER_PENDING("Order Pending"),
    ORDER_PROCESSING("Order Processing"),
    ORDER_COMPLETED("Order Completed"),
    PAYMENT_PENDING("Payment Pending"),
    PAYMENT_COMPLETED("Payment Completed"),
    FOLLOW_UP_REQUIRED("Follow-up Required"),
    CANCELLED("Cancelled"),
    ARCHIVED("Archived")
}

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class PaymentStatus {
    UNPAID, PARTIAL, PAID
}

enum class PaymentMethod(val label: String) {
    UPI("UPI"),
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    CHEQUE("Cheque")
}

enum class VerificationStatus {
    PENDING, UNDER_REVIEW, VERIFIED, REJECTED
}

enum class SyncStatus {
    PENDING, SYNCED, FAILED
}

data class LineItem(
    val id: String,
    val description: String,
    val quantity: Int,
    val unitRate: Double,
    val amount: Double
)

data class WhatsAppTemplate(
    val id: String,
    val name: String,
    val text: String,
    val category: String
)
