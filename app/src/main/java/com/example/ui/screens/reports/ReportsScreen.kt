package com.example.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.pdf.PdfGenerator
import com.example.ui.components.RupeeText
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@Composable
fun ReportsScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    val inquiries by viewModel.inquiries.collectAsState()
    val admissions by viewModel.admissions.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Reports & Analytics",
                subtitle = "Comprehensive performance metrics",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WaqarNavy)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("TOTAL RECORDED REVENUE", color = WaqarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = PdfGenerator.formatRupee(totalRevenue ?: 0.0),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${payments.size} payments processed across all accounts",
                            color = WaqarSilver,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                ReportSectionCard(
                    title = "Inquiries Breakdown",
                    items = listOf(
                        "Total Inquiries" to "${inquiries.size}",
                        "Pending Action" to "${inquiries.count { it.status == "Pending" }}",
                        "Completed / Enrolled" to "${inquiries.count { it.status == "Completed" || it.status == "Admission Complete" }}",
                        "Cancelled" to "${inquiries.count { it.status == "Cancelled" }}"
                    )
                )
            }

            item {
                ReportSectionCard(
                    title = "Admission Status",
                    items = listOf(
                        "Total Applications" to "${admissions.size}",
                        "Verified Candidates" to "${admissions.count { it.verificationStatus == "Verified" }}",
                        "Pending Verification" to "${admissions.count { it.verificationStatus != "Verified" }}",
                        "Fully Paid" to "${admissions.count { it.remainingAmount <= 0.0 && it.cost > 0 }}"
                    )
                )
            }

            item {
                ReportSectionCard(
                    title = "Orders Pipeline",
                    items = listOf(
                        "Total Work Orders" to "${orders.size}",
                        "In Processing" to "${orders.count { it.status == "Processing" }}",
                        "Completed" to "${orders.count { it.status == "Completed" }}",
                        "Pending" to "${orders.count { it.status == "Pending" }}"
                    )
                )
            }
        }
    }
}

@Composable
fun ReportSectionCard(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = WaqarNavy, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                    Text(value, fontWeight = FontWeight.Bold, color = WaqarBlue)
                }
            }
        }
    }
}
