package com.example.ui.screens.payments

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PaymentEntity
import com.example.data.model.PaymentMethod
import com.example.data.pdf.PdfGenerator
import com.example.ui.components.RupeeText
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsReceiptScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val payments by viewModel.payments.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    var showNewPaymentDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Payment Ledger & Receipts",
                subtitle = "Total Collected: ${PdfGenerator.formatRupee(totalRevenue ?: 0.0)}",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewPaymentDialog = true },
                containerColor = WaqarNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Record Payment")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight)
        ) {
            // Total Revenue Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WaqarNavy)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("LIFETIME REVENUE COLLECTED", color = WaqarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = PdfGenerator.formatRupee(totalRevenue ?: 0.0),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${payments.size} verified transactions recorded in encrypted ledger",
                        color = WaqarSilver,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = WaqarNavy,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (payments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No payment transactions recorded yet", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(payments, key = { it.id }) { payment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                    Column {
                                        Text(payment.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaqarNavy)
                                        Text(
                                            text = "${payment.recordType} • Ref: ${payment.transactionReference.ifBlank { "N/A" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                    RupeeText(payment.amount, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E7D32))
                                }

                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${payment.paymentMethod} • ${dateFormat.format(Date(payment.paymentDate))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )

                                    Button(
                                        onClick = {
                                            try {
                                                val pdf = PdfGenerator.generatePaymentReceipt(context, payment)
                                                PdfGenerator.sharePdf(context, pdf, "Payment Receipt - ${payment.customerName}")
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WaqarBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Receipt PDF", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewPaymentDialog) {
        RecordPaymentDialog(
            onDismiss = { showNewPaymentDialog = false },
            onSave = { payment ->
                viewModel.recordPayment(payment) {
                    showNewPaymentDialog = false
                    Toast.makeText(context, "Payment recorded & receipt generated", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    onDismiss: () -> Unit,
    onSave: (PaymentEntity) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(PaymentMethod.UPI.label) }
    var reference by remember { mutableStateOf("") }
    var recordType by remember { mutableStateOf("INQUIRY") }
    var notes by remember { mutableStateOf("") }

    val methods = PaymentMethod.values().map { it.label }
    val recordTypes = listOf("INQUIRY", "ADMISSION", "ORDER", "INVOICE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment Transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Method Dropdown
                var methodExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = !methodExpanded }
                ) {
                    OutlinedTextField(
                        value = method,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        methods.forEach { m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = { method = m; methodExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("UTR / Transaction Reference (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Payment Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (customerName.isBlank() || amt <= 0.0) return@Button
                    val p = PaymentEntity(
                        id = UUID.randomUUID().toString(),
                        recordType = recordType,
                        recordId = UUID.randomUUID().toString(),
                        customerId = UUID.randomUUID().toString(),
                        customerName = customerName.trim(),
                        amount = amt,
                        paymentMethod = method,
                        transactionReference = reference.trim(),
                        notes = notes.trim()
                    )
                    onSave(p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy)
            ) {
                Text("Confirm & Issue Receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
