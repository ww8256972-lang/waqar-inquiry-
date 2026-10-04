package com.example.ui.screens.recyclebin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@Composable
fun RecycleBinScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val deletedCustomers by viewModel.deletedCustomers.collectAsState()
    val deletedInquiries by viewModel.deletedInquiries.collectAsState()
    val deletedAdmissions by viewModel.deletedAdmissions.collectAsState()
    val deletedOrders by viewModel.deletedOrders.collectAsState()

    var selectedTab by remember { mutableStateOf("Inquiries") }
    var confirmPermanentDeleteId by remember { mutableStateOf<Pair<String, String>?>(null) } // type to id

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Recycle Bin & Safe Recovery",
                subtitle = "Zero customer data loss policy",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight)
        ) {
            TabRow(
                selectedTabIndex = when (selectedTab) {
                    "Inquiries" -> 0
                    "Customers" -> 1
                    "Admissions" -> 2
                    else -> 3
                },
                containerColor = Color.White
            ) {
                Tab(selected = selectedTab == "Inquiries", onClick = { selectedTab = "Inquiries" }, text = { Text("Inquiries (${deletedInquiries.size})") })
                Tab(selected = selectedTab == "Customers", onClick = { selectedTab = "Customers" }, text = { Text("Customers (${deletedCustomers.size})") })
                Tab(selected = selectedTab == "Admissions", onClick = { selectedTab = "Admissions" }, text = { Text("Admissions (${deletedAdmissions.size})") })
                Tab(selected = selectedTab == "Orders", onClick = { selectedTab = "Orders" }, text = { Text("Orders (${deletedOrders.size})") })
            }

            Spacer(Modifier.height(8.dp))

            when (selectedTab) {
                "Inquiries" -> {
                    if (deletedInquiries.isEmpty()) {
                        EmptyRecycleView("No deleted inquiry records")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(deletedInquiries, key = { it.id }) { inq ->
                                DeletedItemCard(
                                    title = inq.customerName,
                                    subtitle = "${inq.serviceOrProject} • Deleted by ${inq.deletedBy ?: "Admin"}",
                                    onRestore = { viewModel.restoreInquiry(inq.id) },
                                    onPermanentDelete = { confirmPermanentDeleteId = "INQUIRY" to inq.id }
                                )
                            }
                        }
                    }
                }
                "Customers" -> {
                    if (deletedCustomers.isEmpty()) {
                        EmptyRecycleView("No deleted customers")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(deletedCustomers, key = { it.id }) { cust ->
                                DeletedItemCard(
                                    title = cust.name,
                                    subtitle = "${cust.phone} • Deleted by ${cust.deletedBy ?: "Admin"}",
                                    onRestore = { viewModel.restoreCustomer(cust.id) },
                                    onPermanentDelete = { confirmPermanentDeleteId = "CUSTOMER" to cust.id }
                                )
                            }
                        }
                    }
                }
                "Admissions" -> {
                    if (deletedAdmissions.isEmpty()) {
                        EmptyRecycleView("No deleted admissions")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(deletedAdmissions, key = { it.id }) { adm ->
                                DeletedItemCard(
                                    title = adm.applicantName,
                                    subtitle = "Reg: ${adm.registrationNumber} • ${adm.courseOrService}",
                                    onRestore = { viewModel.restoreAdmission(adm.id) },
                                    onPermanentDelete = { confirmPermanentDeleteId = "ADMISSION" to adm.id }
                                )
                            }
                        }
                    }
                }
                "Orders" -> {
                    if (deletedOrders.isEmpty()) {
                        EmptyRecycleView("No deleted orders")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(deletedOrders, key = { it.id }) { ord ->
                                DeletedItemCard(
                                    title = ord.titleOrService,
                                    subtitle = "Client: ${ord.customerName}",
                                    onRestore = { viewModel.restoreOrder(ord.id) },
                                    onPermanentDelete = { confirmPermanentDeleteId = "ORDER" to ord.id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    confirmPermanentDeleteId?.let { (type, id) ->
        ConfirmActionDialog(
            title = "Permanent Purge?",
            message = "This will irreversibly remove the record from local storage. This action cannot be undone.",
            confirmText = "Purge Permanently",
            isDestructive = true,
            onConfirm = {
                when (type) {
                    "INQUIRY" -> viewModel.permanentDeleteInquiry(id)
                    "CUSTOMER" -> viewModel.permanentDeleteCustomer(id)
                }
                confirmPermanentDeleteId = null
            },
            onDismiss = { confirmPermanentDeleteId = null }
        )
    }
}

@Composable
fun DeletedItemCard(
    title: String,
    subtitle: String,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WaqarNavy)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Row {
                FilledTonalButton(
                    onClick = onRestore,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Restore", fontSize = 11.sp)
                }
                Spacer(Modifier.width(6.dp))
                IconButton(onClick = onPermanentDelete) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Purge", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun EmptyRecycleView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(message, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        }
    }
}
