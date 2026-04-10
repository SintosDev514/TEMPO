package com.campusconnectplus.feature_admin.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.User
import com.campusconnectplus.data.repository.UserRepository
import com.campusconnectplus.data.repository.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminUsersViewModel(
    private val repo: UserRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val users: StateFlow<List<User>> =
        combine(
            repo.observeUsers(),
            searchQuery
        ) { list, query ->
            if (query.isBlank()) list
            else list.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true) ||
                    it.role.name.contains(query, ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun totalCount(): Int = users.value.size

    fun activeCount(): Int = users.value.count { it.active }

    fun adminCount(): Int = users.value.count { it.role == UserRole.ADMIN }

    fun studentCount(): Int = users.value.count { it.role == UserRole.STUDENT }

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun setRole(userId: Long, roleLabel: String) {
        val role = when (roleLabel) {
            "Admin" -> UserRole.ADMIN
            "Media Team" -> UserRole.MEDIA_TEAM
            "Student Org" -> UserRole.ORGANIZER
            else -> UserRole.STUDENT
        }
        viewModelScope.launch { repo.setRole(userId, role) }
    }

    fun toggleActive(userId: Long) {
        viewModelScope.launch {
            val user = users.value.find { it.id == userId }
            user?.let { repo.setActive(userId, !it.active) }
        }
    }

    fun upsert(user: User) {
        viewModelScope.launch { repo.upsert(user) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }
}
