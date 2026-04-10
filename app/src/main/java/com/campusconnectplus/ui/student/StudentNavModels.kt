package com.campusconnectplus.ui.student

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

sealed class StudentTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : StudentTab("student/home", "Home", Icons.Outlined.Home)
    data object Events : StudentTab("student/events", "Events", Icons.Outlined.CalendarMonth)
    data object Media : StudentTab("student/media", "Media", Icons.Outlined.PhotoLibrary)
    data object Saved : StudentTab("student/saved", "Saved", Icons.Outlined.BookmarkBorder)
    data object Announcements : StudentTab("student/announcements", "News", Icons.Outlined.Campaign)
}

val StudentTabs = listOf(
    StudentTab.Home,
    StudentTab.Events,
    StudentTab.Media,
    StudentTab.Announcements,
    StudentTab.Saved
)

@Composable
fun StudentBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        StudentTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab.route) },
                icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = tab.label) },
                label = { androidx.compose.material3.Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
