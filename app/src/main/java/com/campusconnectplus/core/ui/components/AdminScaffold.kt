package com.campusconnectplus.core.ui.components

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.campusconnectplus.ui.admin.AdminBottomBar
import com.campusconnectplus.ui.admin.AdminTab
import com.campusconnectplus.ui.admin.tabsForRole

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

@Composable
fun AdminScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    allowedTabs: List<AdminTab>,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = { AdminBottomBar(currentRoute = currentRoute, onNavigate = onNavigate, tabs = allowedTabs) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            content()
        }
    }
}
