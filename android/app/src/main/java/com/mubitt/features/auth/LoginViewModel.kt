package com.mubitt.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.data.remote.interceptor.AuthInterceptor
import com.mubitt.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para manejo de autenticación
 * Integra con UserRepository y maneja estados de UI
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authInterceptor: AuthInterceptor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    /**
     * Login con email
     */
    fun loginWithEmail(email: String, password: String) {
        if (!validateEmail(email)) {
            _uiState.value = _uiState.value.copy(
                emailError = "Email inválido"
            )
            return
        }
        
        if (!validatePassword(password)) {
            _uiState.value = _uiState.value.copy(
                passwordError = "La contraseña debe tener al menos 6 caracteres"
            )
            return
        }
        
        performLogin {
            userRepository.login(email, password)
        }
    }
    
    /**
     * Login con teléfono
     */
    fun loginWithPhone(phoneNumber: String, password: String) {
        if (!validatePhoneNumber(phoneNumber)) {
            _uiState.value = _uiState.value.copy(
                phoneError = "Número de teléfono inválido"
            )
            return
        }
        
        if (!validatePassword(password)) {
            _uiState.value = _uiState.value.copy(
                passwordError = "La contraseña debe tener al menos 6 caracteres"
            )
            return
        }
        
        performLogin {
            userRepository.loginWithPhone(phoneNumber, password)
        }
    }
    
    /**
     * Lógica común de login
     */
    private fun performLogin(loginCall: suspend () -> ApiResult<com.mubitt.core.domain.model.User>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                emailError = null,
                phoneError = null,
                passwordError = null
            )
            
            when (val result = loginCall()) {
                is ApiResult.Success -> {
                    // TODO: Guardar token de autenticación
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        currentUser = result.data
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    /**
     * Limpiar errores cuando el usuario empiece a escribir
     */
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            emailError = null,
            phoneError = null,
            passwordError = null
        )
    }
    
    // Métodos de validación
    
    private fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && 
               android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        // Validación básica para números argentinos
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        return cleanNumber.isNotBlank() && 
               (cleanNumber.startsWith("+54") || cleanNumber.length >= 10)
    }
    
    private fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }
}

/**
 * Estado de UI para LoginScreen
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val currentUser: com.mubitt.core.domain.model.User? = null,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null
)