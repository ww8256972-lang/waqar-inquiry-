package com.example

import android.app.Application
import com.example.data.backup.BackupManager
import com.example.data.local.WaqarDatabase
import com.example.data.repository.WaqarRepository
import com.example.data.security.SessionManager
import com.example.data.sync.SyncManager
import com.example.data.whatsapp.WhatsAppService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WaqarInquiryApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: WaqarDatabase
        private set

    lateinit var sessionManager: SessionManager
        private set

    lateinit var whatsAppService: WhatsAppService
        private set

    lateinit var syncManager: SyncManager
        private set

    lateinit var backupManager: BackupManager
        private set

    lateinit var repository: WaqarRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = WaqarDatabase.getInstance(this)
        sessionManager = SessionManager(this)
        whatsAppService = WhatsAppService(this, database.whatsAppDao())
        syncManager = SyncManager(this, database.syncQueueDao())
        backupManager = BackupManager(this, database)

        repository = WaqarRepository(
            context = this,
            database = database,
            sessionManager = sessionManager,
            whatsAppService = whatsAppService,
            syncManager = syncManager,
            backupManager = backupManager
        )

        // Seed initial admin user if not present & ensure audit log integrity
        applicationScope.launch {
            repository.initializeDatabase()
            syncManager.updatePendingCount()
        }
    }
}
