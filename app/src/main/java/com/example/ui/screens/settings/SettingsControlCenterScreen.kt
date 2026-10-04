package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@Composable
fun SettingsControlCenterScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToWhatsAppLimits: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val syncState by viewModel.syncState.collectAsState()
    val session by viewModel.sessionState.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val inquiries by viewModel.inquiries.collectAsState()

    var isSeedingBenchmark by remember { mutableStateOf(false) }
    var seedProgress by remember { mutableStateOf(0) }
    var isBackingUp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Admin Control Center",
                subtitle = "Security, Backups & System Health",
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
            // System Health Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WaqarNavy)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("SYSTEM HEALTH & TELEMETRY", color = WaqarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Database Status:", color = WaqarSilver, fontSize = 13.sp)
                            Text("Operational (SQLite Room)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Cloud Sync Queue:", color = WaqarSilver, fontSize = 13.sp)
                            Text("${syncState.pendingCount} pending items", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Customer Records:", color = WaqarSilver, fontSize = 13.sp)
                            Text("${customers.size} loaded", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Security Operator:", color = WaqarSilver, fontSize = 13.sp)
                            Text("${session.username} (${session.role})", color = WaqarGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Quick Nav List
            item {
                Text("Personalization & Appearance", fontWeight = FontWeight.Bold, color = WaqarNavy)
                Spacer(Modifier.height(8.dp))

                val isDark by viewModel.isDarkMode.collectAsState()
                val isFlowerOn by viewModel.isFlowerAnimationEnabled.collectAsState()

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isDark == true) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = WaqarGold
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Dark Mode Theme", fontWeight = FontWeight.Bold, color = WaqarNavy)
                                Text(if (isDark == true) "Dark Theme Active" else "Light Theme Active", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = isDark == true,
                            onCheckedChange = { viewModel.toggleDarkMode() }
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Spa, contentDescription = null, tint = Color(0xFFE91E63))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Scrolling Flower Animation", fontWeight = FontWeight.Bold, color = WaqarNavy)
                                Text("Ambient drifting blossoms & banner", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        Switch(
                            checked = isFlowerOn,
                            onCheckedChange = { viewModel.toggleFlowerAnimation() }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Operational Modules", fontWeight = FontWeight.Bold, color = WaqarNavy)
                Spacer(Modifier.height(8.dp))

                SettingsNavTile(
                    title = "Recycle Bin & Safe Soft Deletion",
                    subtitle = "Restore accidentally removed records",
                    icon = Icons.Default.DeleteOutline,
                    onClick = onNavigateToRecycleBin
                )

                SettingsNavTile(
                    title = "Immutable Security Audit Logs",
                    subtitle = "Review timestamped user and system events",
                    icon = Icons.Default.History,
                    onClick = onNavigateToAuditLogs
                )

                SettingsNavTile(
                    title = "WhatsApp Limit & Rate Control",
                    subtitle = "Configure daily sending quotas and templates",
                    icon = Icons.AutoMirrored.Filled.Send,
                    onClick = onNavigateToWhatsAppLimits
                )
            }

            // Backup & Database Maintenance
            item {
                Text("Backup & Performance Scaling", fontWeight = FontWeight.Bold, color = WaqarNavy)
                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = WaqarBlue)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Verified Local Encrypted Backup", fontWeight = FontWeight.Bold, color = WaqarNavy)
                                Text("Generate a verified snapshot of customers, inquiries & billing", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        Button(
                            onClick = {
                                isBackingUp = true
                                viewModel.createBackup(context) { res ->
                                    isBackingUp = false
                                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = !isBackingUp,
                            colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isBackingUp) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Create Full Verified Backup")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Section 37: 15,000 synthetic test benchmark
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = WaqarBlue)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("15,000+ Record Stress Benchmark", fontWeight = FontWeight.Bold, color = WaqarNavy)
                                Text("Generate 15,000 synthetic records to verify Room indexing & Paging", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        if (isSeedingBenchmark) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text("Inserted $seedProgress / 15,000 records...", fontSize = 12.sp, color = Color.DarkGray)
                        } else {
                            OutlinedButton(
                                onClick = {
                                    isSeedingBenchmark = true
                                    viewModel.seed15kBenchmark(
                                        onProgress = { p -> seedProgress = p },
                                        onComplete = { total ->
                                            isSeedingBenchmark = false
                                            Toast.makeText(context, "Seeded $total synthetic records successfully!", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Seed 15,000 Records Benchmark")
                            }
                        }
                    }
                }
            }

            // Sign Out
            item {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.logout(onLoggedOut) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out Administrator")
                }
            }
        }
    }
}

@Composable
fun SettingsNavTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = WaqarBlue)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = WaqarNavy)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
