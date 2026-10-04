package com.example.ui.screens.invoices

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import com.example.data.local.entity.InvoiceEntity
import com.example.data.pdf.PdfGenerator
import com.example.ui.components.RupeeText
import com.example.ui.components.StatusBadge
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel
import java.util.UUID

@Composable
fun InvoiceQuotationScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val invoices by viewModel.invoices.collectAsState()
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Tax Invoices & Billing",
                subtitle = "${invoices.size} generated invoices",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateInvoiceDialog = true },
                containerColor = WaqarNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Invoice")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight)
        ) {
            if (invoices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No Invoices Generated Yet", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Tap 'Generate Invoice' to create an official tax invoice with PDF.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(invoices, key = { it.id }) { invoice ->
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
                                        Text(invoice.customerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaqarNavy)
                                        Text("Invoice #${invoice.invoiceNumber}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    }
                                    StatusBadge(invoice.paymentStatus)
                                }

                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Grand Total: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        RupeeText(invoice.grandTotal, style = MaterialTheme.typography.titleMedium, color = WaqarBlue)
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val pdf = PdfGenerator.generateInvoicePdf(context, invoice)
                                                PdfGenerator.sharePdf(context, pdf, "Invoice - ${invoice.invoiceNumber}")
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("PDF Invoice", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateInvoiceDialog) {
        CreateInvoiceDialog(
            onDismiss = { showCreateInvoiceDialog = false },
            onSave = { invoice ->
                viewModel.saveInvoice(invoice) {
                    showCreateInvoiceDialog = false
                    Toast.makeText(context, "Invoice #${invoice.invoiceNumber} created", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun CreateInvoiceDialog(
    onDismiss: () -> Unit,
    onSave: (InvoiceEntity) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var subtotalStr by remember { mutableStateOf("") }
    var discountStr by remember { mutableStateOf("0.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Tax Invoice") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Billed To (Client Name) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Client Phone *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Billing Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtotalStr,
                    onValueChange = { subtotalStr = it },
                    label = { Text("Subtotal (₹) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = discountStr,
                    onValueChange = { discountStr = it },
                    label = { Text("Discount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sub = subtotalStr.toDoubleOrNull() ?: 0.0
                    val disc = discountStr.toDoubleOrNull() ?: 0.0
                    if (customerName.isBlank() || sub <= 0.0) return@Button
                    val tax = sub * 0.18
                    val total = sub + tax - disc
                    val invoice = InvoiceEntity(
                        id = UUID.randomUUID().toString(),
                        invoiceNumber = "INV-" + (1000..9999).random(),
                        customerId = UUID.randomUUID().toString(),
                        customerName = customerName.trim(),
                        customerPhone = phone.trim(),
                        customerAddress = address.trim(),
                        subtotal = sub,
                        taxRate = 18.0,
                        taxAmount = tax,
                        discount = disc,
                        grandTotal = total,
                        amountPaid = 0.0,
                        remainingAmount = total
                    )
                    onSave(invoice)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy)
            ) {
                Text("Generate & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
