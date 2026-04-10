package com.campusconnectplus.ui.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.campusconnectplus.core.di.LocalAppContainer
import com.campusconnectplus.core.di.ServiceLocatorSeed
import com.campusconnectplus.core.ui.components.FloatingScrollbar
import com.campusconnectplus.data.repository.AuthResult
import com.campusconnectplus.data.repository.UserRole
import com.campusconnectplus.feature_admin.users.AdminUsersViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminUsersScreen(vm: AdminUsersViewModel) {
    val state = rememberLazyListState()
    val users by vm.users.collectAsState()
    var query by remember { mutableStateOf("") }
    var showCreateUser by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<Pair<Long, String>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val container = LocalAppContainer.current

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            Column(Modifier.fillMaxSize()) {
                TopBar(
                    title = "User & Role Management",
                    subtitle = "${users.size} total users",
                    onPrimary = { showCreateUser = true }
                )

                Spacer(Modifier.height(10.dp))

                // summary cards row
                Row(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBox("Total", vm.totalCount().toString(), Modifier.weight(1f))
                    StatBox("Active", vm.activeCount().toString(), Modifier.weight(1f))
                    StatBox("Admins", vm.adminCount().toString(), Modifier.weight(1f))
                    StatBox("Students", vm.studentCount().toString(), Modifier.weight(1f))
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; vm.setSearch(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Search users...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(10.dp))

                LazyColumn(
                    state = state,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users.size) { i ->
                        val u = users[i]
                        UserCard(
                            name = u.name,
                            email = u.email,
                            role = roleDisplay(u.role),
                            active = u.active,
                            canChangeRole = u.email != ServiceLocatorSeed.DEFAULT_ADMIN_EMAIL,
                            canDelete = u.email != ServiceLocatorSeed.DEFAULT_ADMIN_EMAIL,
                            onRoleChange = { vm.setRole(u.id, it) },
                            onToggleActive = { vm.toggleActive(u.id) },
                            onDelete = { userToDelete = u.id to u.email }
                        )
                    }
                }
            }

            FloatingScrollbar(listState = state, modifier = Modifier.align(Alignment.CenterEnd))
        }

        if (showCreateUser) {
            CreateUserDialog(
                onDismiss = { showCreateUser = false },
                onCreate = { role, name, email, password ->
                    scope.launch {
                        when (val result = container.authRepository.signUp(role, name, email, password)) {
                            is AuthResult.Success -> {
                                snackbarHostState.showSnackbar("Account created for $email")
                                showCreateUser = false
                            }
                            is AuthResult.Error -> {
                                snackbarHostState.showSnackbar(result.message)
                            }
                        }
                    }
                }
            )
        }

        userToDelete?.let { (id, email) ->
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Delete account?") },
                text = { Text("Permanently delete the account for $email? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            vm.delete(id)
                            userToDelete = null
                            scope.launch { snackbarHostState.showSnackbar("Account deleted") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) { Text("Cancel") }
                }
            )
        }
        }
    }
}

private fun roleDisplay(r: UserRole): String = when (r) {
    UserRole.ADMIN -> "Admin"
    UserRole.MEDIA_TEAM -> "Media Team"
    UserRole.ORGANIZER -> "Student Org"
    UserRole.STUDENT -> "Student"
}

private const val PASSWORD_MIN_LENGTH = 8
private val PASSWORD_UPPER = Regex("[A-Z]")
private val PASSWORD_LOWER = Regex("[a-z]")
private val PASSWORD_DIGIT = Regex("[0-9]")
private val PASSWORD_SPECIAL = Regex("[!@#\$%^&*()_+\\-=\\[\\]{}|;':\",./<>?\\\\`~]")

/** Returns null if password is strong, otherwise a short error message. */
private fun validateStrongPassword(password: String): String? {
    if (password.length < PASSWORD_MIN_LENGTH)
        return "At least $PASSWORD_MIN_LENGTH characters"
    if (!PASSWORD_UPPER.containsMatchIn(password))
        return "Add an uppercase letter"
    if (!PASSWORD_LOWER.containsMatchIn(password))
        return "Add a lowercase letter"
    if (!PASSWORD_DIGIT.containsMatchIn(password))
        return "Add a number"
    if (!PASSWORD_SPECIAL.containsMatchIn(password))
        return "Add a special character (!@#\$%^&* etc.)"
    return null
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, fontWeight = FontWeight.Bold)
            Text(label, color = Color(0xFF64748B), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserCard(
    name: String,
    email: String,
    role: String,
    active: Boolean,
    canChangeRole: Boolean,
    canDelete: Boolean,
    onRoleChange: (String) -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(name, fontWeight = FontWeight.Bold)
            Text(email, color = Color(0xFF64748B), style = MaterialTheme.typography.labelMedium)

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AssistChip(onClick = {}, label = { Text(role) })
                AssistChip(
                    onClick = {},
                    label = { Text(if (active) "active" else "inactive") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (active) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFE2E8F0)
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // role dropdown (fixed for default admin to prevent changing to Media/Staff)
            if (canChangeRole) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Student Org", "Faculty Staff", "Media Team", "Admin").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = { onRoleChange(it); expanded = false }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = role,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Role") }
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canDelete) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete account")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                OutlinedButton(onClick = onToggleActive) {
                    Text(if (active) "Deactivate" else "Activate")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreate: (UserRole, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf(UserRole.ORGANIZER) }
    var roleExpanded by remember { mutableStateOf(false) }
    val roleOptions = listOf(
        UserRole.ORGANIZER to "Staff (Student Org)",
        UserRole.MEDIA_TEAM to "Media Team"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create account (Staff / Media)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = roleOptions.first { it.first == role }.second,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        roleOptions.forEach { (r, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { role = r; roleExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = password.isNotEmpty() && validateStrongPassword(password) != null,
                    supportingText = {
                        val err = validateStrongPassword(password)
                        if (err != null && password.isNotEmpty()) {
                            Text(err)
                        } else {
                            Text("Min $PASSWORD_MIN_LENGTH chars, 1 uppercase, 1 lowercase, 1 number, 1 special (!@#\$%^&*…)")
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            val passwordValid = validateStrongPassword(password) == null
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && passwordValid) {
                        onCreate(role, name.trim(), email.trim(), password)
                    }
                },
                enabled = name.isNotBlank() && email.isNotBlank() && passwordValid
            ) {
                Text("Create account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
