package com.campusconnectplus.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.campusconnectplus.core.ui.components.VideoPlayer
import com.campusconnectplus.data.repository.MediaType
import com.campusconnectplus.feature_student.saved.StudentSavedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSavedScreen(vm: StudentSavedViewModel) {
    val savedEvents by vm.savedEvents.collectAsState()
    val savedMedia by vm.savedMedia.collectAsState()

    var tabIndex by remember { mutableStateOf(0) }
    var selectedEvent by remember { mutableStateOf<com.campusconnectplus.data.repository.Event?>(null) }
    var selectedMedia by remember { mutableStateOf<com.campusconnectplus.data.repository.Media?>(null) }

    val tabs = listOf("Events", "Media")
    val totalItems = savedEvents.size + savedMedia.size

    Column(Modifier.fillMaxSize()) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF6D28D9), Color(0xFF8B5CF6))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Content",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        text = "Available offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.WifiOff,
                    contentDescription = "Offline",
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))

            // Storage summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        text = "Storage Used",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (totalItems / 20f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${savedEvents.size} Events",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                        Text(
                            text = "${savedMedia.size} Media",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                        Text(
                            text = "$totalItems Total Items",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(Modifier.weight(1f)) {
                when (tabIndex) {
                    0 -> {
                        if (savedEvents.isEmpty()) {
                            EmptyState("No saved events yet.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(savedEvents, key = { it.id }) { event ->
                                    SavedCard(
                                        title = event.title,
                                        subtitle = event.date,
                                        onClick = { selectedEvent = event },
                                        onRemove = { vm.removeEvent(event.id) }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        if (savedMedia.isEmpty()) {
                            EmptyState("No saved media yet.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(savedMedia, key = { it.id }) { media ->
                                    SavedCard(
                                        title = media.title.ifEmpty { media.fileName },
                                        subtitle = media.type.name,
                                        onClick = { selectedMedia = media },
                                        onRemove = { vm.removeMedia(media.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = { Text(event.title, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Date: ${event.date}", style = MaterialTheme.typography.bodyMedium)
                    Text("Venue: ${event.venue}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(event.description, style = MaterialTheme.typography.bodyLarge)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEvent = null }) {
                    Text("Close")
                }
            }
        )
    }

    selectedMedia?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedMedia = null },
            title = { Text(item.title.ifEmpty { item.fileName }, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Type: ${item.type.name}", style = MaterialTheme.typography.bodyMedium)
                    if (item.date.isNotBlank()) Text("Date: ${item.date}", style = MaterialTheme.typography.bodyMedium)
                    if (item.sizeMb > 0) Text("Size: ${item.sizeMb} MB", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))

                    if (item.url.isNotBlank()) {
                        if (item.type == MediaType.VIDEO) {
                            VideoPlayer(url = item.url)
                        } else {
                            AsyncImage(
                                model = item.url,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Color.LightGray, RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Text("Media URL: ${item.url}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMedia = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SavedCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onRemove) {
                Text("Remove")
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
