package com.campusconnectplus.ui.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.Media

@Composable
fun EventCard(
    event: Event,
    isSaved: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    imageUrl: String? = event.imageUrl
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp), clip = false),
        shape = RoundedCornerShape(28.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Background Image
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Image,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White.copy(alpha = 0.2f)
                    )
                }
            }

            // Dark Scrim for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 300f
                        )
                    )
            )

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Category and Save Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = event.category.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Bottom Section: Event Details
                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                event.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                event.venue,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Join Now",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "View Details →",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailDialog(
    event: Event,
    onDismiss: () -> Unit,
    eventMedia: List<Media> = emptyList(),
    onMediaClick: (Media) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Side: Image
                    val headerImage = event.imageUrl ?: eventMedia.firstOrNull()?.url
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (headerImage != null) {
                            AsyncImage(
                                model = headerImage,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Image, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.2f))
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
                                )
                        )
                        
                        Text(
                            event.title,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Right Side: Details
                    Column(modifier = Modifier.weight(1.2f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(24.dp)) {
                        DetailContent(event, eventMedia, onMediaClick, onDismiss)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    val headerImage = event.imageUrl ?: eventMedia.firstOrNull()?.url
                    // Large Header Image
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        if (headerImage != null) {
                            AsyncImage(
                                model = headerImage,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Image, null, modifier = Modifier.size(64.dp), tint = Color.White.copy(alpha = 0.2f))
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))
                                )
                        )
                        
                        Text(
                            event.title,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        DetailContent(event, eventMedia, onMediaClick, onDismiss)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailContent(
    event: Event,
    eventMedia: List<Media>,
    onMediaClick: (Media) -> Unit,
    onDismiss: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                event.category.name,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.width(12.dp))
        Text("Open for Registration", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
    }

    Spacer(Modifier.height(24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoSection(Icons.Outlined.CalendarMonth, "Date", event.date, Modifier.weight(1f))
        InfoSection(Icons.Outlined.LocationOn, "Venue", event.venue, Modifier.weight(1f))
    }

    Spacer(Modifier.height(32.dp))

    Text("About this event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Text(
        event.description,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 28.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (eventMedia.isNotEmpty()) {
        Spacer(Modifier.height(32.dp))
        Text("Gallery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(eventMedia) { media ->
                Card(
                    modifier = Modifier.size(150.dp, 100.dp),
                    shape = RoundedCornerShape(12.dp),
                    onClick = { onMediaClick(media) }
                ) {
                    AsyncImage(
                        model = media.url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(40.dp))
    Button(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text("Dismiss", style = MaterialTheme.typography.titleMedium)
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
fun AnnouncementDetailDialog(
    announcement: Announcement,
    onDismiss: () -> Unit,
    timeAgo: (Long) -> String
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Side: Header with Title
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                                )
                            )
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (announcement.priority == 1) Color(0xFFEF4444) else Color.White.copy(alpha = 0.2f),
                                border = if (announcement.priority == 1) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    if (announcement.priority == 1) "IMPORTANT" else "ACADEMIC UPDATE",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                announcement.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 30.sp
                            )
                        }
                    }

                    // Right Side: Content
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        AnnouncementDetailContent(announcement, timeAgo, onDismiss)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header - Immersive Blue Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                                )
                            )
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (announcement.priority == 1) Color(0xFFEF4444) else Color.White.copy(alpha = 0.2f),
                                border = if (announcement.priority == 1) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    if (announcement.priority == 1) "IMPORTANT" else "ACADEMIC UPDATE",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                announcement.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 34.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        AnnouncementDetailContent(announcement, timeAgo, onDismiss)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementDetailContent(
    announcement: Announcement,
    timeAgo: (Long) -> String,
    onDismiss: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Icon(
            Icons.Outlined.Schedule,
            null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "Posted On",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                timeAgo(announcement.updatedAt),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    Spacer(Modifier.height(32.dp))

    Text(
        "Official Announcement",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(16.dp))
    Text(
        announcement.content,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 30.sp,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(Modifier.height(48.dp))

    Button(
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text("I've read this", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
    
    Spacer(Modifier.height(16.dp))
    
    TextButton(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Close Details", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Spacer(Modifier.height(24.dp))
}

@Composable
fun InfoSection(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
