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
import coil.compose.AsyncImage
import com.campusconnectplus.core.ui.components.FloatingScrollbar
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.MediaType
import com.campusconnectplus.feature_admin.media.AdminMediaViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminMediaScreen(vm: AdminMediaViewModel) {
    val state = rememberLazyListState()
    val media by vm.media.collectAsState()
    val events by vm.events.collectAsState()
    var showUpload by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by vm.snackbarMessage.collectAsState()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSnackbarMessage()
        }
    }

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                Column(Modifier.fillMaxSize()) {
                    TopBar(
                        title = "Manage Media", 
                        subtitle = "${media.size} items • ${vm.totalSizeMb}MB total"
                    ) {
                        showUpload = true
                    }

                    Spacer(Modifier.height(10.dp))

                    if (media.isEmpty()) {
                        EmptyAdminPanel(
                            icon = Icons.Outlined.Upload,
                            title = "No media uploaded yet",
                            hint = "Click “Create” to upload photos and videos to your campus gallery."
                        )
                    } else {
                        LazyColumn(
                            state = state,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 84.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(media.size) { i ->
                                val m = media[i]
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = AdminColors.Surface),
                                    border = BorderStroke(1.dp, AdminColors.Border)
                                ) {
                                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = m.url,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFE2E8F0)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(m.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = "${m.sizeMb}MB • ${m.date}", 
                                                color = Color(0xFF64748B), 
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            if (m.eventId.isNotEmpty()) {
                                                val eventName = events.find { it.id == m.eventId }?.title ?: "Linked Event"
                                                Text(
                                                    text = "🔗 $eventName",
                                                    color = AdminColors.Primary,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                        IconButton(onClick = { vm.delete(m.id) }) {
                                            Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                FloatingScrollbar(
                    listState = state, 
                    modifier = Modifier.align(Alignment.CenterEnd)
                )

                if (showUpload) {
                    UploadMediaDialog(
                        events = events,
                        onDismiss = { showUpload = false },
                        onUpload = { uri, title, type, eventId ->
                            vm.uploadMedia(uri, title, type, eventId)
                            showUpload = false
                        }
                    )
                }
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Local Media") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Select File from Device...")
                }
            }
        },
        confirmButton = {
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
                colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary)
            ) {
                Icon(Icons.Outlined.Upload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Upload")
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Cancel") } 
        }
    )
}
