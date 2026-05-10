package com.campusconnectplus.ui.student

import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusconnectplus.core.ui.components.MediaDetailViewer
import com.campusconnectplus.core.ui.components.MediaGridItem
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
                    imageVector = Icons.Filled.WifiOff,
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

            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (index == 0) Icons.Outlined.Event else Icons.Outlined.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(title)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(Modifier.weight(1f)) {
                when (tabIndex) {
                    0 -> {
                        if (savedEvents.isEmpty()) {
                            EmptyState("No saved events yet.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    top = 4.dp,
                                    bottom = 120.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            )
                            {
                                items(savedEvents, key = { it.id }) { event ->
                                    EventSavedCard(
                                        event = event,
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
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(savedMedia, key = { it.id }) { media ->
                                    MediaGridItem(
                                        item = media,
                                        isSaved = true,
                                        onToggleFavorite = { vm.removeMedia(media.id) },
                                        onClick = { selectedMedia = media }
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
        MediaDetailViewer(
            item = item,
            onDownload = { vm.downloadMedia(it) },
            onDismiss = { selectedMedia = null }
        )
    }
}

@Composable
private fun EventSavedCard(
    event: com.campusconnectplus.data.repository.Event,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {

            // Top Banner / Thumbnail Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF7C3AED),
                                Color(0xFF8B5CF6),
                                Color(0xFFA78BFA)
                            )
                        )
                    )
            ) {

                // Fake Event Cover Design
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Event,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "Saved Event",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                }

                // Bookmark Floating Button
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Remove Saved",
                            tint = Color.White
                        )
                    }
                }
            }

            // Content Area
            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                // Date Chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFF3F0FF)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = event.date,
                            color = Color(0xFF7C3AED),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Venue
                Text(
                    text = event.venue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFEDE9FE)
                        ) {
                            Text(
                                text = "Offline Saved",
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                ),
                                color = Color(0xFF6D28D9),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("View")
                    }
                }
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
