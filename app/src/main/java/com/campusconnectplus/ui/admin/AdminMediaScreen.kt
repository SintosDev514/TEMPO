package com.campusconnectplus.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import coil.compose.AsyncImage
import com.campusconnectplus.core.ui.components.FloatingScrollbar
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.Media
import com.campusconnectplus.data.repository.MediaType
import com.campusconnectplus.feature_admin.media.AdminMediaViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminMediaScreen(vm: AdminMediaViewModel) {
    val mediaState by vm.media.collectAsState()
    val events by vm.events.collectAsState()
    var showUploadDialog by remember { mutableStateOf(false) }
    var linkingMedia by remember { mutableStateOf<Media?>(null) }
    
    val listState = rememberLazyListState()

    Scaffold { padding ->
        Column(Modifier.padding(padding)) {
            TopBar(
                title = "Media Gallery",
                subtitle = "Manage event photos and videos",
                onPrimary = { showUploadDialog = true }
            )

            Box(Modifier.weight(1f)) {
                if (mediaState.isEmpty()) {
                    EmptyAdminPanel(
                        icon = Icons.Outlined.Upload,
                        title = "No Media Found",
                        hint = "Upload your first photo or video using the 'Create' button above."
                    )
                } else {
                    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(mediaState.size) { index ->
                                val media = mediaState[index]
                                MediaAdminCard(
                                    media = media,
                                    onDelete = { vm.delete(media.id) },
                                    onLink = { linkingMedia = media }
                                )
                            }
                        }
                    }
                    FloatingScrollbar(listState = listState, modifier = Modifier.align(Alignment.CenterEnd))
                }
            }
        }
    }

    if (showUploadDialog) {
        UploadMediaDialog(
            events = events,
            onDismiss = { showUploadDialog = false },
            onUpload = { uri, title, type, eventId ->
                vm.uploadMedia(uri, title, type, eventId)
                showUploadDialog = false
            }
        )
    }

    linkingMedia?.let { media ->
        LinkMediaDialog(
            media = media,
            events = events,
            onDismiss = { linkingMedia = null },
            onConfirm = { eventId ->
                vm.updateMediaEvent(media, eventId)
                linkingMedia = null
            }
        )
    }
}

@Composable
private fun MediaAdminCard(
    media: Media,
    onDelete: () -> Unit,
    onLink: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminColors.Surface),
        border = BorderStroke(1.dp, AdminColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AdminColors.Background)
            ) {
                if (media.type == MediaType.IMAGE) {
                    AsyncImage(
                        model = media.url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.FileCopy, null, tint = AdminColors.Secondary)
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AdminColors.Dark
                )
                Text(
                    text = if (media.eventId.isNotBlank()) "Linked to Event" else "Independent Media",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (media.eventId.isNotBlank()) AdminColors.Primary else AdminColors.Secondary
                )
            }

            IconButton(onClick = onLink) {
                Icon(Icons.Outlined.Link, null, tint = AdminColors.Primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun UploadMediaDialog(
    events: List<Event>,
    onDismiss: () -> Unit,
    onUpload: (Uri, String, MediaType, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedUri = it
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminColors.HeaderBrush)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Upload Local Media",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Media Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Event Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = events.find { it.id == selectedEventId }?.title ?: "Select Event (Optional)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Link to Event") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (Independent Media)") },
                                onClick = {
                                    selectedEventId = null
                                    dropdownExpanded = false
                                }
                            )
                            events.forEach { event ->
                                DropdownMenuItem(
                                    text = { Text(event.title) },
                                    onClick = {
                                        selectedEventId = event.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .border(1.dp, AdminColors.Border, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.FileCopy, null, tint = AdminColors.Secondary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = selectedUri?.lastPathSegment ?: "No file selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedUri == null) AdminColors.Secondary else AdminColors.Dark
                            )
                        }
                    }

                    Button(
                        onClick = { launcher.launch("image/*,video/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Secondary.copy(alpha = 0.1f), contentColor = AdminColors.Primary)
                    ) {
                        Text("Select File from Device...")
                    }
                }

                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminColors.Background)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { 
                            selectedUri?.let { uri ->
                                val type = if (context.contentResolver.getType(uri)?.startsWith("video") == true) {
                                    MediaType.VIDEO 
                                } else {
                                    MediaType.IMAGE
                                }
                                onUpload(uri, title, type, selectedEventId)
                            } 
                        },
                        enabled = selectedUri != null,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Upload", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun LinkMediaDialog(
    media: Media,
    events: List<Event>,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var selectedEventId by remember { mutableStateOf<String?>(media.eventId.ifBlank { null }) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminColors.HeaderBrush)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Link, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Link Media to Event",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Adjust which event '${media.title}' belongs to.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = events.find { it.id == selectedEventId }?.title ?: "No Event (Independent)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Associated Event") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.LinkOff, null, Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Unlink from all events") 
                                    }
                                },
                                onClick = {
                                    selectedEventId = null
                                    dropdownExpanded = false
                                }
                            )
                            events.forEach { event ->
                                DropdownMenuItem(
                                    text = { Text(event.title) },
                                    onClick = {
                                        selectedEventId = event.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminColors.Background)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { onConfirm(selectedEventId) },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Link", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
