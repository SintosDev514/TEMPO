package com.campusconnectplus.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campusconnectplus.data.repository.UserRole

enum class AdminRole { SystemAdmin, EventOrganizer, MediaTeam }

/** Tabs shown in the admin bottom bar based on role. Admin sees all; Staff (ORGANIZER) and Media see only their areas. */
fun tabsForRole(role: UserRole): List<AdminTab> = when (role) {
    UserRole.ADMIN -> AdminTabs
    UserRole.ORGANIZER -> listOf(AdminTab.Events, AdminTab.Announcements, AdminTab.Settings)
    UserRole.MEDIA_TEAM -> listOf(AdminTab.Media, AdminTab.Settings)
    else -> listOf(AdminTab.Settings)
}

sealed class AdminTab(
    val route: String,
    val label: String,
    /** Short label for bottom bar so 6 items fit without wrapping */
    val shortLabel: String,
    val icon: ImageVector
) {
    data object Dashboard : AdminTab("admin/dashboard", "Dashboard", "Home", Icons.Outlined.Dashboard)
    data object Events : AdminTab("admin/events", "Events", "Events", Icons.Outlined.Event)
    data object Media : AdminTab("admin/media", "Media", "Media", Icons.Outlined.Image)
    data object Announcements : AdminTab("admin/announcements", "Announcements", "News", Icons.Outlined.Campaign)
    data object Users : AdminTab("admin/users", "Users", "Users", Icons.Outlined.Group)
    data object Settings : AdminTab("admin/settings", "Settings", "Settings", Icons.Outlined.Settings)
}

val AdminTabs = listOf(
    AdminTab.Dashboard,
    AdminTab.Events,
    AdminTab.Media,
    AdminTab.Announcements,
    AdminTab.Users,
    AdminTab.Settings
)

@Composable
fun AdminBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    tabs: List<AdminTab> = AdminTabs
) {
    NavigationBar(tonalElevation = 0.dp) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab.route) },
                icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = tab.label) },
                label = {
                    Text(
                        text = tab.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
