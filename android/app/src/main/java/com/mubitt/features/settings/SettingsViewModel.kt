package com.mubitt.features.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SettingsViewModel - Gestión de configuraciones
 * Características específicas San Juan:
 * - Configuraciones localizadas para Argentina
 * - Preferencias de privacidad según legislación local
 * - Optimizaciones para uso en San Juan
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // private val settingsRepository: SettingsRepository, // Inyectar cuando esté disponible
    // private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val notificationSettings = generateNotificationSettings()
                val privacySettings = generatePrivacySettings()
                val experienceSettings = generateExperienceSettings()
                val sanJuanSettings = generateSanJuanSettings()
                val aboutSettings = generateAboutSettings()
                
                _uiState.value = _uiState.value.copy(
                    notificationSettings = notificationSettings,
                    privacySettings = privacySettings,
                    experienceSettings = experienceSettings,
                    sanJuanSettings = sanJuanSettings,
                    aboutSettings = aboutSettings,
                    appVersion = "1.0.0"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar configuraciones: ${e.message}"
                )
            }
        }
    }
    
    fun toggleNotificationSetting(key: String) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.notificationSettings.toMutableList()
                val settingIndex = currentSettings.indexOfFirst { it.key == key }
                
                if (settingIndex != -1) {
                    currentSettings[settingIndex] = currentSettings[settingIndex].copy(
                        enabled = !currentSettings[settingIndex].enabled
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        notificationSettings = currentSettings
                    )
                    
                    // TODO: Guardar en repository
                    // settingsRepository.updateNotificationSetting(key, currentSettings[settingIndex].enabled)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al actualizar configuración: ${e.message}"
                )
            }
        }
    }
    
    fun togglePrivacySetting(key: String) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.privacySettings.toMutableList()
                val settingIndex = currentSettings.indexOfFirst { it.key == key }
                
                if (settingIndex != -1) {
                    currentSettings[settingIndex] = currentSettings[settingIndex].copy(
                        enabled = !currentSettings[settingIndex].enabled
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        privacySettings = currentSettings
                    )
                    
                    // TODO: Guardar en repository
                    // settingsRepository.updatePrivacySetting(key, currentSettings[settingIndex].enabled)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al actualizar configuración: ${e.message}"
                )
            }
        }
    }
    
    fun toggleExperienceSetting(key: String) {
        viewModelScope.launch {
            try {
                val currentSettings = _uiState.value.experienceSettings.toMutableList()
                val settingIndex = currentSettings.indexOfFirst { it.key == key }
                
                if (settingIndex != -1) {
                    currentSettings[settingIndex] = currentSettings[settingIndex].copy(
                        enabled = !currentSettings[settingIndex].enabled
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        experienceSettings = currentSettings
                    )
                    
                    // TODO: Guardar en repository
                    // settingsRepository.updateExperienceSetting(key, currentSettings[settingIndex].enabled)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al actualizar configuración: ${e.message}"
                )
            }
        }
    }
    
    fun navigateToSetting(key: String) {
        viewModelScope.launch {
            when (key) {
                "logout" -> showLogoutDialog()
                "clear_data" -> showClearDataDialog()
                "privacy_policy" -> {
                    // TODO: Abrir navegador con política de privacidad
                }
                "terms_conditions" -> {
                    // TODO: Abrir navegador con términos y condiciones
                }
                "contact_support" -> {
                    // TODO: Navegar a SupportScreen
                }
                "language" -> {
                    // TODO: Mostrar selector de idioma
                }
                "theme" -> {
                    // TODO: Mostrar selector de tema
                }
                "san_juan_zones" -> {
                    // TODO: Mostrar configurador de zonas de San Juan
                }
                "local_references" -> {
                    // TODO: Mostrar configurador de referencias locales
                }
                "student_discount" -> {
                    // TODO: Mostrar verificación de descuento estudiantil
                }
                else -> {
                    // Setting no reconocido
                }
            }
        }
    }
    
    private fun showLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = true)
    }
    
    fun dismissLogoutDialog() {
        _uiState.value = _uiState.value.copy(showLogoutDialog = false)
    }
    
    fun confirmLogout() {
        viewModelScope.launch {
            try {
                // TODO: Implementar logout
                // authRepository.logout()
                
                _uiState.value = _uiState.value.copy(
                    showLogoutDialog = false,
                    successMessage = "Sesión cerrada exitosamente"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showLogoutDialog = false,
                    errorMessage = "Error al cerrar sesión: ${e.message}"
                )
            }
        }
    }
    
    private fun showClearDataDialog() {
        _uiState.value = _uiState.value.copy(showClearDataDialog = true)
    }
    
    fun dismissClearDataDialog() {
        _uiState.value = _uiState.value.copy(showClearDataDialog = false)
    }
    
    fun confirmClearData() {
        viewModelScope.launch {
            try {
                // TODO: Limpiar todos los datos locales
                // settingsRepository.clearAllData()
                
                _uiState.value = _uiState.value.copy(
                    showClearDataDialog = false,
                    successMessage = "Datos eliminados exitosamente"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showClearDataDialog = false,
                    errorMessage = "Error al limpiar datos: ${e.message}"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    private fun generateNotificationSettings(): List<SettingItem> {
        return listOf(
            SettingItem(
                key = "trip_notifications",
                title = "Notificaciones de viajes",
                subtitle = "Conductor asignado, llegadas, completado",
                icon = Icons.Default.DirectionsCar,
                iconBackgroundColor = Color(0xFF4CAF50),
                type = SettingType.SWITCH,
                enabled = true
            ),
            SettingItem(
                key = "promotional_notifications",
                title = "Promociones y descuentos",
                subtitle = "Ofertas especiales y descuentos en San Juan",
                icon = Icons.Default.LocalOffer,
                iconBackgroundColor = Color(0xFFFF9800),
                type = SettingType.SWITCH,
                enabled = true
            ),
            SettingItem(
                key = "safety_notifications",
                title = "Alertas de seguridad",
                subtitle = "Emergencias y situaciones importantes",
                icon = Icons.Default.Security,
                iconBackgroundColor = Color(0xFFF44336),
                type = SettingType.SWITCH,
                enabled = true
            ),
            SettingItem(
                key = "sound_notifications",
                title = "Sonidos",
                subtitle = "Sonidos para notificaciones push",
                icon = Icons.Default.VolumeUp,
                iconBackgroundColor = Color(0xFF2196F3),
                type = SettingType.SWITCH,
                enabled = false
            )
        )
    }
    
    private fun generatePrivacySettings(): List<SettingItem> {
        return listOf(
            SettingItem(
                key = "location_sharing",
                title = "Compartir ubicación",
                subtitle = "Permitir que contactos vean tu viaje en tiempo real",
                icon = Icons.Default.LocationOn,
                iconBackgroundColor = Color(0xFF4CAF50),
                type = SettingType.SWITCH,
                enabled = true
            ),
            SettingItem(
                key = "trip_history_save",
                title = "Guardar historial",
                subtitle = "Mantener registro de viajes anteriores",
                icon = Icons.Default.History,
                iconBackgroundColor = Color(0xFF9C27B0),
                type = SettingType.SWITCH,
                enabled = true
            ),
            SettingItem(
                key = "privacy_policy",
                title = "Política de privacidad",
                subtitle = "Ver términos de privacidad y protección de datos",
                icon = Icons.Default.PrivacyTip,
                iconBackgroundColor = Color(0xFF607D8B),
                type = SettingType.NAVIGATION
            ),
            SettingItem(
                key = "clear_data",
                title = "Limpiar datos",
                subtitle = "Eliminar todos los datos guardados localmente",
                icon = Icons.Default.Delete,
                iconBackgroundColor = Color(0xFFF44336),
                type = SettingType.NAVIGATION
            )
        )
    }
    
    private fun generateExperienceSettings(): List<SettingItem> {
        return listOf(
            SettingItem(
                key = "dark_theme",
                title = "Tema oscuro",
                subtitle = "Interfaz oscura para mejor visibilidad nocturna",
                icon = Icons.Default.DarkMode,
                iconBackgroundColor = Color(0xFF424242),
                type = SettingType.SWITCH,
                enabled = false
            ),
            SettingItem(
                key = "language",
                title = "Idioma",
                subtitle = "Configurar idioma de la aplicación",
                value = "Español (Argentina)",
                icon = Icons.Default.Language,
                iconBackgroundColor = Color(0xFF00BCD4),
                type = SettingType.NAVIGATION
            ),
            SettingItem(
                key = "map_style",
                title = "Estilo de mapa",
                subtitle = "Personalizar apariencia del mapa",
                value = "San Juan Night",
                icon = Icons.Default.Map,
                iconBackgroundColor = Color(0xFF4CAF50),
                type = SettingType.NAVIGATION
            ),
            SettingItem(
                key = "auto_complete_addresses",
                title = "Autocompletar direcciones",
                subtitle = "Sugerir direcciones basadas en historial",
                icon = Icons.Default.EditLocation,
                iconBackgroundColor = Color(0xFF2196F3),
                type = SettingType.SWITCH,
                enabled = true
            )
        )
    }
    
    private fun generateSanJuanSettings(): List<SettingItem> {
        return listOf(
            SettingItem(
                key = "san_juan_zones",
                title = "Zonas preferenciales",
                subtitle = "Configurar zonas frecuentes en San Juan",
                icon = Icons.Default.LocationCity,
                iconBackgroundColor = Color(0xFF8BC34A),
                type = SettingType.NAVIGATION
            ),
            SettingItem(
                key = "local_references",
                title = "Referencias locales",
                subtitle = "Personalizar referencias conocidas",
                icon = Icons.Default.Signpost,
                iconBackgroundColor = Color(0xFFFF5722),
                type = SettingType.NAVIGATION
            ),
            SettingItem(
                key = "student_discount",
                title = "Descuento estudiantil UNSJ",
                subtitle = "Verificar cuenta de estudiante universitario",
                icon = Icons.Default.School,
                iconBackgroundColor = Color(0xFF3F51B5),
                type = SettingType.NAVIGATION
            ),
            SettingItem(
                key = "fiesta_sol_mode",
                title = "Modo Fiesta del Sol",
                subtitle = "Configuraciones especiales para eventos masivos",
                icon = Icons.Default.Festival,
                iconBackgroundColor = Color(0xFFFFEB3B),
                type = SettingType.SWITCH,
                enabled = false
            )
        )
    }
    
    private fun generateAboutSettings(): List<SettingItem> {
        return listOf(
            SettingItem(
                key = "terms_conditions",
                title = "Términos y condiciones",
                subtitle = "Leer términos de uso del servicio",
                icon = Icons.Default.Assignment,
                iconBackgroundColor = Color(0xFF795548),
                type = SettingType.NAVIGATION
            ),
            SettingItem(
                key = "contact_support",
                title = "Contactar soporte",
                subtitle = "Ayuda y atención al cliente",
                icon = Icons.Default.SupportAgent,
                iconBackgroundColor = Color(0xFF009688),
                type = SettingType.NAVIGATION
            ),
            SettingItem(
                key = "rate_app",
                title = "Calificar app",
                subtitle = "Déjanos tu opinión en Google Play",
                icon = Icons.Default.Star,
                iconBackgroundColor = Color(0xFFFF9800),
                type = SettingType.NAVIGATION
            ),
            SettingItem(
                key = "logout",
                title = "Cerrar sesión",
                subtitle = "Salir de tu cuenta de Mubitt",
                icon = Icons.Default.Logout,
                iconBackgroundColor = Color(0xFFF44336),
                type = SettingType.NAVIGATION
            )
        )
    }
}

data class SettingsUiState(
    val notificationSettings: List<SettingItem> = emptyList(),
    val privacySettings: List<SettingItem> = emptyList(),
    val experienceSettings: List<SettingItem> = emptyList(),
    val sanJuanSettings: List<SettingItem> = emptyList(),
    val aboutSettings: List<SettingItem> = emptyList(),
    val appVersion: String = "",
    val showLogoutDialog: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)