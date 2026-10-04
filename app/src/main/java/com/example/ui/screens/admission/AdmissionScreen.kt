package com.example.ui.screens.admission

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
import com.example.data.local.entity.AdmissionEntity
import com.example.data.whatsapp.WhatsAppService
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel
import java.util.UUID

@Composable
fun AdmissionScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val admissions by viewModel.admissions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") }
    var showDialog by remember { mutableStateOf(false) }
    var editingAdmission by remember { mutableStateOf<AdmissionEntity?>(null) }
    var activeWhatsAppDialog by remember { mutableStateOf<AdmissionEntity?>(null) }
    var confirmDelete by remember { mutableStateOf<AdmissionEntity?>(null) }

    val filtered = remember(admissions, searchQuery, selectedTab) {
        admissions.filter { adm ->
            val matchQuery = searchQuery.isBlank() ||
                    adm.applicantName.contains(searchQuery, ignoreCase = true) ||
                    adm.registrationNumber.contains(searchQuery, ignoreCase = true) ||
                    adm.courseOrService.contains(searchQuery, ignoreCase = true)
            val matchTab = when (selectedTab) {
                "Verified" -> adm.verificationStatus.equals("Verified", ignoreCase = true)
                "Pending" -> adm.verificationStatus.equals("Pending", ignoreCase = true) || adm.verificationStatus.equals("Under Review", ignoreCase = true)
                else -> true
            }
            matchQuery && matchTab
        }
    }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Admission & Verification",
                subtitle = "${admissions.size} candidate admissions",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingAdmission = null
                    showDialog = true
                },
                containerColor = WaqarNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Admission")
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
                    placeholder = { Text("Search candidate name or reg number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = when (selectedTab) {
                        "Pending" -> 1
                        "Verified" -> 2
                        else -> 0
                    },
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedTab == "All",
                        onClick = { selectedTab = "All" },
                        text = { Text("All (${admissions.size})") }
                    )
                    Tab(
                        selected = selectedTab == "Pending",
                        onClick = { selectedTab = "Pending" },
                        text = { Text("Verification Pending (${admissions.count { it.verificationStatus != "Verified" }})") }
                    )
                    Tab(
                        selected = selectedTab == "Verified",
                        onClick = { selectedTab = "Verified" },
                        text = { Text("Verified (${admissions.count { it.verificationStatus == "Verified" }})") }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No admission records found", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { admission ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingAdmission = admission
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
                                        Text(
                                            text = admission.applicantName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = WaqarNavy
                                        )
                                        Text(
                                            text = "Reg: ${admission.registrationNumber} • ${admission.courseOrService}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                    StatusBadge(admission.verificationStatus)
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Admission Fee: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    RupeeText(admission.cost, style = MaterialTheme.typography.bodySmall, color = WaqarBlue)

                                    if (admission.remainingAmount > 0) {
                                        Spacer(Modifier.width(12.dp))
                                        Text("Balance: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        RupeeText(admission.remainingAmount, style = MaterialTheme.typography.bodySmall, color = Color.Red)
                                    }
                                }

                                if (admission.documentNames.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "Documents: ${admission.documentNames}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WaqarSlate
                                    )
                                }

                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = Color(0xFFEEEEEE))
                                Spacer(Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (admission.verificationStatus != "Verified") {
                                        Button(
                                            onClick = {
                                                viewModel.saveAdmission(
                                                    admission.copy(verificationStatus = "Verified"),
                                                    notifyCandidate = true
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Mark Verified", fontSize = 12.sp)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                    }

                                    IconButton(
                                        onClick = { activeWhatsAppDialog = admission },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = WaqarGreen)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = "Send WhatsApp")
                                    }

                                    IconButton(
                                        onClick = {
                                            editingAdmission = admission
                                            showDialog = true
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = WaqarBlue)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }

                                    IconButton(
                                        onClick = { confirmDelete = admission },
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

    if (showDialog) {
        AdmissionEditDialog(
            admission = editingAdmission,
            onDismiss = { showDialog = false },
            onSave = { updated ->
                viewModel.saveAdmission(updated, notifyCandidate = updated.verificationStatus == "Verified") {
                    showDialog = false
                }
            }
        )
    }

    activeWhatsAppDialog?.let { adm ->
        val msg = "Hello ${adm.applicantName}! This is Waqar Inquiry Services regarding your admission registration (${adm.registrationNumber}) for ${adm.courseOrService}."
        WhatsAppSendDialog(
            customerName = adm.applicantName,
            phone = adm.phone,
            initialMessage = msg,
            onDismiss = { activeWhatsAppDialog = null },
            onSend = { text ->
                viewModel.openWhatsAppChat(adm.phone, text)
                activeWhatsAppDialog = null
            }
        )
    }

    confirmDelete?.let { adm ->
        ConfirmActionDialog(
            title = "Delete Admission Record?",
            message = "Candidate ${adm.applicantName}'s record will be moved to the Recycle Bin.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.softDeleteAdmission(adm.id)
                confirmDelete = null
            },
            onDismiss = { confirmDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmissionEditDialog(
    admission: AdmissionEntity?,
    onDismiss: () -> Unit,
    onSave: (AdmissionEntity) -> Unit
) {
    var name by remember { mutableStateOf(admission?.applicantName ?: "") }
    var phone by remember { mutableStateOf(admission?.phone ?: "") }
    var email by remember { mutableStateOf(admission?.email ?: "") }
    var course by remember { mutableStateOf(admission?.courseOrService ?: "") }
    var regNumber by remember { mutableStateOf(admission?.registrationNumber ?: ("ADM-" + (1000..9999).random())) }
    var costStr by remember { mutableStateOf(admission?.cost?.toString() ?: "0.0") }
    var paidStr by remember { mutableStateOf(admission?.amountPaid?.toString() ?: "0.0") }
    var verificationStatus by remember { mutableStateOf(admission?.verificationStatus ?: "Pending") }
    var docs by remember { mutableStateOf(admission?.documentNames ?: "ID Proof, Academic Records") }
    var notes by remember { mutableStateOf(admission?.notes ?: "") }

    val verifOptions = listOf("Pending", "Under Review", "Verified", "Rejected")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (admission == null) "New Admission Form" else "Edit Admission") },
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
                    label = { Text("Candidate / Applicant Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone / WhatsApp *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = it },
                    label = { Text("Registration Number *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = { Text("Course / Service Enrollment *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = costStr,
                        onValueChange = { costStr = it },
                        label = { Text("Total Fee (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paidStr,
                        onValueChange = { paidStr = it },
                        label = { Text("Paid (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Verification Dropdown
                var verifExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = verifExpanded,
                    onExpandedChange = { verifExpanded = !verifExpanded }
                ) {
                    OutlinedTextField(
                        value = verificationStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Verification Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = verifExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = verifExpanded,
                        onDismissRequest = { verifExpanded = false }
                    ) {
                        verifOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    verificationStatus = opt
                                    verifExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = docs,
                    onValueChange = { docs = it },
                    label = { Text("Attached Documents") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) return@Button
                    val cost = costStr.toDoubleOrNull() ?: 0.0
                    val paid = paidStr.toDoubleOrNull() ?: 0.0
                    val updated = AdmissionEntity(
                        id = admission?.id ?: UUID.randomUUID().toString(),
                        customerId = admission?.customerId ?: UUID.randomUUID().toString(),
                        applicantName = name.trim(),
                        phone = phone.trim(),
                        whatsapp = phone.trim(),
                        email = email.trim(),
                        address = "",
                        courseOrService = course.trim().ifBlank { "General Course" },
                        registrationNumber = regNumber.trim(),
                        verificationStatus = verificationStatus,
                        cost = cost,
                        amountPaid = paid,
                        remainingAmount = (cost - paid).coerceAtLeast(0.0),
                        documentNames = docs.trim(),
                        notes = notes.trim(),
                        createdAt = admission?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy)
            ) {
                Text("Save Admission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
