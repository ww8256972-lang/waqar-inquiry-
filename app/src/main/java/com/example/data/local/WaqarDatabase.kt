package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AdmissionDao
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.InquiryRecordDao
import com.example.data.local.dao.InvoiceQuotationDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.SyncQueueDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WhatsAppDao
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

@Database(
    entities = [
        CustomerEntity::class,
        InquiryRecordEntity::class,
        AdmissionEntity::class,
        OrderEntity::class,
        PaymentEntity::class,
        InvoiceEntity::class,
        QuotationEntity::class,
        ReminderEntity::class,
        WhatsAppLogEntity::class,
        AuditLogEntity::class,
        AppUserEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WaqarDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun inquiryDao(): InquiryRecordDao
    abstract fun admissionDao(): AdmissionDao
    abstract fun orderDao(): OrderDao
    abstract fun paymentDao(): PaymentDao
    abstract fun invoiceQuotationDao(): InvoiceQuotationDao
    abstract fun reminderDao(): ReminderDao
    abstract fun whatsAppDao(): WhatsAppDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun userDao(): UserDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: WaqarDatabase? = null

        fun getInstance(context: Context): WaqarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaqarDatabase::class.java,
                    "waqar_inquiry_db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
