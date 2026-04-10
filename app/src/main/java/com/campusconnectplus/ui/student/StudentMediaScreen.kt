package com.campusconnectplus.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
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
    val filters = listOf("All", "Events", "Campus Life", "Sports")

    val filteredMedia = remember(media, searchQuery) {
        media.filter {
            searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) ||
                it.fileName.contains(searchQuery, ignoreCase = true)
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
            Spacer(Modifier.height(8.dp))
            Text(
                "${filteredMedia.size} items found",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            if (filteredMedia.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No media available.", style = MaterialTheme.typography.bodyLarge)
                }
                return@Column
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMedia, key = { it.id }) { item ->
                    val isSaved = favIds.contains(item.id)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        onClick = { selectedMedia = item }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title.ifEmpty { item.fileName }, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(item.type.name, style = MaterialTheme.typography.bodyMedium)
                                if (item.date.isNotBlank()) Text(item.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { vm.toggleFavorite(item.id) }) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = if (isSaved) "Unsave" else "Save"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedMedia?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedMedia = null },
            title = { Text(item.title.ifEmpty { item.fileName }, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = {
                Column {
                    Text("Type: ${item.type.name}", style = MaterialTheme.typography.bodyMedium)
                    if (item.date.isNotBlank()) {
                        Text("Date: ${item.date}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (item.duration.isNotBlank()) {
                        Text("Duration: ${item.duration}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (item.sizeMb > 0) {
                        Text("Size: ${item.sizeMb} MB", style = MaterialTheme.typography.bodyMedium)
                    }
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
