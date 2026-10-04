package com.example.data.ai

import com.example.BuildConfig

data class HelpAnswer(
    val title: String,
    val summary: String,
    val steps: List<String>,
    val tips: String
)

object AiHelpService {

    private val faqDatabase = mapOf(
        "add_record" to HelpAnswer(
            title = "Adding a New Inquiry / Record",
            summary = "How to create a new customer inquiry with cost and WhatsApp trigger.",
            steps = listOf(
                "Navigate to 'Inquiries' tab from the bottom bar or tap '+ New Record' on Dashboard.",
                "Fill in Customer Name, Phone number, and Service/Project details.",
                "Specify Estimated & Final Cost in ₹, and any initial payment received.",
                "Select initial status (e.g. 'New' or 'Verification Pending') and priority.",
                "Tap 'Save Record'. The system will automatically trigger a WhatsApp confirmation notification on behalf of Waqar!"
            ),
            tips = "Existing customers are matched automatically by phone number to prevent duplicates."
        ),
        "customer_management" to HelpAnswer(
            title = "Managing Customers & Customer 360",
            summary = "View full history, interactions, and financial balances for any customer.",
            steps = listOf(
                "Open 'Customers' or use Global Search to locate a contact.",
                "Tap any customer to open the 'Customer 360° Profile'.",
                "View linked Inquiries, Admissions, Orders, Invoices, and Payment ledger.",
                "Use the quick WhatsApp button to initiate personalized messaging without saving manual numbers."
            ),
            tips = "You can star key accounts to mark them as Favorites for quick one-tap access."
        ),
        "admission_verification" to HelpAnswer(
            title = "Admission Form & Verification Process",
            summary = "Managing candidate admission documents and verification lifecycle.",
            steps = listOf(
                "Go to 'Admissions' screen.",
                "Tap on the candidate or create a new Admission entry.",
                "Under 'Verification Status', select 'Under Review' or 'Verified'.",
                "Record document verification details (e.g. ID proofs, Marksheets).",
                "Once verified, the system sends an automatic WhatsApp verification message to the candidate."
            ),
            tips = "Candidates can be moved from 'Pending' to 'Admission Complete' once tuition or fees are paid."
        ),
        "orders_workflow" to HelpAnswer(
            title = "Orders Pending / Completed Workflow",
            summary = "Track services, deadlines, and milestone payments.",
            steps = listOf(
                "Open the 'Orders' section.",
                "Switch between 'Pending', 'Processing', and 'Completed' tabs.",
                "Click 'Update Status' to advance from Pending to Processing or Completed.",
                "Enter amount paid in ₹; remaining balance calculates automatically in real-time."
            ),
            tips = "Completed orders can generate instant PDF receipts for customer handoff."
        ),
        "whatsapp_integration" to HelpAnswer(
            title = "WhatsApp Auto Integration & Limits",
            summary = "Official WhatsApp Intent deep linking and daily message quota engine.",
            steps = listOf(
                "Choose any record and tap the green WhatsApp icon.",
                "Select from pre-configured templates (Inquiry Welcome, Payment Receipt, Verification).",
                "Placeholders like {customer_name}, {amount}, {admission_id} are auto-filled.",
                "Send via official WhatsApp intent or review the audit log."
            ),
            tips = "Admin can configure daily quotas (default: 100/day) in Settings to avoid spam limits."
        ),
        "payment_receipt" to HelpAnswer(
            title = "Generating Payment Receipts & Invoices",
            summary = "Offline-first native PDF generation with Rupee ₹ calculations.",
            steps = listOf(
                "Navigate to 'Payments' or 'Invoices'.",
                "Select 'New Payment' or open an existing invoice.",
                "Enter payment method (UPI, Cash, Bank Transfer, Card) and reference/UTR number.",
                "Tap 'Generate PDF Receipt' or 'Download Invoice'.",
                "The PDF is rendered locally on device and can be printed or shared directly to WhatsApp."
            ),
            tips = "Financial totals are computed with decimal safety to prevent fractional floating errors."
        ),
        "recycle_bin" to HelpAnswer(
            title = "Recycle Bin & Safe Soft Deletion",
            summary = "Zero customer data loss policy with restore and audit logging.",
            steps = listOf(
                "Deleting a customer, inquiry, or order moves it to the Recycle Bin.",
                "Open 'Settings' -> 'Recycle Bin'.",
                "View who deleted the record and when.",
                "Tap 'Restore' to return the record to active status instantly.",
                "Permanent deletion requires Admin role confirmation."
            ),
            tips = "All delete and restore actions are permanently logged in the Immutable Audit Log."
        ),
        "sync_offline" to HelpAnswer(
            title = "Offline Operation & Cloud Sync",
            summary = "Full offline-first reliability with automatic conflict resolution.",
            steps = listOf(
                "The app operates 100% offline using local encrypted Room storage.",
                "Changes made while offline are queued safely in the Sync Queue.",
                "When internet connectivity returns, tap 'Sync Now' on the Dashboard or let background sync handle it.",
                "No records are ever discarded or deleted due to connection drops."
            ),
            tips = "Check 'System Health' in Settings to view pending queue size and database status."
        )
    )

    fun getScreenContextHelp(route: String): HelpAnswer {
        return when {
            route.contains("inquiry") -> faqDatabase["add_record"]!!
            route.contains("customer") -> faqDatabase["customer_management"]!!
            route.contains("admission") -> faqDatabase["admission_verification"]!!
            route.contains("order") -> faqDatabase["orders_workflow"]!!
            route.contains("payment") || route.contains("invoice") -> faqDatabase["payment_receipt"]!!
            route.contains("whatsapp") -> faqDatabase["whatsapp_integration"]!!
            route.contains("recycle") -> faqDatabase["recycle_bin"]!!
            else -> faqDatabase["sync_offline"]!!
        }
    }

    fun getAllHelpTopics(): List<HelpAnswer> = faqDatabase.values.toList()

    fun searchHelp(query: String): List<HelpAnswer> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return getAllHelpTopics()
        return faqDatabase.values.filter {
            it.title.lowercase().contains(q) ||
                    it.summary.lowercase().contains(q) ||
                    it.steps.any { step -> step.lowercase().contains(q) } ||
                    it.tips.lowercase().contains(q)
        }
    }
}
