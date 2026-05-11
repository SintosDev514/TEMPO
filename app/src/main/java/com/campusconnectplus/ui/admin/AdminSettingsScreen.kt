package com.campusconnectplus.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun AdminSettingsScreen(onLogout: () -> Unit = {}) {
    Scaffold { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .background(AdminColors.Background)
                .padding(paddingValues)
        ) {
            TopBar(title = "System Settings", subtitle = "View system information and manage your session")

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // System Information Section
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AdminColors.Border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = AdminColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "System Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AdminColors.Dark
                            )
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        InfoRow("App Version", "1.0.2")
                        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = AdminColors.Border.copy(alpha = 0.5f))
                        InfoRow("Database", "Room (Local)")
                        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = AdminColors.Border.copy(alpha = 0.5f))
                        InfoRow("Status", "Operational", valueColor = Color(0xFF10B981))
                    }
                }

                Spacer(Modifier.weight(1f))

                // Professional Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFEE2E2)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Sign Out",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Spacer(Modifier.height(80.dp)) // Padding for bottom bar
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = AdminColors.Dark) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label, 
            style = MaterialTheme.typography.bodyLarge,
            color = AdminColors.Secondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            value, 
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor, 
            fontWeight = FontWeight.Bold
        )
    }
}
