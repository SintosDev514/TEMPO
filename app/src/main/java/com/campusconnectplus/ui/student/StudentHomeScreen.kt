package com.campusconnectplus.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import coil.compose.AsyncImage
import com.campusconnectplus.core.ui.components.StatRingCanvas
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.feature_student.home.HomeStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    homeStats: StateFlow<HomeStats>,
    events: StateFlow<List<Event>>,
    isOnline: StateFlow<Boolean>,
    isRefreshing: StateFlow<Boolean>,
    onRefresh: () -> Unit,
    onQuickNavigateEvents: () -> Unit,
    onQuickNavigateMedia: () -> Unit,
    onQuickNavigateSaved: () -> Unit,
    onQuickNavigateAnnouncements: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    getMediaForEvent: (String) -> kotlinx.coroutines.flow.Flow<List<com.campusconnectplus.data.repository.Media>>
) {
    val stats by homeStats.collectAsState()
    val eventList by events.collectAsState()
    val online by isOnline.collectAsState()
    val refreshing by isRefreshing.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val activeEvents = stats.eventsCount
    val totalPhotos = stats.mediaCount
    val savedItems = stats.savedCount
    val announcementsCount = stats.announcementsCount

    val listState = rememberLazyListState()

    // UI improvement: hide default overscroll glow, keep design clean
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                item {
                    OfflineBanner(online)
                }
                item {
                    HomeHeader(
                        activeEvents = activeEvents,
                        totalPhotos = totalPhotos,
                        savedItems = savedItems,
                        announcementsCount = announcementsCount,
                        onAdminClick = onNavigateToAdmin,
                        onNotificationClick = onQuickNavigateAnnouncements,
                        isLandscape = isLandscape
                    )
                }

                item { Spacer(Modifier.height(14.dp)) }

                if (refreshing && eventList.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (eventList.isNotEmpty()) {
                    item {
                        Column {
                            SectionTitle("Event Highlights")
                            Spacer(Modifier.height(14.dp))
                            EventHighlightsCarousel(eventList.take(5), getMediaForEvent)
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }

                item {
                    Column {
                        SectionTitle("Quick Access")
                        Spacer(Modifier.height(10.dp))
                        QuickAccessGrid(
                            onEvents = onQuickNavigateEvents,
                            onMedia = onQuickNavigateMedia,
                            onSaved = onQuickNavigateSaved,
                            onAnnouncements = onQuickNavigateAnnouncements,
                            activeEvents = activeEvents,
                            totalPhotos = totalPhotos,
                            savedItems = savedItems,
                            announcementsCount = announcementsCount,
                            isLandscape = isLandscape
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }

                if (eventList.size > 5) {
                    item {
                        TrendingNowCard(eventList.drop(5).take(3))
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
            // UI improvement: custom slim scrollbar (won’t “damage” UI like side scroll indicator)
            SlimScrollbar(listState = listState, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
private fun OfflineBanner(isOnline: Boolean) {
    AnimatedVisibility(
        visible = !isOnline,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "You're offline. Showing local data.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    activeEvents: Int,
    totalPhotos: Int,
    savedItems: Int,
    announcementsCount: Int,
    onAdminClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    isLandscape: Boolean = false
) {
    val headerBrush = Brush.verticalGradient(
        listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBrush)
            .padding(top = if (isLandscape) 16.dp else 24.dp, bottom = if (isLandscape) 20.dp else 32.dp, start = 20.dp, end = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "CampusConnect+",
                    style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Welcome back, Student!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            // Canvas-drawn engagement ring (animation/canvas UI idea)
            val totalContent = maxOf(1, activeEvents + totalPhotos)
            StatRingCanvas(
                progress = (activeEvents.toFloat() / totalContent.toFloat()).coerceIn(0f, 1f),
                ringSize = if (isLandscape) 32.dp else 40.dp
            )
            Spacer(Modifier.width(if (isLandscape) 12.dp else 8.dp))
            
            Surface(
                onClick = onAdminClick,
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(if (isLandscape) 36.dp else 40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AdminPanelSettings, "Admin", tint = Color.White, modifier = Modifier.size(if (isLandscape) 18.dp else 20.dp))
                }
            }
            
            Spacer(Modifier.width(8.dp))

            Surface(
                onClick = onNotificationClick,
                color = Color.White.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(if (isLandscape) 36.dp else 40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Notifications, 
                        "Notifications", 
                        tint = Color.White, 
                        modifier = Modifier.size(if (isLandscape) 18.dp else 20.dp)
                    )
                    
                    if (announcementsCount > 0) {
                        Surface(
                            color = Color(0xFFEF4444),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp),
                            border = BorderStroke(1.5.dp, Color(0xFF1E3A8A))
                        ) {}
                    }
                }
            }
        }

        Spacer(Modifier.height(if (isLandscape) 16.dp else 28.dp))

        // Glassmorphism Stat Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatPill("Events", activeEvents.toString(), Icons.Outlined.CalendarMonth, Modifier.weight(1f), isLandscape)
            StatPill("Photos", totalPhotos.toString(), Icons.Outlined.PhotoLibrary, Modifier.weight(1f), isLandscape)
            StatPill("Saved", savedItems.toString(), Icons.Outlined.BookmarkBorder, Modifier.weight(1f), isLandscape)
        }
    }
}

@Composable
private fun StatPill(
    label: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF93C5FD), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 14.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF93C5FD), modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(6.dp))
                Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun EventHighlightsCarousel(
    events: List<Event>,
    getMediaForEvent: (String) -> kotlinx.coroutines.flow.Flow<List<com.campusconnectplus.data.repository.Media>>
) {
    val pagerState = rememberPagerState(pageCount = { events.size })
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % events.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 48.dp),
        pageSpacing = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) { index ->
        val event = events[index]
        val eventMedia by getMediaForEvent(event.id).collectAsState(initial = emptyList())
        val firstImageUrl = eventMedia.firstOrNull { it.type == com.campusconnectplus.data.repository.MediaType.IMAGE }?.url
        HighlightCard(
            event, 
            imageUrl = event.imageUrl ?: firstImageUrl,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HighlightCard(
    event: Event,
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(200.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp), clip = false),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
                        )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        event.category.name,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    event.title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        event.date,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        event.venue,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Reactions removed
                }
            }
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
    savedItems: Int,
    announcementsCount: Int,
    isLandscape: Boolean = false
) {
    Column(Modifier.padding(horizontal = 20.dp)) {
        if (isLandscape) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickTile(
                    title = "Events",
                    subtitle = "$activeEvents upcoming",
                    brush = Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))),
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = onEvents,
                    modifier = Modifier.weight(1f),
                    isLandscape = true
                )
                QuickTile(
                    title = "Media",
                    subtitle = "$totalPhotos new",
                    brush = Brush.verticalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))),
                    icon = Icons.Outlined.PhotoLibrary,
                    onClick = onMedia,
                    modifier = Modifier.weight(1f),
                    isLandscape = true
                )
                QuickTile(
                    title = "Saved",
                    subtitle = "$savedItems items",
                    brush = Brush.verticalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))),
                    icon = Icons.Outlined.BookmarkBorder,
                    onClick = onSaved,
                    modifier = Modifier.weight(1f),
                    isLandscape = true
                )
                QuickTile(
                    title = "Announce",
                    subtitle = "$announcementsCount updates",
                    brush = Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF1E293B))),
                    icon = Icons.Outlined.Campaign,
                    onClick = onAnnouncements,
                    modifier = Modifier.weight(1f),
                    isLandscape = true
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickTile(
                    title = "Events",
                    subtitle = "$activeEvents upcoming",
                    brush = Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))),
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = onEvents,
                    modifier = Modifier.weight(1f)
                )
                QuickTile(
                    title = "Media",
                    subtitle = "$totalPhotos new",
                    brush = Brush.verticalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))),
                    icon = Icons.Outlined.PhotoLibrary,
                    onClick = onMedia,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuickTile(
                    title = "Saved",
                    subtitle = "$savedItems items",
                    brush = Brush.verticalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))),
                    icon = Icons.Outlined.BookmarkBorder,
                    onClick = onSaved,
                    modifier = Modifier.weight(1f)
                )
                QuickTile(
                    title = "Announce",
                    subtitle = "$announcementsCount updates",
                    brush = Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF1E293B))),
                    icon = Icons.Outlined.Campaign,
                    onClick = onAnnouncements,
                    modifier = Modifier.weight(1f)
                )
            }
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
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    Surface(
        modifier = modifier.height(if (isLandscape) 90.dp else 120.dp),
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(brush)
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(if (isLandscape) 32.dp else 40.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(if (isLandscape) 18.dp else 20.dp))
                }
            }
            Column(Modifier.align(Alignment.BottomStart)) {
                Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, style = if (isLandscape) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium)
                if (!isLandscape) {
                    Text(subtitle, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun TrendingNowCard(trendingEvents: List<Event>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.TrendingUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Trending on Campus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(16.dp))
            trendingEvents.forEachIndexed { index, event ->
                TrendingItem(event.title, isLast = index == trendingEvents.size - 1)
            }
        }
    }
}

@Composable
private fun TrendingItem(text: String, isLast: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(6.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {}
        Spacer(Modifier.width(12.dp))
        Text(
            text, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    if (!isLast) {
        HorizontalDivider(modifier = Modifier.padding(start = 18.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }
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
                .offset(y = (progress * 400).dp)
                .height(54.dp)
                .width(4.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.Black.copy(alpha = 0.18f))
        )
    }
}
