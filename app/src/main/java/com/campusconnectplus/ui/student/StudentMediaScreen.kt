package com.campusconnectplus.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.campusconnectplus.core.ui.components.MediaDetailViewer
import com.campusconnectplus.core.ui.components.MediaGridItem
import com.campusconnectplus.core.ui.components.VideoPlayer
import com.campusconnectplus.data.repository.MediaType
import com.campusconnectplus.feature_student.media.StudentMediaViewModel

@Composable
fun StudentMediaScreen(vm: StudentMediaViewModel) {
    val media by vm.media.collectAsState()
    val favIds by vm.favoriteMediaIds.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedMedia by remember { mutableStateOf<com.campusconnectplus.data.repository.Media?>(null) }
    val filters = listOf("All", "Images", "Videos", "Saved")

    val filteredMedia = remember(media, searchQuery, selectedFilter, favIds) {
        media.filter {
            (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) ||
                it.fileName.contains(searchQuery, ignoreCase = true)) &&
            when (selectedFilter) {
                "Images" -> it.type == MediaType.IMAGE
                "Videos" -> it.type == MediaType.VIDEO
                "Saved" -> favIds.contains(it.id)
                else -> true
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Header – teal, matching reference (Media Gallery)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF0D9488), Color(0xFF14B8A6))))
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                "Media Gallery",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search media...", color = Color.White.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            labelColor = Color.White,
                            selectedContainerColor = Color.White,
                            selectedLabelColor = Color(0xFF0D9488)
                        )
                    )
                }
            }
        }

        if (filteredMedia.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No media available.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMedia, key = { it.id }) { item ->
                    MediaGridItem(
                        item = item,
                        isSaved = favIds.contains(item.id),
                        onToggleFavorite = { vm.toggleFavorite(item.id) },
                        onClick = { selectedMedia = item }
                    )
                }
            }
        }
    }

    selectedMedia?.let { item ->
        MediaDetailViewer(
            item = item,
            onDownload = { vm.downloadMedia(it) },
            onDismiss = { selectedMedia = null }
        )
    }
}

