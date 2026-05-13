package com.campusconnectplus.ui.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.campusconnectplus.core.ui.components.FloatingScrollbar
import com.campusconnectplus.data.repository.Announcement
import com.campusconnectplus.data.repository.AnnouncementStatus
import com.campusconnectplus.feature_admin.announcements.AdminAnnouncementsViewModel

private fun priorityLabel(priority: Int): String = if (priority == 1) "important" else "normal"

private fun timeAgo(updatedAt: Long): String {
    val diff = System.currentTimeMillis() - updatedAt
    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminAnnouncementsScreen(vm: AdminAnnouncementsViewModel) {
    val state = rememberLazyListState()
    val announcements by vm.announcements.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var announcementToEdit by remember { mutableStateOf<com.campusconnectplus.data.repository.Announcement?>(null) }

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                TopBar(title = "Announcements & Updates", subtitle = "${announcements.size} announcements total") {
                    showCreate = true
                }

                Spacer(Modifier.height(10.dp))

                if (announcements.isEmpty()) {
                    EmptyAdminPanel(
                        icon = Icons.Outlined.Campaign,
                        title = "No announcements created yet",
                        hint = "Click “Create” to broadcast your first campus update."
                    )
                } else {
                    LazyColumn(
                        state = state,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 84.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(announcements.size) { i ->
                            val a = announcements[i]
                            Card(shape = RoundedCornerShape(16.dp)) {
                                Row(Modifier.padding(14.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        Text(a.title, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(4.dp))
                                        Text(a.content, color = Color(0xFF64748B), style = MaterialTheme.typography.bodySmall)
                                        Spacer(Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            AssistChip(onClick = {}, label = { Text(a.status.name.lowercase()) })
                                            AssistChip(onClick = {}, label = { Text(priorityLabel(a.priority)) })
                                            Text("• ${timeAgo(a.updatedAt)}", color = Color(0xFF64748B), style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                    IconButton(onClick = { announcementToEdit = a }) { Icon(Icons.Outlined.Edit, null) }
                                    IconButton(onClick = { vm.delete(a.id) }) { Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444)) }
                                }
                            }
                        }
                    }
                }
            }

            FloatingScrollbar(listState = state, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterEnd))

            if (showCreate) {
                CreateAnnouncementDialog(
                    onDismiss = { showCreate = false },
                    onCreate = { title, content, priority, status ->
                        vm.upsert(com.campusconnectplus.data.repository.Announcement(title = title, content = content, priority = if (priority == "Important") 1 else 0, status = when (status) { "Published" -> com.campusconnectplus.data.repository.AnnouncementStatus.ACTIVE; "Draft" -> com.campusconnectplus.data.repository.AnnouncementStatus.ARCHIVED; else -> com.campusconnectplus.data.repository.AnnouncementStatus.ACTIVE }))
                        showCreate = false
                    }
                )
            }
            announcementToEdit?.let { ann ->
                EditAnnouncementDialog(
                    initial = ann,
                    onDismiss = { announcementToEdit = null },
                    onSave = { updated ->
                        vm.upsert(updated)
                        announcementToEdit = null
                    }
                )
            }
        }
    }
}

@Composable
private fun CreateAnnouncementDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Normal") }
    var status by remember { mutableStateOf("Pending") }

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
                    Icon(Icons.Outlined.Campaign, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Create New Announcement",
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExposedDropdown("Priority Level", priority, listOf("Normal", "Important"), { priority = it }, Modifier.weight(1f))
                        ExposedDropdown("Status", status, listOf("Draft", "Pending", "Published"), { status = it }, Modifier.weight(1f))
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
                        onClick = { onCreate(title, content, priority, status) },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create Announcement", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditAnnouncementDialog(
    initial: Announcement,
    onDismiss: () -> Unit,
    onSave: (Announcement) -> Unit
) {
    var title by remember(initial.id) { mutableStateOf(initial.title) }
    var content by remember(initial.id) { mutableStateOf(initial.content) }
    var priority by remember(initial.id) { mutableStateOf(if (initial.priority == 1) "Important" else "Normal") }
    var status by remember(initial.id) {
        mutableStateOf(
            when (initial.status) {
                AnnouncementStatus.ACTIVE -> "Published"
                AnnouncementStatus.ARCHIVED -> "Draft"
            }
        )
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
                    Icon(Icons.Outlined.Campaign, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Edit Announcement",
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExposedDropdown("Priority Level", priority, listOf("Normal", "Important"), { priority = it }, Modifier.weight(1f))
                        ExposedDropdown("Status", status, listOf("Draft", "Pending", "Published"), { status = it }, Modifier.weight(1f))
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
                            onSave(
                                initial.copy(
                                    title = title,
                                    content = content,
                                    priority = if (priority == "Important") 1 else 0,
                                    status = when (status) {
                                        "Published" -> AnnouncementStatus.ACTIVE
                                        "Draft" -> AnnouncementStatus.ARCHIVED
                                        else -> AnnouncementStatus.ACTIVE
                                    },
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdown(
    label: String,
    value: String,
    items: List<String>,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = { onPick(it); expanded = false }
                )
            }
        }
    }
}
