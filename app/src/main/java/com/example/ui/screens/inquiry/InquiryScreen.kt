package com.example.ui.screens.inquiry

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
import com.example.data.local.entity.InquiryRecordEntity
import com.example.data.model.Priority
import com.example.data.model.RecordStatus
import com.example.data.whatsapp.WhatsAppService
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val inquiries by viewModel.inquiries.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<InquiryRecordEntity?>(null) }
    var activeWhatsAppDialog by remember { mutableStateOf<InquiryRecordEntity?>(null) }
    var confirmDeleteRecord by remember { mutableStateOf<InquiryRecordEntity?>(null) }

    val filteredList = remember(inquiries, searchQuery, selectedFilter) {
        inquiries.filter { item ->
            val matchQuery = searchQuery.isBlank() ||
                    item.customerName.contains(searchQuery, ignoreCase = true) ||
                    item.phone.contains(searchQuery, ignoreCase = true) ||
                    item.serviceOrProject.contains(searchQuery, ignoreCase = true)
            val matchFilter = selectedFilter == "All" || item.status.equals(selectedFilter, ignoreCase = true)
            matchQuery && matchFilter
        }
    }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Inquiries & Records",
                subtitle = "${inquiries.size} active records",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingRecord = null
                    showEditDialog = true
                },
                containerColor = WaqarNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("+ New Record")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight)
        ) {
            // Search and Filter Bar
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, phone, or service...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(10.dp))

                // Status Filter Chips
                val filterOptions = listOf("All", "New", "Pending", "Under Review", "Verification Pending", "Verification Complete", "Admission Pending", "Order Completed")
                ScrollableTabRow(
                    selectedTabIndex = filterOptions.indexOf(selectedFilter).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    divider = {},
                    containerColor = Color.Transparent
                ) {
                    filterOptions.forEach { opt ->
                        FilterChip(
                            selected = selectedFilter == opt,
                            onClick = { selectedFilter = opt },
                            label = { Text(opt, fontSize = 12.sp) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No matching records found", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingRecord = record
                                    showEditDialog = true
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
                                        Text(
                                            text = record.customerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = WaqarNavy
                                        )
                                        Text(
                                            text = "ID: ${record.id.take(8).uppercase()} • ${record.serviceOrProject}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                    StatusBadge(record.status)
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(record.phone, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)

                                    Spacer(Modifier.width(16.dp))
                                    Text("Cost: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    RupeeText(record.finalCost, style = MaterialTheme.typography.bodySmall, color = WaqarBlue)

                                    if (record.remainingAmount > 0) {
                                        Spacer(Modifier.width(12.dp))
                                        Text("Due: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        RupeeText(record.remainingAmount, style = MaterialTheme.typography.bodySmall, color = Color.Red)
                                    }
                                }

                                if (record.notes.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Note: ${record.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        maxLines = 2
                                    )
                                }

                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFFEEEEEE))
                                Spacer(Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { activeWhatsAppDialog = record },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = WaqarGreen)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "WhatsApp")
                                    }

                                    IconButton(
                                        onClick = {
                                            editingRecord = record
                                            showEditDialog = true
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = WaqarBlue)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }

                                    IconButton(
                                        onClick = { confirmDeleteRecord = record },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Move to Recycle Bin")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New/Edit Record Dialog
    if (showEditDialog) {
        InquiryEditDialog(
            record = editingRecord,
            onDismiss = { showEditDialog = false },
            onSave = { updatedRecord ->
                viewModel.saveInquiry(updatedRecord) {
                    showEditDialog = false
                }
            }
        )
    }

    // WhatsApp Send Dialog
    activeWhatsAppDialog?.let { record ->
        val renderedMsg = viewModel.whatsAppQuota.value.let {
            WhatsAppService.TEMPLATE_DEFAULT_WELCOME
        }
        WhatsAppSendDialog(
            customerName = record.customerName,
            phone = record.phone,
            initialMessage = renderedMsg,
            onDismiss = { activeWhatsAppDialog = null },
            onSend = { text ->
                viewModel.openWhatsAppChat(record.phone, text)
                activeWhatsAppDialog = null
            }
        )
    }

    // Confirm Delete Dialog
    confirmDeleteRecord?.let { record ->
        ConfirmActionDialog(
            title = "Move to Recycle Bin?",
            message = "Inquiry for ${record.customerName} will be moved to the Recycle Bin. You can restore it anytime.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.softDeleteInquiry(record.id)
                confirmDeleteRecord = null
            },
            onDismiss = { confirmDeleteRecord = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryEditDialog(
    record: InquiryRecordEntity?,
    onDismiss: () -> Unit,
    onSave: (InquiryRecordEntity) -> Unit
) {
    var name by remember { mutableStateOf(record?.customerName ?: "") }
    var phone by remember { mutableStateOf(record?.phone ?: "") }
    var email by remember { mutableStateOf(record?.email ?: "") }
    var service by remember { mutableStateOf(record?.serviceOrProject ?: "") }
    var details by remember { mutableStateOf(record?.inquiryDetails ?: "") }
    var estimatedCostStr by remember { mutableStateOf(record?.estimatedCost?.toString() ?: "0.0") }
    var finalCostStr by remember { mutableStateOf(record?.finalCost?.toString() ?: "0.0") }
    var amountPaidStr by remember { mutableStateOf(record?.amountPaid?.toString() ?: "0.0") }
    var status by remember { mutableStateOf(record?.status ?: "New") }
    var priority by remember { mutableStateOf(record?.priority ?: "MEDIUM") }
    var notes by remember { mutableStateOf(record?.notes ?: "") }
    var assignedStaff by remember { mutableStateOf(record?.assignedStaff ?: "Waqar") }

    val allStatuses = RecordStatus.values().map { it.label }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (record == null) "New Inquiry Record" else "Edit Inquiry Record")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone / WhatsApp Number *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text("Service / Project Title *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Inquiry Details") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = finalCostStr,
                        onValueChange = { finalCostStr = it },
                        label = { Text("Final Cost (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = amountPaidStr,
                        onValueChange = { amountPaidStr = it },
                        label = { Text("Paid (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Status Dropdown
                var statusExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Workflow Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        allStatuses.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    status = st
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Internal Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) return@Button
                    val cost = finalCostStr.toDoubleOrNull() ?: 0.0
                    val paid = amountPaidStr.toDoubleOrNull() ?: 0.0
                    val updated = InquiryRecordEntity(
                        id = record?.id ?: UUID.randomUUID().toString(),
                        customerId = record?.customerId ?: UUID.randomUUID().toString(),
                        customerName = name.trim(),
                        phone = phone.trim(),
                        whatsapp = phone.trim(),
                        email = email.trim(),
                        inquiryDetails = details.trim(),
                        serviceOrProject = service.trim().ifBlank { "General Inquiry" },
                        estimatedCost = cost,
                        finalCost = cost,
                        amountPaid = paid,
                        remainingAmount = (cost - paid).coerceAtLeast(0.0),
                        status = status,
                        priority = priority,
                        assignedStaff = assignedStaff,
                        notes = notes.trim(),
                        createdAt = record?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy)
            ) {
                Text("Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
