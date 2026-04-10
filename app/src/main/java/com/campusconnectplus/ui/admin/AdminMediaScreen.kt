package com.campusconnectplus.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusconnectplus.core.ui.components.FloatingScrollbar
import com.campusconnectplus.data.repository.MediaType
import com.campusconnectplus.feature_admin.media.AdminMediaViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminMediaScreen(vm: AdminMediaViewModel) {
    val state = rememberLazyListState()
    val media by vm.media.collectAsState()
    var showUpload by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                TopBar(title = "Manage Media", subtitle = "${media.size} items • ${vm.totalSizeMb}MB total") {
                    showUpload = true
                }

                Spacer(Modifier.height(10.dp))

                if (media.isEmpty()) {
                    EmptyAdminPanel(
                        iconText = "🖼️",
                        title = "No media uploaded yet",
                        hint = "Click “Create” to upload photos and videos from your device."
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
                            Card(shape = RoundedCornerShape(16.dp)) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(56.dp)
                                            .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(m.title, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Spacer(Modifier.height(4.dp))
                                        Text("${m.sizeMb}MB • ${m.date}", color = Color(0xFF64748B), style = MaterialTheme.typography.labelMedium)
                                    }
                                    IconButton(onClick = { vm.delete(m.id.toString()) }) {
                                        Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FloatingScrollbar(listState = state, modifier = Modifier.align(Alignment.CenterEnd))

            if (showUpload) {
                UploadMediaDialog(
                    onDismiss = { showUpload = false },
                    onUpload = { uri, title, type ->
                        vm.uploadMedia(uri, title, type)
                        showUpload = false
                    }
                )
            }
        }
    }
}

@Composable
private fun UploadMediaDialog(
    onDismiss: () -> Unit,
    onUpload: (Uri, String, MediaType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
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
                    modifier = Modifier.fillMaxWidth()
                )

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
                            text = selectedUri?.path?.substringAfterLast('/') ?: "No file selected",
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
                onClick = { selectedUri?.let { onUpload(it, title, if (context.contentResolver.getType(it)?.startsWith("video") == true) MediaType.VIDEO else MediaType.IMAGE) } },
                enabled = selectedUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary)
            ) {
                Icon(Icons.Outlined.Upload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Upload")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
