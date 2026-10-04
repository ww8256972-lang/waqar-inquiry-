package com.example.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ReminderEntity
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun CalendarRemindersScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val reminders by viewModel.reminders.collectAsState()
    var showNewReminderDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Follow-up Calendar",
                subtitle = "${reminders.count { !it.isCompleted }} scheduled follow-ups",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewReminderDialog = true },
                containerColor = WaqarNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight)
        ) {
            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No Scheduled Follow-ups", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Add reminders for candidate follow-ups, fee dues, and inquiries.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
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
                                    Text(reminder.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WaqarNavy)
                                    if (reminder.description.isNotBlank()) {
                                        Text(reminder.description, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Due: ${dateFormat.format(Date(reminder.dueDate))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WaqarSlate,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(onClick = { viewModel.completeReminder(reminder.id) }) {
                                    Icon(
                                        if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Complete",
                                        tint = if (reminder.isCompleted) Color(0xFF2E7D32) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewReminderDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewReminderDialog = false },
            title = { Text("Create Follow-up Reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Follow-up Title *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Details / Customer Contact") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isBlank()) return@Button
                        val rem = ReminderEntity(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            description = description.trim(),
                            dueDate = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
                        )
                        viewModel.saveReminder(rem) {
                            showNewReminderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy)
                ) {
                    Text("Save Reminder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewReminderDialog = false }) { Text("Cancel") }
            }
        )
    }
}
