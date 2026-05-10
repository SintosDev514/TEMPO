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
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.campusconnectplus.core.ui.components.MediaDetailViewer
import com.campusconnectplus.core.ui.util.UiState
import com.campusconnectplus.data.repository.EventCategory
import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.feature_student.events.StudentEventsViewModel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight

@Composable
fun StudentEventsScreen(vm: StudentEventsViewModel) {
    val eventsState by vm.eventsState.collectAsState()
    val favIds by vm.favoriteEventIds.collectAsState()
    val snackbarMessage by vm.snackbarMessage.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<EventCategory?>(null) }
    var selectedEvent by remember { mutableStateOf<com.campusconnectplus.data.repository.Event?>(null) }
    var selectedMedia by remember { mutableStateOf<Media?>(null) }

    val events = when (val s = eventsState) {
        is UiState.Success -> s.data
        else -> emptyList()
    }
    val filteredEvents = remember(events, searchQuery, selectedCategory) {
        events.filter { event ->
            (searchQuery.isBlank() || event.title.contains(searchQuery, ignoreCase = true)) &&
            (selectedCategory == null || event.category == selectedCategory)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.clearSnackbarMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2B59D9))))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Campus Events",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    IconButton(
                        onClick = { vm.refresh() },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search events...", color = Color.White.copy(alpha = 0.7f)) },
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
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            labelColor = Color.White,
                            selectedContainerColor = Color.White,
                            selectedLabelColor = Color(0xFF1E3A8A)
                        )
                    )
                    EventCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                labelColor = Color.White,
                                selectedContainerColor = Color.White,
                                selectedLabelColor = Color(0xFF1E3A8A)
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${filteredEvents.size} events found",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    TextButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Use category chips above to filter by type")
                        }
                    }) {
                        Icon(Icons.Outlined.FilterList, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Filter", color = Color.White)
                    }
                }
            }

            // Content
            Box(Modifier.fillMaxSize().padding(16.dp)) {
                when (val s = eventsState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF1E3A8A))
                        }
                    }
                    is UiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(s.message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is UiState.Success -> {
                        if (filteredEvents.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No events available.", style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp),
                                verticalArrangement = Arrangement.spacedBy(22.dp)
                            ) {

                                items(filteredEvents, key = { it.id }) { event ->

                                    val isSaved = favIds.contains(event.id)

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(28.dp),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 10.dp
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        onClick = { selectedEvent = event }
                                    ) {

                                        Column {

                                            // HEADER IMAGE AREA
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(210.dp)
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(
                                                                Color(0xFF1E3A8A),
                                                                Color(0xFF2563EB),
                                                                Color(0xFF60A5FA)
                                                            )
                                                        )
                                                    )
                                            ) {

                                                // TOP BADGES
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {

                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = Color.White.copy(alpha = 0.18f)
                                                    ) {
                                                        Text(
                                                            text = event.category.name,
                                                            modifier = Modifier.padding(
                                                                horizontal = 14.dp,
                                                                vertical = 7.dp
                                                            ),
                                                            color = Color.White,
                                                            style = MaterialTheme.typography.labelMedium
                                                        )
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = Color.White.copy(alpha = 0.18f)
                                                    ) {
                                                        IconButton(
                                                            onClick = { vm.toggleFavorite(event.id) }
                                                        ) {
                                                            Icon(
                                                                imageVector =
                                                                    if (isSaved)
                                                                        Icons.Filled.Bookmark
                                                                    else
                                                                        Icons.Outlined.BookmarkBorder,
                                                                contentDescription = null,
                                                                tint = Color.White
                                                            )
                                                        }
                                                    }
                                                }

                                                // EVENT TITLE
                                                Column(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(20.dp)
                                                ) {

                                                    Text(
                                                        text = event.title,
                                                        style = MaterialTheme.typography.headlineSmall,
                                                        color = Color.White,
                                                        maxLines = 2
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {

                                                        Surface(
                                                            shape = RoundedCornerShape(50),
                                                            color = Color.White.copy(alpha = 0.15f)
                                                        ) {
                                                            Text(
                                                                text = event.date,
                                                                modifier = Modifier.padding(
                                                                    horizontal = 12.dp,
                                                                    vertical = 6.dp
                                                                ),
                                                                color = Color.White,
                                                                style = MaterialTheme.typography.labelMedium
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Surface(
                                                            shape = RoundedCornerShape(50),
                                                            color = Color.White.copy(alpha = 0.15f)
                                                        ) {
                                                            Text(
                                                                text = event.venue,
                                                                modifier = Modifier.padding(
                                                                    horizontal = 12.dp,
                                                                    vertical = 6.dp
                                                                ),
                                                                color = Color.White,
                                                                style = MaterialTheme.typography.labelMedium
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // CONTENT
                                            Column(
                                                modifier = Modifier.padding(20.dp)
                                            ) {

                                                Text(
                                                    text = event.description,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 4
                                                )

                                                Spacer(modifier = Modifier.height(18.dp))

                                                HorizontalDivider(
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                                )

                                                Spacer(modifier = Modifier.height(16.dp))

                                                // BOTTOM ACTIONS
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
                                                            color = Color(0xFFE0E7FF)
                                                        ) {
                                                            Text(
                                                                text = "Trending Event",
                                                                modifier = Modifier.padding(
                                                                    horizontal = 12.dp,
                                                                    vertical = 6.dp
                                                                ),
                                                                color = Color(0xFF1D4ED8),
                                                                style = MaterialTheme.typography.labelMedium
                                                            )
                                                        }
                                                    }

                                                    FilledTonalButton(
                                                        onClick = { selectedEvent = event },
                                                        shape = RoundedCornerShape(14.dp)
                                                    ) {
                                                        Text("View Details")
                                                    }
                                                }
                                            }
                                        }
                                    }
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
            title = { Text(event.title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = {
                Column {
                    Text("Date: ${event.date}", style = MaterialTheme.typography.bodyMedium)
                    Text("Venue: ${event.venue}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(event.description, style = MaterialTheme.typography.bodyLarge)

                    // Event Media Section
                    val eventMedia by vm.getMediaForEvent(event.id).collectAsState(initial = emptyList())
                    if (eventMedia.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Event Media", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 16.dp)
                        ) {
                            items(eventMedia) { media ->
                                Card(
                                    modifier = Modifier.size(110.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = { selectedMedia = media }
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
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEvent = null }) {
                    Text("Close")
                }
            }
        )
    }

    selectedMedia?.let { media ->
        MediaDetailViewer(
            item = media,
            onDownload = { vm.downloadMedia(it) },
            onDismiss = { selectedMedia = null }
        )
    }
}
