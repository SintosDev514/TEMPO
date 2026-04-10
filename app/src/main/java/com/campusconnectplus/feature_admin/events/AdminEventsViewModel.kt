package com.campusconnectplus.feature_admin.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.Event
import com.campusconnectplus.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminEventsViewModel(
    private val repo: EventRepository
) : ViewModel() {

    val events: StateFlow<List<Event>> =
        repo.observeEvents().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    fun delete(eventId: String) {
        viewModelScope.launch {
            repo.delete(eventId)
            _snackbarMessage.value = "Event deleted"
        }
    }

    fun upsert(event: Event) {
        viewModelScope.launch {
            repo.upsert(event)
            _snackbarMessage.value = if (event.id == 0L) "Event created" else "Event updated"
        }
    }
}
