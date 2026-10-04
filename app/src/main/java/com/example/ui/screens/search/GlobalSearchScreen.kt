package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusBadge
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

data class GlobalSearchResult(
    val type: String,
    val id: String,
    val title: String,
    val subtitle: String,
    val status: String
)

@Composable
fun GlobalSearchScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val inquiries by viewModel.inquiries.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val admissions by viewModel.admissions.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val results = remember(query, inquiries, customers, admissions, orders) {
        if (query.isBlank()) emptyList() else {
            val q = query.trim().lowercase()
            val list = mutableListOf<GlobalSearchResult>()

            inquiries.filter {
                it.customerName.lowercase().contains(q) ||
                        it.phone.contains(q) ||
                        it.id.lowercase().contains(q) ||
                        it.serviceOrProject.lowercase().contains(q)
            }.forEach {
                list.add(GlobalSearchResult("INQUIRY", it.id, it.customerName, "${it.serviceOrProject} • ${it.phone}", it.status))
            }

            admissions.filter {
                it.applicantName.lowercase().contains(q) ||
                        it.registrationNumber.lowercase().contains(q) ||
                        it.courseOrService.lowercase().contains(q)
            }.forEach {
                list.add(GlobalSearchResult("ADMISSION", it.id, it.applicantName, "Reg: ${it.registrationNumber} • ${it.courseOrService}", it.verificationStatus))
            }

            orders.filter {
                it.customerName.lowercase().contains(q) ||
                        it.titleOrService.lowercase().contains(q) ||
                        it.id.lowercase().contains(q)
            }.forEach {
                list.add(GlobalSearchResult("ORDER", it.id, it.titleOrService, "Client: ${it.customerName}", it.status))
            }

            customers.filter {
                it.name.lowercase().contains(q) ||
                        it.phone.contains(q) ||
                        it.tags.lowercase().contains(q)
            }.forEach {
                list.add(GlobalSearchResult("CUSTOMER", it.id, it.name, "${it.phone} • ${it.tags}", it.status))
            }

            list
        }
    }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Global Search",
                subtitle = "Unified multi-entity finder",
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
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search by name, phone, order, admission reg...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (query.isBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Type a search query above to look up records", color = Color.Gray, fontSize = 14.sp)
                }
            } else if (results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records matched '$query'", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(results) { res ->
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
                                    Surface(
                                        color = WaqarSlate,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            res.type,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(res.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WaqarNavy)
                                    Text(res.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                }
                                StatusBadge(res.status)
                            }
                        }
                    }
                }
            }
        }
    }
}
