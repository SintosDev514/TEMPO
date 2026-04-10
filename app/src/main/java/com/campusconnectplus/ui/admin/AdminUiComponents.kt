package com.campusconnectplus.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast

// Modern Professional Color Palette (Synced with Student UI)
object AdminColors {
    val Primary = Color(0xFF1E3A8A) // Student Blue (Dark)
    val PrimaryLight = Color(0xFF2B59D9) // Student Blue (Light)
    val Secondary = Color(0xFF64748B) // Slate Gray
    val Dark = Color(0xFF0F172A) // Near Black
    val Background = Color(0xFFF8FAFC)
    val Surface = Color.White
    val Border = Color(0xFFE2E8F0)
    
    // Gradient matching StudentHeader
    val HeaderBrush = Brush.verticalGradient(listOf(Primary, PrimaryLight))
}

@Composable
fun TopBar(title: String, subtitle: String, onPrimary: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AdminColors.HeaderBrush)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "CampusConnect+",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Admin Portal",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.2.sp
                )
            }

            if (onPrimary != null) {
                val ctx = LocalContext.current
                IconButton(
                    onClick = { Toast.makeText(ctx, "No new notifications", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onPrimary,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    elevation = ButtonDefaults.filledTonalButtonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Create", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            title,
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            subtitle,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun EmptyAdminPanel(iconText: String, title: String, hint: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = AdminColors.Background,
                border = BorderStroke(1.dp, AdminColors.Border)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(iconText, fontSize = 32.sp)
                }
            }
            Text(title, fontWeight = FontWeight.Bold, color = AdminColors.Dark)
            Text(
                hint,
                color = AdminColors.Secondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
