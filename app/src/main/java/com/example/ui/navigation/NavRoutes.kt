package com.example.ui.navigation

object NavRoutes {
    const val LOGIN = "login"
    const val CHANGE_PASSWORD = "change_password"
    const val DASHBOARD = "dashboard"
    const val INQUIRIES = "inquiries"
    const val CUSTOMERS = "customers"
    const val CUSTOMER_360 = "customer_360/{customerId}"
    const val ADMISSIONS = "admissions"
    const val ORDERS = "orders"
    const val PAYMENTS = "payments"
    const val INVOICES = "invoices"
    const val CALENDAR = "calendar"
    const val WHATSAPP = "whatsapp"
    const val GLOBAL_SEARCH = "global_search"
    const val REPORTS = "reports"
    const val RECYCLE_BIN = "recycle_bin"
    const val AUDIT_LOG = "audit_log"
    const val ASK_AI = "ask_ai"
    const val SETTINGS = "settings"

    fun customer360(customerId: String): String = "customer_360/$customerId"
}
