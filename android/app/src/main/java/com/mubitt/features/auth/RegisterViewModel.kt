package com.mubitt.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubitt.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para RegisterScreen
 * Maneja la validación y registro de nuevos usuarios
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = validateName(name)
        )
        updateCanRegister()
    }
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = validateEmail(email)
        )
        updateCanRegister()
    }
    
    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(
            phone = phone,
            phoneError = validatePhone(phone)
        )
        updateCanRegister()
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = validatePassword(password)
        )
        updateCanRegister()
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = validateConfirmPassword(confirmPassword, _uiState.value.password)
        )
        updateCanRegister()
    }
    
    fun updateTermsAccepted(accepted: Boolean) {
        _uiState.value = _uiState.value.copy(
            termsAccepted = accepted,
            termsError = if (accepted) null else "Debes aceptar los términos y condiciones"
        )
        updateCanRegister()
    }
    
    fun register() {
        if (!_uiState.value.canRegister) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                generalError = null
            )
            
            try {
                // TODO: Implementar registro real con backend
                val success = userRepository.registerUser(
                    name = _uiState.value.name,
                    email = _uiState.value.email,
                    phone = _uiState.value.phone,
                    password = _uiState.value.password
                )
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistrationSuccessful = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        generalError = "Error al crear la cuenta. Inténtalo de nuevo."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = "Error de conexión: ${e.message}"
                )
            }
        }
    }
    
    private fun updateCanRegister() {
        val state = _uiState.value
        val canRegister = state.name.isNotBlank() &&
                         state.email.isNotBlank() &&
                         state.phone.isNotBlank() &&
                         state.password.isNotBlank() &&
                         state.confirmPassword.isNotBlank() &&
                         state.termsAccepted &&
                         state.nameError == null &&
                         state.emailError == null &&
                         state.phoneError == null &&
                         state.passwordError == null &&
                         state.confirmPasswordError == null &&
                         state.termsError == null
        
        _uiState.value = _uiState.value.copy(canRegister = canRegister)
    }
    
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> null // No mostrar error si está vacío
            name.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            name.length > 50 -> "El nombre es demasiado largo"
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) -> "El nombre solo puede contener letras y espacios"
            else -> null
        }
    }
    
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> null
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                "Ingresa un email válido"
            else -> null
        }
    }
    
    private fun validatePhone(phone: String): String? {
        // Validación para números argentinos de San Juan
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return when {
            phone.isBlank() -> null
            cleanPhone.length < 10 -> "Número de teléfono muy corto"
            cleanPhone.length > 13 -> "Número de teléfono muy largo"
            !cleanPhone.startsWith("2644") && !cleanPhone.startsWith("542644") -> 
                "Ingresa un número de San Juan (ej: 2644123456)"
            else -> null
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> null
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            password.length > 128 -> "La contraseña es demasiado larga"
            !password.matches(Regex(".*[0-9].*")) -> "La contraseña debe contener al menos un número"
            else -> null
        }
    }
    
    private fun validateConfirmPassword(confirmPassword: String, password: String): String? {
        return when {
            confirmPassword.isBlank() -> null
            confirmPassword != password -> "Las contraseñas no coinciden"
            else -> null
        }
    }
}

/**
 * Estado de UI para RegisterScreen
 */
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsAccepted: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val termsError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isRegistrationSuccessful: Boolean = false,
    val canRegister: Boolean = false
)