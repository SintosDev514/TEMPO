package com.campusconnectplus.ui.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.campusconnectplus.core.ui.components.FloatingScrollbar
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventCategory
import com.campusconnectplus.feature_admin.events.AdminEventsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminEventsScreen(vm: AdminEventsViewModel) {
    val state = rememberLazyListState()
    val events by vm.events.collectAsState()
    val snackbarMessage by vm.snackbarMessage.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var eventToEdit by remember { mutableStateOf<com.campusconnectplus.data.repository.Event?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.clearSnackbarMessage()
        }
    }

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(Modifier.fillMaxSize()) {
                    TopBar(
                        title = "Manage Events",
                        subtitle = "${events.size} events stored locally"
                    ) {
                        showCreate = true
                    }

                    Spacer(Modifier.height(10.dp))

                    if (events.isEmpty()) {
                        EmptyAdminPanel(
                            iconText = "📅",
                            title = "No events created yet",
                            hint = "Click “Add” to create your first event."
                        )
                    } else {
                        LazyColumn(
                            state = state,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 12.dp,
                                end = 16.dp,
                                bottom = 84.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(events.size) { i ->
                                val e = events[i]
                                Card(shape = RoundedCornerShape(16.dp)) {
                                    Column(Modifier.padding(14.dp)) {
                                        Row(Modifier.fillMaxSize()) {
                                            Column(Modifier.weight(1f)) {
                                                Text(e.title, fontWeight = FontWeight.Bold)
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    e.date,
                                                    color = Color(0xFF64748B),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                                Text(
                                                    e.venue,
                                                    color = Color(0xFF64748B),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }
                                            IconButton(onClick = {
                                                eventToEdit = e
                                            }) {
                                                Icon(Icons.Outlined.Edit, contentDescription = null)
                                            }
                                            IconButton(onClick = { vm.delete(e.id.toString()) }) {
                                                Icon(
                                                    Icons.Outlined.Delete,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        AssistChip(onClick = {}, label = { Text(e.category.name) })
                                        Spacer(Modifier.height(8.dp))
                                        Text(e.description, color = Color(0xFF334155))
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

                if (showCreate) {
                    CreateEventDialog(
                        onDismiss = { showCreate = false },
                        onCreate = { title, date, time, location, category, desc ->
                            vm.upsert(
                                com.campusconnectplus.data.repository.Event(
                                    title = title,
                                    date = if (time.isNotBlank()) "$date $time" else date,
                                    venue = location,
                                    description = desc,
                                    category = when (category) {
                                        "Cultural" -> com.campusconnectplus.data.repository.EventCategory.CULTURAL
                                        "Sports" -> com.campusconnectplus.data.repository.EventCategory.SPORTS
                                        else -> com.campusconnectplus.data.repository.EventCategory.ACADEMIC
                                    }
                                )
                            )
                            showCreate = false
                        }
                    )
                }
                eventToEdit?.let { event ->
                    EditEventDialog(
                        initial = event,
                        onDismiss = { eventToEdit = null },
                        onSave = { updated ->
                            vm.upsert(updated)
                            eventToEdit = null
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Academic") }
    var desc by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val dateState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var dateText by remember { mutableStateOf("") }

    val timeState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    var timeText by remember { mutableStateOf("") }

    fun validate(): Boolean {
        var isValid = true
        if (title.isBlank()) { titleError = "Title is required"; isValid = false } else titleError = null
        if (dateText.isBlank()) { dateError = "Date is required"; isValid = false } else dateError = null
        if (location.isBlank()) { locationError = "Location is required"; isValid = false } else locationError = null
        return isValid
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AdminColors.Primary,
        focusedLabelColor = AdminColors.Primary,
        cursorColor = AdminColors.Primary,
        focusedLeadingIconColor = AdminColors.Primary,
        unfocusedLeadingIconColor = AdminColors.Secondary,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminColors.HeaderBrush)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Create New Event",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; titleError = null },
                        label = { Text("Event Title") },
                        leadingIcon = { Icon(Icons.Outlined.Event, contentDescription = null, Modifier.size(22.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = titleError != null,
                        supportingText = { titleError?.let { Text(it) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = dateText,
                            onValueChange = {},
                            label = { Text("Date") },
                            leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null, Modifier.size(22.dp)) },
                            modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                            enabled = false,
                            isError = dateError != null,
                            supportingText = { dateError?.let { Text(it) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = if (dateError != null) MaterialTheme.colorScheme.error else AdminColors.Border,
                                disabledLabelColor = if (dateError != null) MaterialTheme.colorScheme.error else AdminColors.Secondary,
                                disabledLeadingIconColor = AdminColors.Secondary
                            )
                        )
                        OutlinedTextField(
                            value = timeText,
                            onValueChange = {},
                            label = { Text("Time") },
                            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null, Modifier.size(22.dp)) },
                            modifier = Modifier.weight(1f).clickable { showTimePicker = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = AdminColors.Border,
                                disabledLabelColor = AdminColors.Secondary,
                                disabledLeadingIconColor = AdminColors.Secondary
                            )
                        )
                    }
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it; locationError = null },
                        label = { Text("Location / Venue") },
                        leadingIcon = { Icon(Icons.Outlined.Place, contentDescription = null, Modifier.size(22.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = locationError != null,
                        supportingText = { locationError?.let { Text(it) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Outlined.Category, contentDescription = null, Modifier.size(22.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        leadingIcon = { Icon(Icons.Outlined.Subject, contentDescription = null, Modifier.size(22.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminColors.Background)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminColors.Primary),
                        border = BorderStroke(1.dp, AdminColors.Border),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { if (validate()) onCreate(title, dateText, timeText, location, category, desc) },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary),
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Create Event", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        dateText = sdf.format(Date(it))
                        dateError = null
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                    cal.set(Calendar.MINUTE, timeState.minute)
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    timeText = sdf.format(cal.time)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timeState)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEventDialog(
    initial: Event,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit
) {
    var title by remember(initial.id) { mutableStateOf(initial.title) }
    var location by remember(initial.id) { mutableStateOf(initial.venue) }
    var category by remember(initial.id) {
        mutableStateOf(
            when (initial.category) {
                EventCategory.ACADEMIC -> "Academic"
                EventCategory.CULTURAL -> "Cultural"
                EventCategory.SPORTS -> "Sports"
            }
        )
    }
    var desc by remember(initial.id) { mutableStateOf(initial.description) }
    var dateText by remember(initial.id) { mutableStateOf(initial.date) }
    var timeText by remember(initial.id) { mutableStateOf("") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val dateState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        var isValid = true
        if (title.isBlank()) { titleError = "Title is required"; isValid = false } else titleError = null
        if (dateText.isBlank()) { dateError = "Date is required"; isValid = false } else dateError = null
        if (location.isBlank()) { locationError = "Location is required"; isValid = false } else locationError = null
        return isValid
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AdminColors.Primary,
        focusedLabelColor = AdminColors.Primary,
        cursorColor = AdminColors.Primary,
        focusedLeadingIconColor = AdminColors.Primary,
        unfocusedLeadingIconColor = AdminColors.Secondary,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f).clip(RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminColors.HeaderBrush)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text("Edit Event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; titleError = null },
                        label = { Text("Event Title") },
                        leadingIcon = { Icon(Icons.Outlined.Event, contentDescription = null, Modifier.size(22.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = titleError != null,
                        supportingText = { titleError?.let { Text(it) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = dateText,
                            onValueChange = {},
                            label = { Text("Date") },
                            leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null, Modifier.size(22.dp)) },
                            modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                            enabled = false,
                            isError = dateError != null,
                            supportingText = { dateError?.let { Text(it) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = if (dateError != null) MaterialTheme.colorScheme.error else AdminColors.Border,
                                disabledLabelColor = if (dateError != null) MaterialTheme.colorScheme.error else AdminColors.Secondary,
                                disabledLeadingIconColor = AdminColors.Secondary
                            )
                        )
                        OutlinedTextField(
                            value = timeText,
                            onValueChange = {},
                            label = { Text("Time") },
                            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null, Modifier.size(22.dp)) },
                            modifier = Modifier.weight(1f).clickable { showTimePicker = true },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = AdminColors.Border,
                                disabledLabelColor = AdminColors.Secondary,
                                disabledLeadingIconColor = AdminColors.Secondary
                            )
                        )
                    }
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it; locationError = null },
                        label = { Text("Location / Venue") },
                        leadingIcon = { Icon(Icons.Outlined.Place, contentDescription = null, Modifier.size(22.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = locationError != null,
                        supportingText = { locationError?.let { Text(it) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Outlined.Category, contentDescription = null, Modifier.size(22.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        leadingIcon = { Icon(Icons.Outlined.Subject, contentDescription = null, Modifier.size(22.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(AdminColors.Background).padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AdminColors.Primary),
                        border = BorderStroke(1.dp, AdminColors.Border),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Cancel", fontWeight = FontWeight.Medium) }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (validate()) {
                                onSave(
                                    initial.copy(
                                        title = title,
                                        date = if (timeText.isNotBlank()) "$dateText $timeText" else dateText,
                                        venue = location,
                                        description = desc,
                                        category = when (category) {
                                            "Cultural" -> EventCategory.CULTURAL
                                            "Sports" -> EventCategory.SPORTS
                                            else -> EventCategory.ACADEMIC
                                        },
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary),
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Update Event", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        dateText = sdf.format(Date(it))
                        dateError = null
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dateState) }
    }
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                    cal.set(Calendar.MINUTE, timeState.minute)
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    timeText = sdf.format(cal.time)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timeState) }
        )
    }
}
