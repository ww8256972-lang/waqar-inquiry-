package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SnowFallingAnimation
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaqarViewModel

@Composable
fun LoginScreen(
    viewModel: WaqarViewModel,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("waqar") }
    var password by remember { mutableStateOf("waqar") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showFaceRecognitionDialog by remember { mutableStateOf(false) }

    val session by viewModel.sessionState.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    if (session.mustChangePassword) {
        ChangePasswordContent(viewModel = viewModel, onPasswordChanged = onLoginSuccess)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaqarNavy),
        contentAlignment = Alignment.Center
    ) {
        // Snow Falling Animation
        SnowFallingAnimation(modifier = Modifier.fillMaxSize(), snowCount = 80)

        // Dark / Light Theme Toggle in Top-Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isDark == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = WaqarGold
                    )
                }
            }
        }

        // Login Card with input fields
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3D Emblem Symbol
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(WaqarNavy)
                        .border(2.5.dp, WaqarGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "W",
                        color = WaqarGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 34.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "WAQAR WEBSITE INQUIRY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = WaqarNavy
                    )
                )
                Text(
                    text = "Administrative Access Portal",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Credentials fields with snow-falling ambiance
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    label = { Text("Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WaqarBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WaqarBlue) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Standard Sign In
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        viewModel.login(
                            username = username,
                            pass = password,
                            onSuccess = {
                                isLoading = false
                                onLoginSuccess()
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Sign In (waqar / waqar)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto Face-Detector feature specifically for admin face recognition with high-level security
                OutlinedButton(
                    onClick = { showFaceRecognitionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, WaqarBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WaqarNavy)
                ) {
                    Icon(Icons.Default.Face, contentDescription = null, tint = WaqarGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Auto Face-Detector (Admin Waqar)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = WaqarGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "High-Level Security: AES-256 Biometric Auth",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    if (showFaceRecognitionDialog) {
        FaceRecognitionDialog(
            onDismiss = { showFaceRecognitionDialog = false },
            onFaceAuthenticated = {
                showFaceRecognitionDialog = false
                viewModel.loginWithFaceRecognition(
                    onSuccess = onLoginSuccess,
                    onError = { err -> errorMessage = err }
                )
            }
        )
    }
}

@Composable
fun ChangePasswordContent(
    viewModel: WaqarViewModel,
    onPasswordChanged: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaqarNavy),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "First Login Security Requirement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WaqarNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To safeguard Waqar Website Inquiry records, please set a new strong password for your administrator account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (newPassword != confirmPassword) {
                            error = "Passwords do not match"
                            return@Button
                        }
                        viewModel.changePassword(
                            newPass = newPassword,
                            onSuccess = onPasswordChanged,
                            onError = { error = it }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaqarNavy)
                ) {
                    Text("Update & Proceed")
                }
            }
        }
    }
}
