package com.example.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.InquiryRecordEntity
import com.example.data.whatsapp.WhatsAppService
import com.example.ui.components.InquiryItemCard
import com.example.ui.components.RupeeText
import com.example.ui.components.StatusBadge
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.components.WhatsAppSendDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@Composable
fun DashboardScreen(
    viewModel: WaqarViewModel,
    onNavigateToInquiries: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToAdmissions: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToWhatsApp: () -> Unit,
    onNavigateToAskAi: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val context = LocalContext.current
    val inquiries by viewModel.inquiries.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val admissions by viewModel.admissions.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val quota by viewModel.whatsAppQuota.collectAsState()
    val session by viewModel.sessionState.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val isFlowerAnimEnabled by viewModel.isFlowerAnimationEnabled.collectAsState()

    var activeDialogInquiry by remember { mutableStateOf<InquiryRecordEntity?>(null) }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "WAQAR WEBSITE INQUIRY",
                subtitle = "Welcome back, ${session.username} (${session.role})",
                syncState = syncState,
                actions = {
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (isDark == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = WaqarGold
                        )
                    }
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToAskAi) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Ask AI", tint = WaqarGold)
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight)
        ) {
            // Background Floating Flower Petals
            if (isFlowerAnimEnabled) {
                com.example.ui.components.FloatingFlowerPetals(modifier = Modifier.fillMaxSize(), petalCount = 24)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Scrolling Flower Animation Banner
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Waqar Blossom Garland",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { viewModel.toggleFlowerAnimation() },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    if (isFlowerAnimEnabled) "Petals: On" else "Petals: Off",
                                    fontSize = 11.sp,
                                    color = WaqarSlate
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        com.example.ui.components.ScrollingFlowerBanner()
                    }
                }
            // Cloud Sync & Network status strip
            item {
                Surface(
                    color = if (syncState.isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (syncState.isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (syncState.isOnline) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (syncState.isOnline) "System Online • Sync Active" else "Offline Mode • Changes Saved",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (syncState.isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.syncNow { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (syncState.isSyncing) "Syncing..." else "Sync Now",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // WhatsApp Automation Announcement Card with the exact required copy
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WaqarNavy)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(WaqarGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "WHATSAPP AUTOMATIC ENGINE",
                                    color = WaqarGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Auto Notification Active",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$quota Left Today",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "\"${WhatsAppService.TEMPLATE_DEFAULT_WELCOME}\"",
                            color = Color(0xFFECEFF1),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToWhatsApp,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WaqarSilver),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Manage Templates & Limits", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Key Metrics Grid
            item {
                Text(
                    text = "Key Operations & Revenue",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WaqarNavy
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Total Inquiries",
                        value = "${inquiries.size}",
                        subtitle = "${inquiries.count { it.status == "Pending" }} Pending",
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        color = WaqarBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToInquiries
                    )
                    MetricCard(
                        title = "Admissions",
                        value = "${admissions.size}",
                        subtitle = "${admissions.count { it.verificationStatus == "Verified" }} Verified",
                        icon = Icons.Default.School,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAdmissions
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Active Orders",
                        value = "${orders.count { it.status != "Completed" }}",
                        subtitle = "${orders.size} Total Orders",
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToOrders
                    )
                    MetricCard(
                        title = "Collected Revenue",
                        value = "₹ " + String.format(java.util.Locale.US, "%,.0f", totalRevenue ?: 0.0),
                        subtitle = "Payment Ledger",
                        icon = Icons.Default.CurrencyRupee,
                        color = WaqarNavy,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPayments
                    )
                }
            }

            // Quick Actions Bar
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WaqarNavy
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(icon = Icons.Default.Add, label = "+ Record", onClick = onNavigateToInquiries)
                    QuickActionButton(icon = Icons.Default.School, label = "Admission", onClick = onNavigateToAdmissions)
                    QuickActionButton(icon = Icons.Default.Receipt, label = "Invoice", onClick = onNavigateToInvoices)
                    QuickActionButton(icon = Icons.Default.Payments, label = "Payment", onClick = onNavigateToPayments)
                    QuickActionButton(icon = Icons.Default.SmartToy, label = "Ask AI", onClick = onNavigateToAskAi)
                }
            }

            // Recent Inquiries
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Inquiries & Follow-ups",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WaqarNavy
                    )
                    TextButton(onClick = onNavigateToInquiries) {
                        Text("View All (${inquiries.size})", fontSize = 12.sp)
                    }
                }
            }

            if (inquiries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.Assignment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No Inquiries Recorded Yet", fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("Tap '+ Record' to register a new inquiry and trigger automatic messaging.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(inquiries.take(5)) { inquiry ->
                    InquiryItemCard(
                        inquiry = inquiry,
                        onWhatsAppClick = {
                            activeDialogInquiry = inquiry
                        },
                        onClick = onNavigateToInquiries
                    )
                }
            }
        }
    }
}

    activeDialogInquiry?.let { inquiry ->
        WhatsAppSendDialog(
            customerName = inquiry.customerName,
            phone = inquiry.phone,
            initialMessage = WhatsAppService.TEMPLATE_DEFAULT_WELCOME,
            onDismiss = { activeDialogInquiry = null },
            onSend = { text ->
                viewModel.openWhatsAppChat(inquiry.phone, text)
                activeDialogInquiry = null
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = color)
            )
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.White,
                contentColor = WaqarNavy
            )
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = WaqarNavy)
    }
}
