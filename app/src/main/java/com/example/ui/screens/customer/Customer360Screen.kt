package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CustomerEntity
import com.example.data.whatsapp.WhatsAppService
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel
import java.util.UUID

@Composable
fun Customer360Screen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val inquiries by viewModel.inquiries.collectAsState()
    val admissions by viewModel.admissions.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val payments by viewModel.payments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var showWhatsAppDialog by remember { mutableStateOf(false) }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers else {
            customers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phone.contains(searchQuery, ignoreCase = true) ||
                        it.tags.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = if (selectedCustomer != null) selectedCustomer!!.name else "Customer 360° Directory",
                subtitle = if (selectedCustomer != null) "Full Client Profile & History" else "${customers.size} verified customer profiles",
                canNavigateBack = true,
                onNavigateBack = {
                    if (selectedCustomer != null) selectedCustomer = null else onNavigateBack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight)
        ) {
            if (selectedCustomer == null) {
                // Directory List
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by customer name, phone, tags...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCustomer = customer },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(WaqarNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        customer.name.take(1).uppercase(),
                                        color = WaqarGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaqarNavy)
                                    Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    if (customer.tags.isNotBlank()) {
                                        Text(customer.tags, style = MaterialTheme.typography.bodySmall, color = WaqarSlate, fontSize = 11.sp)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                // Detailed 360 View for selected customer
                val cust = selectedCustomer!!
                val custInquiries = inquiries.filter { it.customerId == cust.id || it.phone == cust.phone }
                val custAdmissions = admissions.filter { it.customerId == cust.id || it.phone == cust.phone }
                val custOrders = orders.filter { it.customerId == cust.id || it.phone == cust.phone }
                val custPayments = payments.filter { it.customerId == cust.id || it.customerName.equals(cust.name, ignoreCase = true) }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Profile Header Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WaqarNavy)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(WaqarBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            cust.name.take(1).uppercase(),
                                            color = WaqarGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column {
                                        Text(cust.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        Text(cust.phone, color = WaqarSilver, fontSize = 14.sp)
                                        if (cust.email.isNotBlank()) {
                                            Text(cust.email, color = WaqarSilver, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Account Status: ${cust.status}", color = Color.White, fontSize = 12.sp)
                                    Button(
                                        onClick = { showWhatsAppDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = WaqarGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Chat WhatsApp", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Activity Breakdown
                    item {
                        Text("Linked Inquiries (${custInquiries.size})", fontWeight = FontWeight.Bold, color = WaqarNavy)
                    }
                    if (custInquiries.isEmpty()) {
                        item { Text("No linked inquiries", color = Color.Gray, fontSize = 13.sp) }
                    } else {
                        items(custInquiries) { inq ->
                            InquiryItemCard(inquiry = inq, onWhatsAppClick = { showWhatsAppDialog = true }, onClick = {})
                        }
                    }

                    item {
                        Text("Admissions (${custAdmissions.size})", fontWeight = FontWeight.Bold, color = WaqarNavy)
                    }
                    if (custAdmissions.isEmpty()) {
                        item { Text("No admissions linked", color = Color.Gray, fontSize = 13.sp) }
                    } else {
                        items(custAdmissions) { adm ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(adm.courseOrService, fontWeight = FontWeight.Bold, color = WaqarNavy)
                                        Text("Reg: ${adm.registrationNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    StatusBadge(adm.verificationStatus)
                                }
                            }
                        }
                    }

                    item {
                        Text("Payments Timeline (${custPayments.size})", fontWeight = FontWeight.Bold, color = WaqarNavy)
                    }
                    if (custPayments.isEmpty()) {
                        item { Text("No payments recorded yet", color = Color.Gray, fontSize = 13.sp) }
                    } else {
                        items(custPayments) { p ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(p.paymentMethod, fontWeight = FontWeight.Bold, color = WaqarNavy)
                                        Text("Ref: ${p.transactionReference}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    RupeeText(p.amount, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWhatsAppDialog && selectedCustomer != null) {
        val cust = selectedCustomer!!
        WhatsAppSendDialog(
            customerName = cust.name,
            phone = cust.phone,
            initialMessage = "Hello ${cust.name}! This is Waqar Website Inquiry reaching out regarding your account.",
            onDismiss = { showWhatsAppDialog = false },
            onSend = { text ->
                viewModel.openWhatsAppChat(cust.phone, text)
                showWhatsAppDialog = false
            }
        )
    }
}
