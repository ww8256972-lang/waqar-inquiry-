package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.admission.AdmissionScreen
import com.example.ui.screens.ai.AskAiScreen
import com.example.ui.screens.audit.AuditLogScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.calendar.CalendarRemindersScreen
import com.example.ui.screens.customer.Customer360Screen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.inquiry.InquiryScreen
import com.example.ui.screens.invoices.InvoiceQuotationScreen
import com.example.ui.screens.orders.OrdersScreen
import com.example.ui.screens.payments.PaymentsReceiptScreen
import com.example.ui.screens.recyclebin.RecycleBinScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.search.GlobalSearchScreen
import com.example.ui.screens.settings.SettingsControlCenterScreen
import com.example.ui.screens.whatsapp.WhatsAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WaqarGold
import com.example.ui.theme.WaqarNavy
import com.example.ui.viewmodel.WaqarViewModel
import com.example.ui.viewmodel.WaqarViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WaqarInquiryApp
        val factory = WaqarViewModelFactory(app.repository)

        setContent {
            val viewModel: WaqarViewModel = viewModel(factory = factory)
            val isDarkState by viewModel.isDarkMode.collectAsState()
            val darkTheme = isDarkState ?: androidx.compose.foundation.isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = darkTheme, dynamicColor = false) {
                val navController = rememberNavController()
                val session by viewModel.sessionState.collectAsState()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val isAuthRoute = currentRoute == NavRoutes.LOGIN || currentRoute == NavRoutes.CHANGE_PASSWORD
                val showBottomNav = !isAuthRoute && session.isLoggedIn

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) {
                            NavigationBar(
                                containerColor = WaqarNavy,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == NavRoutes.DASHBOARD,
                                    onClick = {
                                        navController.navigate(NavRoutes.DASHBOARD) {
                                            popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                    label = { Text("Dashboard") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WaqarGold,
                                        selectedTextColor = WaqarGold,
                                        unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        indicatorColor = WaqarNavy
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == NavRoutes.INQUIRIES,
                                    onClick = {
                                        navController.navigate(NavRoutes.INQUIRIES) {
                                            launchSingleTop = true
                                        }
                                    },
                                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Inquiries") },
                                    label = { Text("Inquiries") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WaqarGold,
                                        selectedTextColor = WaqarGold,
                                        unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        indicatorColor = WaqarNavy
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == NavRoutes.ADMISSIONS,
                                    onClick = {
                                        navController.navigate(NavRoutes.ADMISSIONS) {
                                            launchSingleTop = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.School, contentDescription = "Admissions") },
                                    label = { Text("Admissions") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WaqarGold,
                                        selectedTextColor = WaqarGold,
                                        unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        indicatorColor = WaqarNavy
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == NavRoutes.ORDERS,
                                    onClick = {
                                        navController.navigate(NavRoutes.ORDERS) {
                                            launchSingleTop = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Orders") },
                                    label = { Text("Orders") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WaqarGold,
                                        selectedTextColor = WaqarGold,
                                        unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        indicatorColor = WaqarNavy
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == NavRoutes.PAYMENTS,
                                    onClick = {
                                        navController.navigate(NavRoutes.PAYMENTS) {
                                            launchSingleTop = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Ledger") },
                                    label = { Text("Ledger") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = WaqarGold,
                                        selectedTextColor = WaqarGold,
                                        unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
                                        indicatorColor = WaqarNavy
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (session.isLoggedIn) NavRoutes.DASHBOARD else NavRoutes.LOGIN,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavRoutes.LOGIN) {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate(NavRoutes.DASHBOARD) {
                                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(NavRoutes.DASHBOARD) {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToInquiries = { navController.navigate(NavRoutes.INQUIRIES) },
                                onNavigateToCustomers = { navController.navigate(NavRoutes.CUSTOMERS) },
                                onNavigateToAdmissions = { navController.navigate(NavRoutes.ADMISSIONS) },
                                onNavigateToOrders = { navController.navigate(NavRoutes.ORDERS) },
                                onNavigateToPayments = { navController.navigate(NavRoutes.PAYMENTS) },
                                onNavigateToInvoices = { navController.navigate(NavRoutes.INVOICES) },
                                onNavigateToWhatsApp = { navController.navigate(NavRoutes.WHATSAPP) },
                                onNavigateToAskAi = { navController.navigate(NavRoutes.ASK_AI) },
                                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                                onNavigateToSearch = { navController.navigate(NavRoutes.GLOBAL_SEARCH) }
                            )
                        }

                        composable(NavRoutes.INQUIRIES) {
                            InquiryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.CUSTOMERS) {
                            Customer360Screen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.ADMISSIONS) {
                            AdmissionScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.ORDERS) {
                            OrdersScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.PAYMENTS) {
                            PaymentsReceiptScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.INVOICES) {
                            InvoiceQuotationScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.CALENDAR) {
                            CalendarRemindersScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.WHATSAPP) {
                            WhatsAppScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.GLOBAL_SEARCH) {
                            GlobalSearchScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.REPORTS) {
                            ReportsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.RECYCLE_BIN) {
                            RecycleBinScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.AUDIT_LOG) {
                            AuditLogScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.ASK_AI) {
                            AskAiScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.SETTINGS) {
                            SettingsControlCenterScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToRecycleBin = { navController.navigate(NavRoutes.RECYCLE_BIN) },
                                onNavigateToAuditLogs = { navController.navigate(NavRoutes.AUDIT_LOG) },
                                onNavigateToWhatsAppLimits = { navController.navigate(NavRoutes.WHATSAPP) },
                                onLoggedOut = {
                                    navController.navigate(NavRoutes.LOGIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
