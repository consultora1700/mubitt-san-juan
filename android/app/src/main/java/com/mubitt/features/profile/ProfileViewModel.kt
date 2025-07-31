package com.mubitt.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubitt.core.domain.model.User
import com.mubitt.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para ProfileScreen
 * Maneja la información del usuario y las operaciones del perfil
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // TODO: Obtener usuario real del repository
                val user = getCurrentUser()
                val stats = getUserStats(user?.id)
                
                _uiState.value = _uiState.value.copy(
                    user = user,
                    userStats = stats,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar el perfil: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode
        )
    }
    
    fun updateUserName(name: String) {
        _uiState.value.user?.let { user ->
            _uiState.value = _uiState.value.copy(
                user = user.copy(name = name)
            )
        }
    }
    
    fun updateUserEmail(email: String) {
        _uiState.value.user?.let { user ->
            _uiState.value = _uiState.value.copy(
                user = user.copy(email = email)
            )
        }
    }
    
    fun updateUserPhone(phone: String) {
        _uiState.value.user?.let { user ->
            _uiState.value = _uiState.value.copy(
                user = user.copy(phoneNumber = phone)
            )
        }
    }
    
    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            
            try {
                val user = _uiState.value.user
                if (user != null) {
                    val success = userRepository.updateUserProfile(user)
                    
                    if (success) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isEditMode = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = "Error al guardar los cambios",
                            isSaving = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error de conexión: ${e.message}",
                    isSaving = false
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true)
            
            try {
                userRepository.logout()
                _uiState.value = _uiState.value.copy(
                    isLoggingOut = false,
                    isLoggedOut = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cerrar sesión: ${e.message}",
                    isLoggingOut = false
                )
            }
        }
    }
    
    private suspend fun getCurrentUser(): User {
        // TODO: Obtener usuario real del repository
        return createMockUser()
    }
    
    private suspend fun getUserStats(userId: String?): UserStats {
        // TODO: Obtener estadísticas reales del repository
        return UserStats(
            totalTrips = (50..200).random(),
            memberSince = "2024"
        )
    }
    
    private fun createMockUser(): User {
        return User(
            id = "user_001",
            name = "Juan Pérez",
            email = "juan.perez@email.com",
            phoneNumber = "+54 264 123-4567",
            profileImageUrl = null,
            rating = 4.7,
            isVerified = true,
            createdAt = System.currentTimeMillis()
        )
    }
}

/**
 * Estado de UI para ProfileScreen
 */
data class ProfileUiState(
    val user: User? = null,
    val userStats: UserStats = UserStats(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoggedOut: Boolean = false,
    val error: String? = null
)

/**
 * Estadísticas del usuario
 */
data class UserStats(
    val totalTrips: Int = 0,
    val memberSince: String = "2024"
)