package com.example.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiHelpService
import com.example.data.ai.HelpAnswer
import com.example.ui.components.WaqarTopAppBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@Composable
fun AskAiScreen(
    viewModel: WaqarViewModel,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf<HelpAnswer?>(null) }
    val topics = remember(searchQuery) { AiHelpService.searchHelp(searchQuery) }

    Scaffold(
        topBar = {
            WaqarTopAppBar(
                title = "Ask AI Assistant",
                subtitle = "App Knowledge Base & Operational Guidance",
                canNavigateBack = true,
                onNavigateBack = {
                    if (selectedTopic != null) selectedTopic = null else onNavigateBack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(WaqarSurfaceLight)
        ) {
            if (selectedTopic == null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = WaqarNavy)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = WaqarGold, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Ask AI Assistant", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Select a question or search for features below", color = WaqarSilver, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Ask: 'How do I add a record?', 'verification'...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(topics) { topic ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTopic = topic },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(topic.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = WaqarNavy)
                                Spacer(Modifier.height(4.dp))
                                Text(topic.summary, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            }
                        }
                    }
                }
            } else {
                val topic = selectedTopic!!
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(topic.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WaqarNavy)
                                Spacer(Modifier.height(6.dp))
                                Text(topic.summary, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)

                                Spacer(Modifier.height(16.dp))
                                Text("Step-by-Step Instructions:", fontWeight = FontWeight.Bold, color = WaqarBlue)
                                Spacer(Modifier.height(8.dp))

                                topic.steps.forEachIndexed { index, step ->
                                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text("${index + 1}. ", fontWeight = FontWeight.Bold, color = WaqarNavy)
                                        Text(step, style = MaterialTheme.typography.bodySmall, color = Color.Black)
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                Surface(
                                    color = Color(0xFFFFF8E1),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = WaqarGold, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(topic.tips, style = MaterialTheme.typography.bodySmall, color = Color(0xFF5D4037))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { selectedTopic = null },
                            colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to All AI Topics")
                        }
                    }
                }
            }
        }
    }
}
