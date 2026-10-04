package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.data.pdf.PdfGenerator
import com.example.data.sync.SyncState
import com.example.data.whatsapp.WhatsAppService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaqarTopAppBar(
    title: String,
    subtitle: String? = null,
    syncState: SyncState? = null,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = WaqarNavy,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 4.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WaqarBlue)
                        .border(1.5.dp, WaqarGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "W",
                        color = WaqarGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }
        },
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (syncState != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (syncState.isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800))
                        )
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WaqarSilver,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = actions
    )
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "verified", "admission complete", "order completed", "payment completed", "paid", "completed" ->
            Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "pending", "verification pending", "admission pending", "order pending", "payment pending", "unpaid" ->
            Color(0xFFFFF3E0) to Color(0xFFE65100)
        "order processing", "under review", "partial" ->
            Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "new" ->
            Color(0xFFEDE7F6) to Color(0xFF512DA8)
        "cancelled", "rejected" ->
            Color(0xFFFFEBEE) to Color(0xFFC62828)
        else ->
            Color(0xFFECEFF1) to Color(0xFF37474F)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RupeeText(
    amount: Double,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text = PdfGenerator.formatRupee(amount),
        style = style,
        color = color,
        fontWeight = fontWeight
    )
}

@Composable
fun WhatsAppSendDialog(
    customerName: String,
    phone: String,
    initialMessage: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var messageText by remember { mutableStateOf(initialMessage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = WaqarGreen)
                Spacer(Modifier.width(8.dp))
                Text("WhatsApp Notification")
            }
        },
        text = {
            Column {
                Text(
                    text = "Recipient: $customerName (${phone.ifBlank { "No number" }})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Message Body") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Template placeholder replacements applied automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(messageText) },
                colors = ButtonDefaults.buttonColors(containerColor = WaqarGreen)
            ) {
                Text("Send via WhatsApp", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) MaterialTheme.colorScheme.error else WaqarNavy
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InquiryItemCard(
    inquiry: com.example.data.local.entity.InquiryRecordEntity,
    onWhatsAppClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = inquiry.customerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = WaqarNavy
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(inquiry.status)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${inquiry.serviceOrProject} • ${inquiry.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Cost: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    RupeeText(inquiry.finalCost, style = MaterialTheme.typography.bodySmall, color = WaqarBlue)
                    if (inquiry.remainingAmount > 0) {
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Due: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        RupeeText(inquiry.remainingAmount, style = MaterialTheme.typography.bodySmall, color = Color.Red)
                    }
                }
            }

            IconButton(
                onClick = onWhatsAppClick,
                colors = IconButtonDefaults.iconButtonColors(contentColor = WaqarGreen)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send WhatsApp")
            }
        }
    }
}
