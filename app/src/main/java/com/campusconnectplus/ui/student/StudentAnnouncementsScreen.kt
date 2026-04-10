package com.campusconnectplus.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.feature_student.announcements.StudentAnnouncementsViewModel

private fun priorityLabel(priority: Int): String = if (priority == 1) "Important" else "Normal"

private fun timeAgo(updatedAt: Long): String {
    val diff = System.currentTimeMillis() - updatedAt
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}

@Composable
fun StudentAnnouncementsScreen(vm: StudentAnnouncementsViewModel) {
    val announcements by vm.announcements.collectAsState()
    var selectedAnnouncement by remember { mutableStateOf<Announcement?>(null) }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2B59D9))))
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                "Announcements",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${announcements.size} announcement${if (announcements.size == 1) "" else "s"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        if (announcements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    "No announcements right now.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(announcements, key = { it.id }) { a ->
                    AnnouncementCard(a, onClick = { selectedAnnouncement = a })
                }
            }
        }
    }

    selectedAnnouncement?.let { a ->
        AlertDialog(
            onDismissRequest = { selectedAnnouncement = null },
            title = { Text(a.title, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Posted ${timeAgo(a.updatedAt)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(a.content, style = MaterialTheme.typography.bodyLarge)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAnnouncement = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun AnnouncementCard(a: Announcement, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(a.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                a.content,
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(priorityLabel(a.priority)) }
                )
                Text(
                    timeAgo(a.updatedAt),
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
