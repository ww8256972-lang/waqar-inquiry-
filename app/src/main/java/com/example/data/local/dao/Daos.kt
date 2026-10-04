package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import com.example.data.local.entity.SyncQueueEntity
import com.example.data.local.entity.WhatsAppLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit OFFSET :offset")
    fun getCustomersPaged(limit: Int, offset: Int): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllActiveCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Query("""
        SELECT * FROM customers 
        WHERE isDeleted = 0 
        AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT COUNT(*) FROM customers WHERE isDeleted = 0")
    fun getActiveCustomerCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("UPDATE customers SET isDeleted = 1, deletedAt = :deletedAt, deletedBy = :deletedBy WHERE id = :id")
    suspend fun softDeleteCustomer(id: String, deletedAt: Long = System.currentTimeMillis(), deletedBy: String)

    @Query("UPDATE customers SET isDeleted = 0, deletedAt = NULL, deletedBy = NULL WHERE id = :id")
    suspend fun restoreCustomer(id: String)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun permanentlyDeleteCustomer(id: String)
}

@Dao
interface InquiryRecordDao {
    @Query("SELECT * FROM inquiry_records WHERE isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit OFFSET :offset")
    fun getInquiriesPaged(limit: Int, offset: Int): Flow<List<InquiryRecordEntity>>

    @Query("SELECT * FROM inquiry_records WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllActiveInquiries(): Flow<List<InquiryRecordEntity>>

    @Query("SELECT * FROM inquiry_records WHERE customerId = :customerId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getInquiriesByCustomer(customerId: String): Flow<List<InquiryRecordEntity>>

    @Query("SELECT * FROM inquiry_records WHERE id = :id")
    suspend fun getInquiryById(id: String): InquiryRecordEntity?

    @Query("""
        SELECT * FROM inquiry_records 
        WHERE isDeleted = 0 
        AND (customerName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR serviceOrProject LIKE '%' || :query || '%' OR id LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC
    """)
    fun searchInquiries(query: String): Flow<List<InquiryRecordEntity>>

    @Query("SELECT * FROM inquiry_records WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedInquiries(): Flow<List<InquiryRecordEntity>>

    @Query("SELECT COUNT(*) FROM inquiry_records WHERE isDeleted = 0")
    fun getTotalInquiriesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inquiry_records WHERE isDeleted = 0 AND status = 'Pending'")
    fun getPendingInquiriesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: InquiryRecordEntity)

    @Update
    suspend fun updateInquiry(inquiry: InquiryRecordEntity)

    @Query("UPDATE inquiry_records SET isDeleted = 1, deletedAt = :deletedAt, deletedBy = :deletedBy WHERE id = :id")
    suspend fun softDeleteInquiry(id: String, deletedAt: Long = System.currentTimeMillis(), deletedBy: String)

    @Query("UPDATE inquiry_records SET isDeleted = 0, deletedAt = NULL, deletedBy = NULL WHERE id = :id")
    suspend fun restoreInquiry(id: String)

    @Query("DELETE FROM inquiry_records WHERE id = :id")
    suspend fun permanentlyDeleteInquiry(id: String)
}

@Dao
interface AdmissionDao {
    @Query("SELECT * FROM admissions WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllActiveAdmissions(): Flow<List<AdmissionEntity>>

    @Query("SELECT * FROM admissions WHERE customerId = :customerId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getAdmissionsByCustomer(customerId: String): Flow<List<AdmissionEntity>>

    @Query("SELECT * FROM admissions WHERE id = :id")
    suspend fun getAdmissionById(id: String): AdmissionEntity?

    @Query("SELECT * FROM admissions WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedAdmissions(): Flow<List<AdmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmission(admission: AdmissionEntity)

    @Update
    suspend fun updateAdmission(admission: AdmissionEntity)

    @Query("UPDATE admissions SET isDeleted = 1, deletedAt = :deletedAt, deletedBy = :deletedBy WHERE id = :id")
    suspend fun softDeleteAdmission(id: String, deletedAt: Long = System.currentTimeMillis(), deletedBy: String)

    @Query("UPDATE admissions SET isDeleted = 0, deletedAt = NULL, deletedBy = NULL WHERE id = :id")
    suspend fun restoreAdmission(id: String)

    @Query("DELETE FROM admissions WHERE id = :id")
    suspend fun permanentlyDeleteAdmission(id: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllActiveOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getOrdersByCustomer(customerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedOrders(): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET isDeleted = 1, deletedAt = :deletedAt, deletedBy = :deletedBy WHERE id = :id")
    suspend fun softDeleteOrder(id: String, deletedAt: Long = System.currentTimeMillis(), deletedBy: String)

    @Query("UPDATE orders SET isDeleted = 0, deletedAt = NULL, deletedBy = NULL WHERE id = :id")
    suspend fun restoreOrder(id: String)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun permanentlyDeleteOrder(id: String)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE recordId = :recordId ORDER BY paymentDate DESC")
    fun getPaymentsByRecord(recordId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsByCustomer(customerId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC LIMIT 100")
    fun getAllRecentPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: String): PaymentEntity?

    @Query("SELECT SUM(amount) FROM payments")
    fun getTotalRevenue(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: String)
}

@Dao
interface InvoiceQuotationDao {
    @Query("SELECT * FROM invoices WHERE isDeleted = 0 ORDER BY invoiceDate DESC")
    fun getAllActiveInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId AND isDeleted = 0 ORDER BY invoiceDate DESC")
    fun getInvoicesByCustomer(customerId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("UPDATE invoices SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteInvoice(id: String, deletedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM quotations WHERE isDeleted = 0 ORDER BY quotationDate DESC")
    fun getAllActiveQuotations(): Flow<List<QuotationEntity>>

    @Query("SELECT * FROM quotations WHERE customerId = :customerId AND isDeleted = 0 ORDER BY quotationDate DESC")
    fun getQuotationsByCustomer(customerId: String): Flow<List<QuotationEntity>>

    @Query("SELECT * FROM quotations WHERE id = :id")
    suspend fun getQuotationById(id: String): QuotationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: QuotationEntity)

    @Update
    suspend fun updateQuotation(quotation: QuotationEntity)

    @Query("UPDATE quotations SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteQuotation(id: String, deletedAt: Long = System.currentTimeMillis())
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isDeleted = 0 AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isDeleted = 0 ORDER BY dueDate DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun markCompleted(id: String, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE reminders SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteReminder(id: String, deletedAt: Long = System.currentTimeMillis())
}

@Dao
interface WhatsAppDao {
    @Query("SELECT * FROM whatsapp_logs ORDER BY createdAt DESC LIMIT 100")
    fun getAllLogs(): Flow<List<WhatsAppLogEntity>>

    @Query("SELECT COUNT(*) FROM whatsapp_logs WHERE createdAt >= :startOfDay")
    suspend fun getTodayMessageCount(startOfDay: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WhatsAppLogEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM app_users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): AppUserEntity?

    @Query("SELECT * FROM app_users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<AppUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUserEntity)

    @Query("UPDATE app_users SET passwordHash = :newHash, mustChangePassword = 0 WHERE id = :userId")
    suspend fun updatePassword(userId: String, newHash: String)
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingSyncItems(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteQueueItem(id: String)

    @Query("UPDATE sync_queue SET status = :status, attempts = attempts + 1, lastAttemptTime = :now WHERE id = :id")
    suspend fun updateQueueItemStatus(id: String, status: String, now: Long = System.currentTimeMillis())
}
