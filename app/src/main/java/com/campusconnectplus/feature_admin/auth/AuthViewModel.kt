package com.campusconnectplus.feature_admin.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnectplus.data.repository.AuthResult
import com.campusconnectplus.data.repository.AuthRepository
import com.campusconnectplus.data.repository.User
import com.campusconnectplus.data.repository.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess.asStateFlow()

    fun clearSignUpSuccess() {
        _signUpSuccess.value = false
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    _currentUser.value = result.user
                    _errorMessage.value = null
                }
                is AuthResult.Error -> _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun signUp(role: UserRole, name: String, email: String, password: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            _signUpSuccess.value = false
            _isLoading.value = true
            when (val result = authRepository.signUp(role, name, email, password)) {
                is AuthResult.Success -> {
                    _currentUser.value = null
                    _errorMessage.value = null
                    _signUpSuccess.value = true
                }
                is AuthResult.Error -> _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun logout() {
        _currentUser.value = null
        _errorMessage.value = null
    }
}
