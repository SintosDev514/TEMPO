package com.campusconnectplus.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.campusconnectplus.core.util.Constants
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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

@Composable
fun AdminLoginScreen(
    authViewModel: com.campusconnectplus.feature_admin.auth.AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val authError by authViewModel.errorMessage.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    LaunchedEffect(username, password) { authViewModel.clearError() }

    fun validate(): Boolean {
        var isValid = true
        if (username.isBlank()) {
            usernameError = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username.trim()).matches()) {
            usernameError = "Enter a valid email address"
            isValid = false
        } else {
            usernameError = null
        }

        if (password.isBlank()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }
        return isValid
    }

    Surface(Modifier.fillMaxSize(), color = AdminColors.Background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Box(
                Modifier
                    .size(64.dp)
                    .background(AdminColors.Primary, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp)) }

            Spacer(Modifier.height(16.dp))
            Text("CampusConnect+", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = AdminColors.Dark)
            Text("Administrative Access Portal", color = AdminColors.Secondary)

            Spacer(Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminColors.Border)
            ) {
                Column(Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { 
                            username = it
                            if (usernameError != null) usernameError = null
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = usernameError != null,
                        supportingText = { usernameError?.let { Text(it) } },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            if (passwordError != null) passwordError = null
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(image, null)
                            }
                        },
                        isError = passwordError != null,
                        supportingText = { passwordError?.let { Text(it) } },
                        shape = RoundedCornerShape(12.dp)
                    )

                    authError?.let { msg ->
                        val friendlyMsg = when {
                            msg.contains("Invalid login credentials", ignoreCase = true) -> 
                                "Incorrect email or password. Please check your credentials and try again."
                            msg.contains("network", ignoreCase = true) || msg.contains("Unable to resolve host", ignoreCase = true) ->
                                "Network connection lost. Please check your internet connection."
                            msg.contains("maintenance", ignoreCase = true) || msg.contains("503", ignoreCase = true) || msg.contains("500", ignoreCase = true) ->
                                "The system is currently undergoing maintenance. Please try again later."
                            msg.contains("Too many requests", ignoreCase = true) || msg.contains("429", ignoreCase = true) ->
                                "Too many attempts. For security reasons, please try again in a few minutes."
                            else -> "An unexpected authentication error occurred. Please try again."
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = friendlyMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (validate()) authViewModel.login(username.trim(), password)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Sign In", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                "Authorized Access Only · v1.0.2",
                color = AdminColors.Secondary,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun AdminSignUpScreen(
    authViewModel: com.campusconnectplus.feature_admin.auth.AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val authError by authViewModel.errorMessage.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val signUpSuccess by authViewModel.signUpSuccess.collectAsState()
    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            authViewModel.clearSignUpSuccess()
            onNavigateToLogin()
        }
    }

    fun validate(): Boolean {
        var isValid = true
        if (name.isBlank()) { nameError = "Name is required"; isValid = false } else nameError = null
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { 
            emailError = "Valid email is required"; isValid = false 
        } else emailError = null
        if (password.length < 6) { passwordError = "Min 6 characters required"; isValid = false } else passwordError = null
        return isValid
    }

    Surface(Modifier.fillMaxSize(), color = AdminColors.Background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            Text("Create Admin Account", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = AdminColors.Dark)
            Text("Register for the administrative portal", color = AdminColors.Secondary)
            Spacer(Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, AdminColors.Border)
            ) {
                Column(Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = null },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError != null,
                        supportingText = { nameError?.let { Text(it) } },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null,
                        supportingText = { emailError?.let { Text(it) } },
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = { passwordError?.let { Text(it) } },
                        shape = RoundedCornerShape(12.dp)
                    )
                    authError?.let { msg ->
                        val friendlyMsg = when {
                            msg.contains("Invalid login credentials", ignoreCase = true) -> 
                                "Incorrect email or password. Please check your credentials and try again."
                            msg.contains("network", ignoreCase = true) || msg.contains("Unable to resolve host", ignoreCase = true) ->
                                "Network connection lost. Please check your internet connection."
                            msg.contains("maintenance", ignoreCase = true) || msg.contains("503", ignoreCase = true) || msg.contains("500", ignoreCase = true) ->
                                "The system is currently undergoing maintenance. Please try again later."
                            msg.contains("Too many requests", ignoreCase = true) || msg.contains("429", ignoreCase = true) ->
                                "Too many attempts. For security reasons, please try again in a few minutes."
                            else -> "An unexpected authentication error occurred. Please try again."
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = friendlyMsg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (validate()) {
                                authViewModel.signUp(
                                    com.campusconnectplus.data.repository.UserRole.ORGANIZER,
                                    name,
                                    email,
                                    password
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AdminColors.Primary)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Create Account", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onNavigateToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Already have an account? Sign In", style = MaterialTheme.typography.bodySmall, color = AdminColors.Primary)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
