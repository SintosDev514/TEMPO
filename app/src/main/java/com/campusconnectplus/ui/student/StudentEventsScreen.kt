package com.campusconnectplus.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.campusconnectplus.core.ui.components.MediaDetailViewer
import com.campusconnectplus.core.ui.util.UiState
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventCategory
import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.feature_student.events.StudentEventsViewModel
import kotlinx.coroutines.launch

@Composable
fun StudentEventsScreen(vm: StudentEventsViewModel) {
    val eventsState by vm.eventsState.collectAsState()
    val isOnline by vm.isOnline.collectAsState()
    val favIds by vm.favoriteEventIds.collectAsState()
    val snackbarMessage by vm.snackbarMessage.collectAsState()
    val isRefreshing by vm.isRefreshing.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<EventCategory?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
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
            AnimatedVisibility(
                visible = !isOnline,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Outlined.CloudOff, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Offline mode: viewing saved data", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            // Header
            Surface(
                color = Color(0xFF1E3A8A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = if (isLandscape) 12.dp else 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Campus Events",
                            style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = { vm.refresh() },
                            enabled = !isRefreshing,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Outlined.Refresh, "Refresh", tint = Color.White)
                            }
                        }
                    }
                    if (!isLandscape) {
                        Spacer(Modifier.height(16.dp))
                    } else {
                        Spacer(Modifier.height(8.dp))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search events...", color = Color.White.copy(alpha = 0.6f)) },
                            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Color.White) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                        
                        if (isLandscape) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CategoryChip(
                                    label = "All",
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null }
                                )
                                EventCategory.entries.forEach { cat ->
                                    CategoryChip(
                                        label = cat.name,
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat }
                                    )
                                }
                            }
                        }
                    }
                    
                    if (!isLandscape) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryChip(
                                label = "All",
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null }
                            )
                            EventCategory.entries.forEach { cat ->
                                CategoryChip(
                                    label = cat.name,
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                            }
                        }
                    }
                }
            }

            // Results summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${filteredEvents.size} events found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Use the category chips to filter by event type")
                    }
                }) {
                    Icon(Icons.Outlined.FilterList, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Filter")
                }
            }

            // Content
            Box(Modifier.fillMaxSize()) {
                when (val s = eventsState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF1E3A8A))
                        }
                    }
                    is UiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(s.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is UiState.Success -> {
                        if (filteredEvents.isEmpty()) {
                            EmptyState()
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 340.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 100.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(filteredEvents, key = { it.id }) { event ->
                                    val eventMedia by vm.getMediaForEvent(event.id).collectAsState(initial = emptyList())
                                    val firstImageUrl = eventMedia.firstOrNull { it.type == com.campusconnectplus.data.repository.MediaType.IMAGE }?.url
                                    
                                    EventCard(
                                        event = event,
                                        isSaved = favIds.contains(event.id),
                                        onToggleFavorite = { vm.toggleFavorite(event.id) },
                                        onClick = { selectedEvent = event },
                                        imageUrl = event.imageUrl ?: firstImageUrl
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

    selectedMedia?.let { media ->
        MediaDetailViewer(
            item = media,
            onDownload = { vm.downloadMedia(it) },
            onDismiss = { selectedMedia = null }
        )
    }
}

@Composable
fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White.copy(alpha = 0.15f),
            labelColor = Color.White,
            selectedContainerColor = Color.White,
            selectedLabelColor = Color(0xFF1E3A8A)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.White.copy(alpha = 0.3f),
            selectedBorderColor = Color.White
        )
    )
}

@Composable
fun EmptyState() {

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.EventBusy, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Spacer(Modifier.height(24.dp))
        Text("No events found", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "We couldn't find any events matching your criteria. Try different filters or keywords.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
