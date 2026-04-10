package com.campusconnectplus.core.di

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.campusconnectplus.feature_admin.AdminDashboardViewModel
import com.campusconnectplus.feature_admin.auth.AuthViewModel
import com.campusconnectplus.feature_admin.announcements.AdminAnnouncementsViewModel
import com.campusconnectplus.feature_admin.events.AdminEventsViewModel
import com.campusconnectplus.feature_admin.media.AdminMediaViewModel
import com.campusconnectplus.feature_admin.users.AdminUsersViewModel
import com.campusconnectplus.feature_student.announcements.StudentAnnouncementsViewModel
import com.campusconnectplus.feature_student.events.StudentEventsViewModel
import com.campusconnectplus.feature_student.media.StudentMediaViewModel
import com.campusconnectplus.feature_student.saved.StudentSavedViewModel

/** CompositionLocal so composables can obtain [AppContainer] for ViewModel factories. */
val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("No AppContainer provided. Wrap content with CompositionLocalProvider(LocalAppContainer provides container)")
}

object ViewModelFactories {

    fun studentEventsFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        StudentEventsViewModel(container.eventRepository, container.favoriteRepository)
    }

    fun studentMediaFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        StudentMediaViewModel(container.mediaRepository, container.favoriteRepository)
    }

    fun studentSavedFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        StudentSavedViewModel(
            container.eventRepository,
            container.mediaRepository,
            container.favoriteRepository
        )
    }

    fun studentAnnouncementsFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        StudentAnnouncementsViewModel(container.announcementRepository)
    }

    fun studentHomeFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        com.campusconnectplus.feature_student.home.StudentHomeViewModel(
            container.eventRepository,
            container.mediaRepository,
            container.favoriteRepository
        )
    }

    fun adminEventsFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        AdminEventsViewModel(container.eventRepository)
    }

    fun adminMediaFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        AdminMediaViewModel(container.mediaRepository)
    }

    fun adminAnnouncementsFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        AdminAnnouncementsViewModel(container.announcementRepository)
    }

    fun adminUsersFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        AdminUsersViewModel(container.userRepository)
    }

    fun adminDashboardFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        AdminDashboardViewModel(
            container.eventRepository,
            container.mediaRepository,
            container.announcementRepository,
            container.userRepository
        )
    }

    fun authFactory(container: AppContainer): ViewModelProvider.Factory = factory {
        AuthViewModel(container.authRepository)
    }

    private inline fun <reified VM : ViewModel> factory(
        crossinline create: () -> VM
    ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
    }
}

