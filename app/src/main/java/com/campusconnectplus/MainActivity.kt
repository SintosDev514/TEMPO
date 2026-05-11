package com.campusconnectplus

import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campusconnectplus.core.ui.components.AdminScaffold
import com.campusconnectplus.core.ui.components.StudentScaffold
import com.campusconnectplus.core.util.DebugLog
import com.campusconnectplus.data.repository.UserRole
import com.campusconnectplus.ui.admin.*
import com.campusconnectplus.ui.student.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Use string literals to check results to avoid lint issues with SDK-specific constants
        val imagesGranted = permissions["android.permission.READ_MEDIA_IMAGES"] ?: false
        val videoGranted = permissions["android.permission.READ_MEDIA_VIDEO"] ?: false
        val visualUserSelected = permissions["android.permission.READ_MEDIA_VISUAL_USER_SELECTED"] ?: false
        val storageGranted = permissions["android.permission.READ_EXTERNAL_STORAGE"] ?: false

        if (imagesGranted && videoGranted) {
            DebugLog.log("MainActivity", "Full media access granted", emptyMap(), "H1")
        } else if (visualUserSelected) {
            DebugLog.log("MainActivity", "Partial media access granted (User Selected)", emptyMap(), "H1")
        } else if (storageGranted) {
            DebugLog.log("MainActivity", "Legacy storage access granted", emptyMap(), "H1")
        } else {
            DebugLog.log("MainActivity", "Some permissions denied", emptyMap(), "H1")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DebugLog.init(this)

        checkAndRequestPermissions()

        setContent {
            val context = LocalContext.current
            val authVm: com.campusconnectplus.feature_admin.auth.AuthViewModel = hiltViewModel(context as ComponentActivity)
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()

                    NavHost(
                        navController = nav,
                        startDestination = StudentTab.Home.route,
                        enterTransition = {
                            if (targetState.destination.route == "admin/login") fadeIn() else EnterTransition.None
                        },
                        exitTransition = {
                            if (initialState.destination.route == "admin/login") fadeOut() else ExitTransition.None
                        },
                        popEnterTransition = {
                            if (targetState.destination.route == "admin/login") fadeIn() else EnterTransition.None
                        },
                        popExitTransition = {
                            if (initialState.destination.route == "admin/login") fadeOut() else ExitTransition.None
                        }
                    ) {

                        composable(StudentTab.Home.route) {
                            val homeVm: com.campusconnectplus.feature_student.home.StudentHomeViewModel = hiltViewModel()
                            LaunchedEffect(Unit) { DebugLog.log("MainActivity.kt:Home", "Student home composed", emptyMap(), "H1") }
                            Box(Modifier.fillMaxSize()) {
                                StudentScaffold(
                                    currentRoute = StudentTab.Home.route,
                                    onNavigate = nav::navigate
                                ) {
                                    StudentHomeScreen(
                                        homeStats = homeVm.homeStats,
                                        events = homeVm.events,
                                        isOnline = homeVm.isOnline,
                                        onQuickNavigateEvents = { nav.navigate(StudentTab.Events.route) },
                                        onQuickNavigateMedia = { nav.navigate(StudentTab.Media.route) },
                                        onQuickNavigateSaved = { nav.navigate(StudentTab.Saved.route) },
                                        onQuickNavigateAnnouncements = { nav.navigate(StudentTab.Announcements.route) },
                                        onNavigateToAdmin = { nav.navigate("admin/login") },
                                        getMediaForEvent = { homeVm.getMediaForEvent(it) }
                                    )
                                }
                            }
                        }

                        composable(StudentTab.Events.route) {
                            val studentEventsVm: com.campusconnectplus.feature_student.events.StudentEventsViewModel = hiltViewModel()
                            Box(Modifier.fillMaxSize()) {
                                StudentScaffold(
                                    currentRoute = StudentTab.Events.route,
                                    onNavigate = nav::navigate
                                ) {
                                    StudentEventsScreen(vm = studentEventsVm)
                                }
                            }
                        }

                        composable(StudentTab.Media.route) {
                            val studentMediaVm: com.campusconnectplus.feature_student.media.StudentMediaViewModel = hiltViewModel()
                            Box(Modifier.fillMaxSize()) {
                                StudentScaffold(
                                    currentRoute = StudentTab.Media.route,
                                    onNavigate = nav::navigate
                                ) {
                                    StudentMediaScreen(vm = studentMediaVm)
                                }
                            }
                        }

                        composable(StudentTab.Saved.route) {
                            val studentSavedVm: com.campusconnectplus.feature_student.saved.StudentSavedViewModel = hiltViewModel()
                            Box(Modifier.fillMaxSize()) {
                                StudentScaffold(currentRoute = StudentTab.Saved.route, onNavigate = nav::navigate) {
                                    StudentSavedScreen(vm = studentSavedVm)
                                }
                            }
                        }

                        composable(StudentTab.Announcements.route) {
                            val studentAnnVm: com.campusconnectplus.feature_student.announcements.StudentAnnouncementsViewModel = hiltViewModel()
                            Box(Modifier.fillMaxSize()) {
                                StudentScaffold(
                                    currentRoute = StudentTab.Announcements.route,
                                    onNavigate = nav::navigate
                                ) {
                                    StudentAnnouncementsScreen(vm = studentAnnVm)
                                }
                            }
                        }

                        composable("admin/login") {
                            val currentUser by authVm.currentUser.collectAsState()
                            LaunchedEffect(currentUser) {
                                if (currentUser != null) {
                                    val startRoute = when (currentUser!!.role) {
                                        UserRole.ADMIN -> AdminTab.Dashboard.route
                                        UserRole.ORGANIZER -> AdminTab.Events.route
                                        UserRole.MEDIA_TEAM -> AdminTab.Media.route
                                        else -> AdminTab.Settings.route
                                    }
                                    DebugLog.log("MainActivity.kt:admin/login", "Navigating after login", mapOf("role" to currentUser!!.role.name, "startRoute" to startRoute), "H2")
                                    nav.navigate(startRoute) {
                                        popUpTo("admin/login") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                            Box(Modifier.fillMaxSize()) {
                                AdminLoginScreen(authViewModel = authVm)
                            }
                        }

                        composable(AdminTab.Dashboard.route) {
                            val currentUser by authVm.currentUser.collectAsState()
                            val allowedTabs = remember(currentUser?.role) { tabsForRole(currentUser?.role ?: UserRole.ADMIN) }
                            LaunchedEffect(currentUser) {
                                if (currentUser == null) {
                                    nav.navigate("admin/login") { popUpTo(AdminTab.Dashboard.route) { inclusive = true } }
                                } else if (AdminTab.Dashboard.route !in allowedTabs.map { it.route }) {
                                    val firstRoute = allowedTabs.first().route
                                    DebugLog.log("MainActivity.kt:Dashboard", "Redirecting to first allowed tab", mapOf("firstRoute" to firstRoute, "allowedSize" to allowedTabs.size), "H3")
                                    nav.navigate(firstRoute) { popUpTo(AdminTab.Dashboard.route) { inclusive = true } }
                                }
                            }
                            if (currentUser != null && AdminTab.Dashboard.route in allowedTabs.map { it.route }) {
                                val adminDashboardVm: com.campusconnectplus.feature_admin.AdminDashboardViewModel = hiltViewModel()
                                Box(Modifier.fillMaxSize()) {
                                    AdminScaffold(
                                        currentRoute = AdminTab.Dashboard.route,
                                        onNavigate = nav::navigate,
                                        allowedTabs = allowedTabs
                                    ) {
                                        AdminDashboardScreen(vm = adminDashboardVm, onViewDetails = { nav.navigate(AdminTab.Events.route) })
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        composable(AdminTab.Events.route) {
                            val currentUser by authVm.currentUser.collectAsState()
                            val allowedTabs = remember(currentUser?.role) { tabsForRole(currentUser?.role ?: UserRole.ADMIN) }
                            LaunchedEffect(currentUser) {
                                if (currentUser == null) {
                                    nav.navigate("admin/login") { popUpTo(AdminTab.Events.route) { inclusive = true } }
                                } else if (AdminTab.Events.route !in allowedTabs.map { it.route }) {
                                    nav.navigate(allowedTabs.first().route) { popUpTo(AdminTab.Events.route) { inclusive = true } }
                                }
                            }
                            if (currentUser != null && AdminTab.Events.route in allowedTabs.map { it.route }) {
                                val adminEventsVm: com.campusconnectplus.feature_admin.events.AdminEventsViewModel = hiltViewModel()
                                Box(Modifier.fillMaxSize()) {
                                    AdminScaffold(
                                        currentRoute = AdminTab.Events.route,
                                        onNavigate = nav::navigate,
                                        allowedTabs = allowedTabs
                                    ) {
                                        AdminEventsScreen(vm = adminEventsVm)
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        composable(AdminTab.Media.route) {
                            val currentUser by authVm.currentUser.collectAsState()
                            val allowedTabs = remember(currentUser?.role) { tabsForRole(currentUser?.role ?: UserRole.ADMIN) }
                            LaunchedEffect(currentUser) {
                                if (currentUser == null) {
                                    nav.navigate("admin/login") { popUpTo(AdminTab.Media.route) { inclusive = true } }
                                } else if (AdminTab.Media.route !in allowedTabs.map { it.route }) {
                                    nav.navigate(allowedTabs.first().route) { popUpTo(AdminTab.Media.route) { inclusive = true } }
                                }
                            }
                            if (currentUser != null && AdminTab.Media.route in allowedTabs.map { it.route }) {
                                val adminMediaVm: com.campusconnectplus.feature_admin.media.AdminMediaViewModel = hiltViewModel()
                                Box(Modifier.fillMaxSize()) {
                                    AdminScaffold(
                                        currentRoute = AdminTab.Media.route,
                                        onNavigate = nav::navigate,
                                        allowedTabs = allowedTabs
                                    ) {
                                        AdminMediaScreen(vm = adminMediaVm)
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        composable(AdminTab.Announcements.route) {
                            val currentUser by authVm.currentUser.collectAsState()
                            val allowedTabs = remember(currentUser?.role) { tabsForRole(currentUser?.role ?: UserRole.ADMIN) }
                            LaunchedEffect(currentUser) {
                                if (currentUser == null) {
                                    nav.navigate("admin/login") { popUpTo(AdminTab.Announcements.route) { inclusive = true } }
                                } else if (AdminTab.Announcements.route !in allowedTabs.map { it.route }) {
                                    nav.navigate(allowedTabs.first().route) { popUpTo(AdminTab.Announcements.route) { inclusive = true } }
                                }
                            }
                            if (currentUser != null && AdminTab.Announcements.route in allowedTabs.map { it.route }) {
                                val adminAnnVm: com.campusconnectplus.feature_admin.announcements.AdminAnnouncementsViewModel = hiltViewModel()
                                Box(Modifier.fillMaxSize()) {
                                    AdminScaffold(
                                        currentRoute = AdminTab.Announcements.route,
                                        onNavigate = nav::navigate,
                                        allowedTabs = allowedTabs
                                    ) {
                                        AdminAnnouncementsScreen(vm = adminAnnVm)
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }


                        composable(AdminTab.Settings.route) {
                            val currentUser by authVm.currentUser.collectAsState()
                            val allowedTabs = remember(currentUser?.role) { tabsForRole(currentUser?.role ?: UserRole.ADMIN) }
                            LaunchedEffect(currentUser) {
                                if (currentUser == null) {
                                    nav.navigate("admin/login") { popUpTo(AdminTab.Settings.route) { inclusive = true } }
                                } else if (AdminTab.Settings.route !in allowedTabs.map { it.route }) {
                                    nav.navigate(allowedTabs.first().route) { popUpTo(AdminTab.Settings.route) { inclusive = true } }
                                }
                            }
                            if (currentUser != null && AdminTab.Settings.route in allowedTabs.map { it.route }) {
                                Box(Modifier.fillMaxSize()) {
                                    AdminScaffold(
                                        currentRoute = AdminTab.Settings.route,
                                        onNavigate = nav::navigate,
                                        allowedTabs = allowedTabs
                                    ) {
                                        AdminSettingsScreen(onLogout = {
                                            DebugLog.log("MainActivity.kt:Settings", "Logout triggered", emptyMap(), "H5")
                                            nav.navigate("admin/login") {
                                                popUpTo(StudentTab.Home.route) { inclusive = true }
                                            }
                                            authVm.logout()
                                        })
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add("android.permission.READ_MEDIA_IMAGES")
            permissions.add("android.permission.READ_MEDIA_VIDEO")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissions.add("android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
            }
        } else {
            permissions.add("android.permission.READ_EXTERNAL_STORAGE")
        }

        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}
