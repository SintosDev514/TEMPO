package com.campusconnectplus.core.ui.components

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.campusconnectplus.ui.student.StudentBottomBar

@Composable
fun StudentScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = { StudentBottomBar(currentRoute = currentRoute, onNavigate = onNavigate) }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            content()
        }
    }
}
