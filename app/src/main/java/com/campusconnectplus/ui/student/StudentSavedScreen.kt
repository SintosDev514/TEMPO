package com.campusconnectplus.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.campusconnectplus.core.ui.components.MediaDetailViewer
import com.campusconnectplus.core.ui.components.MediaGridItem
import com.campusconnectplus.feature_student.saved.StudentSavedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSavedScreen(vm: StudentSavedViewModel) {
    val savedEvents by vm.savedEvents.collectAsState()
    val savedMedia by vm.savedMedia.collectAsState()

    var tabIndex by remember { mutableStateOf(0) }
    var selectedEvent by remember { mutableStateOf<com.campusconnectplus.data.repository.Event?>(null) }
    var selectedMedia by remember { mutableStateOf<com.campusconnectplus.data.repository.Media?>(null) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val tabs = listOf("Events", "Media")
    val totalItems = savedEvents.size + savedMedia.size

    Column(Modifier.fillMaxSize()) {
        // Header - Updated to Professional Blue Theme
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                    )
                )
                .padding(horizontal = 24.dp, vertical = if (isLandscape) 16.dp else 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Content",
                        style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    if (!isLandscape) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Available for offline access",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(if (isLandscape) 12.dp else 24.dp))

            // Storage summary card - Polished
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(Modifier.padding(if (isLandscape) 12.dp else 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Collection Status",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "$totalItems Items",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(Modifier.height(if (isLandscape) 8.dp else 12.dp))
                    LinearProgressIndicator(
                        progress = { (totalItems / 20f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isLandscape) 4.dp else 6.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                    if (!isLandscape) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatusChip(Icons.Outlined.Event, "${savedEvents.size} Events")
                            StatusChip(Icons.Outlined.PhotoLibrary, "${savedMedia.size} Media")
                        }
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
                            EmptyState("No saved events yet.", Icons.Outlined.Event)
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 340.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    top = 4.dp,
                                    bottom = 120.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(savedEvents, key = { it.id }) { event ->
                                    val eventMedia by vm.getMediaForEvent(event.id).collectAsState(initial = emptyList())
                                    val firstImageUrl = eventMedia.firstOrNull { it.type == com.campusconnectplus.data.repository.MediaType.IMAGE }?.url

                                    EventCard(
                                        event = event,
                                        isSaved = true,
                                        onToggleFavorite = { vm.removeEvent(event.id) },
                                        onClick = { selectedEvent = event },
                                        imageUrl = event.imageUrl ?: firstImageUrl
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        if (savedMedia.isEmpty()) {
                            EmptyState("No saved media yet.", Icons.Outlined.PhotoLibrary)
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 160.dp),
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
        EventDetailDialog(
            event = event,
            onDismiss = { selectedEvent = null },
            eventMedia = vm.getMediaForEvent(event.id).collectAsState(initial = emptyList()).value,
            onMediaClick = { selectedMedia = it }
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
private fun StatusChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.White)
        Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyState(message: String, icon: ImageVector = Icons.Outlined.BookmarkBorder) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Spacer(Modifier.height(24.dp))
        Text("Nothing here yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
