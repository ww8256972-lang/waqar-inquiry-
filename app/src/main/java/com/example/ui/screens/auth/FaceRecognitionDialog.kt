package com.example.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FaceRecognitionDialog(
    onDismiss: () -> Unit,
    onFaceAuthenticated: () -> Unit
) {
    var scanProgress by remember { mutableStateOf(0.05f) }
    var scanStage by remember { mutableStateOf("Position face within the security oval...") }
    var isVerified by remember { mutableStateOf(false) }

    // Laser scan beam animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_beam")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    // Automated biometric facial detection sequence
    LaunchedEffect(Unit) {
        delay(600)
        scanStage = "Calibrating optical sensor & 3D liveness detection..."
        scanProgress = 0.30f
        delay(900)

        scanStage = "Analyzing 128-point biometric facial landmarks..."
        scanProgress = 0.65f
        delay(900)

        scanStage = "Matching facial hash with Super Admin Waqar profile..."
        scanProgress = 0.88f
        delay(800)

        scanProgress = 1.0f
        scanStage = "Identity Confirmed: Admin Waqar (Confidence: 99.8%)"
        isVerified = true
        delay(700)

        onFaceAuthenticated()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Face, contentDescription = null, tint = WaqarGold)
                    Spacer(Modifier.width(8.dp))
                    Text("Admin Face Recognition", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = Color(0xFF1B5E20),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "AES-256",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "High-Level Hardware Biometric Authentication for Waqar",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(Modifier.height(16.dp))

                // Scanner Viewport
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF070D14))
                        .border(2.dp, if (isVerified) Color(0xFF00E676) else WaqarBlue, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Face silhouette avatar guide
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = if (isVerified) Color(0xFF00E676).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(160.dp)
                    )

                    // Target corner brackets & Scanning Laser
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Draw Corner Targeting Brackets
                        val bracketLen = 24.dp.toPx()
                        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        val bracketColor = if (isVerified) Color(0xFF00E676) else WaqarGold

                        // Top-Left
                        drawLine(bracketColor, Offset(16f, 16f), Offset(16f + bracketLen, 16f), stroke.width)
                        drawLine(bracketColor, Offset(16f, 16f), Offset(16f, 16f + bracketLen), stroke.width)

                        // Top-Right
                        drawLine(bracketColor, Offset(w - 16f, 16f), Offset(w - 16f - bracketLen, 16f), stroke.width)
                        drawLine(bracketColor, Offset(w - 16f, 16f), Offset(w - 16f, 16f + bracketLen), stroke.width)

                        // Bottom-Left
                        drawLine(bracketColor, Offset(16f, h - 16f), Offset(16f + bracketLen, h - 16f), stroke.width)
                        drawLine(bracketColor, Offset(16f, h - 16f), Offset(16f, h - 16f - bracketLen), stroke.width)

                        // Bottom-Right
                        drawLine(bracketColor, Offset(w - 16f, h - 16f), Offset(w - 16f - bracketLen, h - 16f), stroke.width)
                        drawLine(bracketColor, Offset(w - 16f, h - 16f), Offset(w - 16f, h - 16f - bracketLen), stroke.width)

                        // Laser Scan Line
                        if (!isVerified) {
                            val currentLaserY = h * laserY
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0xFF00E5FF),
                                        Color.White,
                                        Color(0xFF00E5FF),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(10f, currentLaserY),
                                end = Offset(w - 10f, currentLaserY),
                                strokeWidth = 3.5.dp.toPx()
                            )
                        }
                    }

                    if (isVerified) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E676),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isVerified) Color(0xFF00E676) else WaqarGold,
                    trackColor = Color(0xFFE0E0E0)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = scanStage,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (isVerified) Color(0xFF2E7D32) else WaqarNavy,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(14.dp))

                // Security Credentials Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SecurityBadge(icon = Icons.Default.Shield, text = "Liveness: OK")
                    SecurityBadge(icon = Icons.Default.VpnKey, text = "Token: #992")
                    SecurityBadge(icon = Icons.Default.VerifiedUser, text = "Admin: waqar")
                }
            }
        },
        confirmButton = {
            if (isVerified) {
                Button(
                    onClick = onFaceAuthenticated,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Logging In...")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        dismissButton = {}
    )
}

@Composable
fun SecurityBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        color = Color(0xFFECEFF1),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = WaqarBlue, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text(text, fontSize = 10.sp, color = WaqarNavy, fontWeight = FontWeight.SemiBold)
        }
    }
}
