package com.example.ui.screens.whatsapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.whatsapp.WhatsAppService
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@Composable
fun WhatsAppScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val quota by viewModel.whatsAppQuota.collectAsState()
    var targetPhone by remember { mutableStateOf("") }
    var selectedTemplateText by remember { mutableStateOf(WhatsAppService.TEMPLATE_DEFAULT_WELCOME) }
    var messageText by remember { mutableStateOf(WhatsAppService.TEMPLATE_DEFAULT_WELCOME) }
    var showLimitConfigDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "WhatsApp Engine & Limits",
                subtitle = "Official intent dispatch & triggers",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quota & Rate Limit Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WaqarNavy)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("DAILY DISPATCH CAPACITY", color = WaqarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("$quota Messages Available", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showLimitConfigDialog = true }) {
                                Icon(Icons.Default.Tune, contentDescription = "Configure Limits", tint = Color.White)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Configured protection mechanism preventing rate limit bans and spam flags.",
                            color = WaqarSilver,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Quick Template Picker
            item {
                Text("Select Official Waqar Template", fontWeight = FontWeight.Bold, color = WaqarNavy)
                Spacer(Modifier.height(8.dp))

                val templates = listOf(
                    "Default Welcome" to WhatsAppService.TEMPLATE_DEFAULT_WELCOME,
                    "Verification Done" to WhatsAppService.TEMPLATE_VERIFICATION_COMPLETE,
                    "Payment Receipt" to WhatsAppService.TEMPLATE_PAYMENT_RECEIPT,
                    "Order Update" to WhatsAppService.TEMPLATE_ORDER_STATUS
                )

                templates.forEach { (label, text) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedTemplateText == text) Color(0xFFE8F5E9) else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, fontWeight = FontWeight.Bold, color = WaqarNavy)
                                TextButton(onClick = {
                                    selectedTemplateText = text
                                    messageText = text
                                }) {
                                    Text("Apply Template")
                                }
                            }
                            Text(text, fontSize = 12.sp, color = Color.DarkGray, maxLines = 3)
                        }
                    }
                }
            }

            // Interactive Message Composer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Live Message Dispatcher", fontWeight = FontWeight.Bold, color = WaqarNavy)

                        OutlinedTextField(
                            value = targetPhone,
                            onValueChange = { targetPhone = it },
                            label = { Text("Recipient Phone Number (with Country Code)") },
                            placeholder = { Text("e.g. 919876543210") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            label = { Text("Message Text") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 8
                        )

                        Button(
                            onClick = {
                                if (targetPhone.isBlank()) {
                                    Toast.makeText(context, "Please enter recipient phone number", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.openWhatsAppChat(targetPhone, messageText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WaqarGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Launch Official WhatsApp", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showLimitConfigDialog) {
        var newLimitStr by remember { mutableStateOf("100") }
        AlertDialog(
            onDismissRequest = { showLimitConfigDialog = false },
            title = { Text("Configure Daily WhatsApp Limit") },
            text = {
                Column {
                    Text("Prevent quota depletion by setting a threshold.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newLimitStr,
                        onValueChange = { newLimitStr = it },
                        label = { Text("Daily Quota") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val lim = newLimitStr.toIntOrNull() ?: 100
                    viewModel.setDailyLimit(lim)
                    showLimitConfigDialog = false
                    Toast.makeText(context, "Daily limit set to $lim", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitConfigDialog = false }) { Text("Cancel") }
            }
        )
    }
}
