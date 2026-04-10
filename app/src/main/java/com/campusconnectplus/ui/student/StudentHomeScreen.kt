package com.campusconnectplus.ui.student

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.campusconnectplus.core.ui.components.StatRingCanvas
import com.campusconnectplus.feature_student.home.HomeStats
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentHomeScreen(
    homeStats: StateFlow<HomeStats>,
    onQuickNavigateEvents: () -> Unit,
    onQuickNavigateMedia: () -> Unit,
    onQuickNavigateSaved: () -> Unit,
    onQuickNavigateAnnouncements: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
) {
    val stats by homeStats.collectAsState(initial = HomeStats())
    val activeEvents = stats.eventsCount
    val totalPhotos = stats.mediaCount
    val savedItems = stats.savedCount

    val listState = rememberLazyListState()

    // UI improvement: hide default overscroll glow, keep design clean
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                item {
                    HomeHeader(
                        activeEvents = activeEvents,
                        totalPhotos = totalPhotos,
                        savedItems = savedItems,
                        onAdminClick = onNavigateToAdmin
                    )
                }

                item { Spacer(Modifier.height(14.dp)) }

                item {
                    SectionTitle("Event Highlights")
                    Spacer(Modifier.height(10.dp))
                    EventHighlightsCarousel()
                }

                item { Spacer(Modifier.height(16.dp)) }

                item {
                    SectionTitle("Quick Access")
                    Spacer(Modifier.height(10.dp))
                    QuickAccessGrid(
                        onEvents = onQuickNavigateEvents,
                        onMedia = onQuickNavigateMedia,
                        onSaved = onQuickNavigateSaved,
                        onAnnouncements = onQuickNavigateAnnouncements,
                        activeEvents = activeEvents,
                        totalPhotos = totalPhotos,
                        savedItems = savedItems
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }

                item {
                    TrendingNowCard()
                }

                item { Spacer(Modifier.height(24.dp)) }
            }

            // UI improvement: custom slim scrollbar (won’t “damage” UI like side scroll indicator)
            SlimScrollbar(listState = listState, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
private fun HomeHeader(
    activeEvents: Int,
    totalPhotos: Int,
    savedItems: Int,
    onAdminClick: () -> Unit = {}
) {
    val headerBrush = Brush.verticalGradient(
        listOf(Color(0xFF1E3A8A), Color(0xFF2B59D9))
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBrush)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "CampusConnect+",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Welcome back, Student!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            // Canvas-drawn engagement ring (animation/canvas UI idea)
            val totalContent = maxOf(1, activeEvents + totalPhotos)
            val engagementProgress = (savedItems.toFloat() / totalContent).coerceIn(0f, 1f)
            StatRingCanvas(
                progress = engagementProgress,
                ringSize = 44.dp,
                strokeWidth = 4.dp,
                trackColor = Color.White.copy(alpha = 0.2f),
                progressColor = Color(0xFF8BE9FF)
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onAdminClick) {
                Icon(Icons.Outlined.AdminPanelSettings, contentDescription = "Admin panel", tint = Color.White)
            }
            val ctx = LocalContext.current
            IconButton(onClick = { Toast.makeText(ctx, "No new notifications", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color.White)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatPill("Active Events", activeEvents.toString(), Icons.Outlined.CalendarMonth)
            StatPill("Total Photos", totalPhotos.toString(), Icons.Outlined.PhotoLibrary)
            StatPill("Saved Items", savedItems.toString(), Icons.Outlined.BookmarkBorder)
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color(0xFF93C5FD))
        Spacer(Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EventHighlightsCarousel() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(3) { index ->
            HighlightCard(
                tag = if (index == 0) "Technology" else if (index == 1) "Cultural" else "Sports",
                title = if (index == 0) "Tech Innovation Summit 2026"
                else if (index == 1) "Cultural Night 2026"
                else "Sports Day Championship",
                date = if (index == 0) "Feb 15, 2026" else if (index == 1) "Feb 25, 2026" else "Feb 20, 2026",
                venue = if (index == 0) "Main Auditorium" else if (index == 1) "Open Theatre" else "Sports Complex"
            )
        }
    }
}

@Composable
private fun HighlightCard(
    tag: String,
    title: String,
    date: String,
    venue: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(310.dp)
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0B1220), Color(0xFF1F2A44))
                )
            )
            .padding(14.dp)
    ) {
        Column(Modifier.align(Alignment.BottomStart)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF00D1FF).copy(alpha = 0.25f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(tag, color = Color(0xFF8BE9FF), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("$date  •  $venue", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun QuickAccessGrid(
    onEvents: () -> Unit,
    onMedia: () -> Unit,
    onSaved: () -> Unit,
    onAnnouncements: () -> Unit,
    activeEvents: Int,
    totalPhotos: Int,
    savedItems: Int
) {
    Column(Modifier.padding(horizontal = 18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickTile(
                title = "Campus Events",
                subtitle = "$activeEvents upcoming",
                brush = Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2B59D9))),
                icon = Icons.Outlined.CalendarMonth,
                onClick = onEvents,
                modifier = Modifier.weight(1f)
            )
            QuickTile(
                title = "Media Gallery",
                subtitle = "$totalPhotos new photos",
                brush = Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF14B8A6))),
                icon = Icons.Outlined.PhotoLibrary,
                onClick = onMedia,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickTile(
                title = "Saved Content",
                subtitle = "$savedItems items offline",
                brush = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6))),
                icon = Icons.Outlined.BookmarkBorder,
                onClick = onSaved,
                modifier = Modifier.weight(1f)
            )
            QuickTile(
                title = "Announcements",
                subtitle = "5 new updates",
                brush = Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF475569))),
                icon = Icons.Outlined.Campaign,
                onClick = onAnnouncements,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickTile(
    title: String,
    subtitle: String,
    brush: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(110.dp),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(brush)
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White)
            }
            Column(Modifier.align(Alignment.BottomStart)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun TrendingNowCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Trending Now", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            TrendingItem("Student Council Elections 2026")
            TrendingItem("New Library Resources Available")
            TrendingItem("Campus WiFi Upgrade Complete")
        }
    }
}

@Composable
private fun TrendingItem(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(Modifier.height(8.dp))
}

/**
 * UI improvement:
 * - custom slim scrollbar for LazyColumn
 * - keeps design clean and consistent (no ugly side indicator)
 */
@Composable
private fun SlimScrollbar(
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    val total = max(1, listState.layoutInfo.totalItemsCount)
    val first = listState.firstVisibleItemIndex.coerceAtLeast(0)
    val progress = first.toFloat() / total.toFloat()

    Box(
        modifier = modifier
            .padding(end = 6.dp)
            .width(6.dp)
            .fillMaxHeight()
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(y = (progress * 420).dp) // tuned for phone feel
                .height(54.dp)
                .width(4.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.Black.copy(alpha = 0.18f))
        )
    }
}
