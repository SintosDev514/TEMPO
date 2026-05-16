package com.campusconnectplus.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.campusconnectplus.feature_admin.AdminDashboardViewModel

@Composable
fun AdminDashboardScreen(vm: AdminDashboardViewModel, onViewDetails: () -> Unit = {}) {
    val stats by vm.stats.collectAsState()

    Column(Modifier.fillMaxSize().background(AdminColors.Background)) {
        TopBar(
            title = "Admin Console", 
            subtitle = "Welcome back, System Administrator",
            onPrimary = null
        )
        
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SectionHeader("Content Overview", onViewDetails = onViewDetails)

            // Content Distribution Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ContentRow(
                    label = "Campus Events",
                    count = stats.totalEvents,
                    icon = Icons.Outlined.Event,
                    trend = "+2 this week"
                )
                ContentRow(
                    label = "Media Library",
                    count = stats.totalMedia,
                    icon = Icons.Outlined.Collections,
                    trend = "42 GB used"
                )
                ContentRow(
                    label = "Active News",
                    count = stats.totalAnnouncements,
                    icon = Icons.Outlined.Campaign,
                    trend = "3 expiring soon"
                )
                ContentRow(
                    label = "Total Engagement",
                    count = stats.totalReactions,
                    icon = Icons.Outlined.ThumbUp,
                    trend = "Reactions across events"
                )
            }



            // Added Spacer to ensure bottom content isn't cut off by Navigation Bar
            Spacer(Modifier.height(84.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, onViewDetails: () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            color = AdminColors.Dark
        )
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onViewDetails) {
            Text("View Details", style = MaterialTheme.typography.labelMedium, color = AdminColors.Primary)
        }
    }
}



@Composable
private fun ContentRow(
    label: String,
    count: Int,
    icon: ImageVector,
    trend: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, AdminColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = AdminColors.Secondary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.SemiBold, color = AdminColors.Dark)
                Text(trend, style = MaterialTheme.typography.labelSmall, color = Color(0xFF10B981))
            }
            Text(
                count.toString(), 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.titleLarge,
                color = AdminColors.Dark
            )
        }
    }
}
