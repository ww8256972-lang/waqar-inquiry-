package com.example.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.local.entity.OrderEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel
import java.util.UUID

@Composable
fun OrdersScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingOrder by remember { mutableStateOf<OrderEntity?>(null) }
    var confirmDelete by remember { mutableStateOf<OrderEntity?>(null) }
    var activeWhatsAppDialog by remember { mutableStateOf<OrderEntity?>(null) }

    val filtered = remember(orders, selectedTab, searchQuery) {
        orders.filter { order ->
            val matchQuery = searchQuery.isBlank() ||
                    order.customerName.contains(searchQuery, ignoreCase = true) ||
                    order.titleOrService.contains(searchQuery, ignoreCase = true)
            val matchTab = when (selectedTab) {
                "Pending" -> order.status.equals("Pending", ignoreCase = true)
                "Processing" -> order.status.equals("Processing", ignoreCase = true)
                "Completed" -> order.status.equals("Completed", ignoreCase = true)
                else -> true
            }
            matchQuery && matchTab
        }
    }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Order Management",
                subtitle = "${orders.count { it.status == "Completed" }} completed • ${orders.count { it.status != "Completed" }} in progress",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingOrder = null
                    showDialog = true
                },
                containerColor = WaqarNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Create Order")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search orders by client or service...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        "Pending" -> 1
                        "Processing" -> 2
                        "Completed" -> 3
                        else -> 0
                    },
                    containerColor = Color.Transparent
                ) {
                    Tab(selected = selectedTab == "All", onClick = { selectedTab = "All" }, text = { Text("All (${orders.size})") })
                    Tab(selected = selectedTab == "Pending", onClick = { selectedTab = "Pending" }, text = { Text("Pending (${orders.count { it.status == "Pending" }})") })
                    Tab(selected = selectedTab == "Processing", onClick = { selectedTab = "Processing" }, text = { Text("Processing (${orders.count { it.status == "Processing" }})") })
                    Tab(selected = selectedTab == "Completed", onClick = { selectedTab = "Completed" }, text = { Text("Completed (${orders.count { it.status == "Completed" }})") })
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders found", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingOrder = order
                                    showDialog = true
                                },
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(order.titleOrService, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaqarNavy)
                                        Text("Client: ${order.customerName} • ${order.phone}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    }
                                    StatusBadge(order.status)
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Cost: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    RupeeText(order.totalCost, style = MaterialTheme.typography.bodySmall, color = WaqarBlue)

                                    if (order.remainingAmount > 0) {
                                        Spacer(Modifier.width(12.dp))
                                        Text("Balance: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        RupeeText(order.remainingAmount, style = MaterialTheme.typography.bodySmall, color = Color.Red)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFFEEEEEE))
                                Spacer(Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quick status progression
                                    Row {
                                        if (order.status == "Pending") {
                                            FilledTonalButton(
                                                onClick = { viewModel.saveOrder(order.copy(status = "Processing")) },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Start Work", fontSize = 11.sp)
                                            }
                                        } else if (order.status == "Processing") {
                                            Button(
                                                onClick = { viewModel.saveOrder(order.copy(status = "Completed")) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Complete", fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { activeWhatsAppDialog = order },
                                            colors = IconButtonDefaults.iconButtonColors(contentColor = WaqarGreen)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "WhatsApp")
                                        }
                                        IconButton(
                                            onClick = { editingOrder = order; showDialog = true },
                                            colors = IconButtonDefaults.iconButtonColors(contentColor = WaqarBlue)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                                        }
                                        IconButton(
                                            onClick = { confirmDelete = order },
                                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        OrderEditDialog(
            order = editingOrder,
            onDismiss = { showDialog = false },
            onSave = { updated ->
                viewModel.saveOrder(updated) { showDialog = false }
            }
        )
    }

    activeWhatsAppDialog?.let { ord ->
        val msg = "Hello ${ord.customerName}! Your order '${ord.titleOrService}' status is currently '${ord.status}'. Best regards, Waqar Website Inquiry."
        WhatsAppSendDialog(
            customerName = ord.customerName,
            phone = ord.phone,
            initialMessage = msg,
            onDismiss = { activeWhatsAppDialog = null },
            onSend = { text ->
                viewModel.openWhatsAppChat(ord.phone, text)
                activeWhatsAppDialog = null
            }
        )
    }

    confirmDelete?.let { ord ->
        ConfirmActionDialog(
            title = "Delete Order?",
            message = "Order '${ord.titleOrService}' will be moved to the Recycle Bin.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.softDeleteOrder(ord.id)
                confirmDelete = null
            },
            onDismiss = { confirmDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEditDialog(
    order: OrderEntity?,
    onDismiss: () -> Unit,
    onSave: (OrderEntity) -> Unit
) {
    var title by remember { mutableStateOf(order?.titleOrService ?: "") }
    var customerName by remember { mutableStateOf(order?.customerName ?: "") }
    var phone by remember { mutableStateOf(order?.phone ?: "") }
    var costStr by remember { mutableStateOf(order?.totalCost?.toString() ?: "0.0") }
    var paidStr by remember { mutableStateOf(order?.amountPaid?.toString() ?: "0.0") }
    var status by remember { mutableStateOf(order?.status ?: "Pending") }
    var details by remember { mutableStateOf(order?.orderDetails ?: "") }

    val statusOpts = listOf("Pending", "Processing", "Completed", "Cancelled")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (order == null) "New Order" else "Edit Order") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Service / Order Title *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Client Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Client Phone *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Cost (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paidStr,
                        onValueChange = { paidStr = it },
                        label = { Text("Paid (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                var statusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Order Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOpts.forEach { st ->
                            DropdownMenuItem(text = { Text(st) }, onClick = { status = st; statusExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Order Specifications & Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || customerName.isBlank()) return@Button
                    val cost = costStr.toDoubleOrNull() ?: 0.0
                    val paid = paidStr.toDoubleOrNull() ?: 0.0
                    val updated = OrderEntity(
                        id = order?.id ?: UUID.randomUUID().toString(),
                        customerId = order?.customerId ?: UUID.randomUUID().toString(),
                        customerName = customerName.trim(),
                        phone = phone.trim(),
                        titleOrService = title.trim(),
                        orderDetails = details.trim(),
                        status = status,
                        totalCost = cost,
                        amountPaid = paid,
                        remainingAmount = (cost - paid).coerceAtLeast(0.0),
                        createdAt = order?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy)
            ) {
                Text("Save Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
