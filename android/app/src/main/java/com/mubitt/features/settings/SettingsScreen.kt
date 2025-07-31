package com.mubitt.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mubitt.shared.theme.Spacing

/**
 * SettingsScreen - Configuraciones de la app
 * Características específicas San Juan:
 * - Preferencias locales (idioma, zona horaria)
 * - Configuraciones de notificaciones optimizadas
 * - Privacidad según regulaciones argentinas
 * - Temas y personalizaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Configuraciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // Sección Notificaciones
            item {
                SettingsSectionHeader(title = "Notificaciones")
            }
            
            items(uiState.notificationSettings) { setting ->
                SettingsSwitchItem(
                    setting = setting,
                    onToggle = { viewModel.toggleNotificationSetting(setting.key) }
                )
            }
            
            // Sección Privacidad
            item {
                SettingsSectionHeader(title = "Privacidad y seguridad")
            }
            
            items(uiState.privacySettings) { setting ->
                when (setting.type) {
                    SettingType.SWITCH -> {
                        SettingsSwitchItem(
                            setting = setting,
                            onToggle = { viewModel.togglePrivacySetting(setting.key) }
                        )
                    }
                    SettingType.NAVIGATION -> {
                        SettingsNavigationItem(
                            setting = setting,
                            onClick = { viewModel.navigateToSetting(setting.key) }
                        )
                    }
                }
            }
            
            // Sección Experiencia
            item {
                SettingsSectionHeader(title = "Experiencia de uso")
            }
            
            items(uiState.experienceSettings) { setting ->
                when (setting.type) {
                    SettingType.SWITCH -> {
                        SettingsSwitchItem(
                            setting = setting,
                            onToggle = { viewModel.toggleExperienceSetting(setting.key) }
                        )
                    }
                    SettingType.NAVIGATION -> {
                        SettingsNavigationItem(
                            setting = setting,
                            onClick = { viewModel.navigateToSetting(setting.key) }
                        )
                    }
                }
            }
            
            // Sección Específica San Juan
            item {
                SettingsSectionHeader(title = "Configuración San Juan")
            }
            
            items(uiState.sanJuanSettings) { setting ->
                SettingsNavigationItem(
                    setting = setting,
                    onClick = { viewModel.navigateToSetting(setting.key) }
                )
            }
            
            // Sección Acerca de
            item {
                SettingsSectionHeader(title = "Acerca de Mubitt")
            }
            
            items(uiState.aboutSettings) { setting ->
                SettingsNavigationItem(
                    setting = setting,
                    onClick = { viewModel.navigateToSetting(setting.key) }
                )
            }
            
            // Información de la app
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Mubitt San Juan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Versión ${uiState.appVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(Spacing.small))
                        
                        Text(
                            text = "Hecho con ❤️ en San Juan, Argentina",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }
    
    // Dialogs
    if (uiState.showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = { viewModel.confirmLogout() },
            onDismiss = { viewModel.dismissLogoutDialog() }
        )
    }
    
    if (uiState.showClearDataDialog) {
        ClearDataConfirmationDialog(
            onConfirm = { viewModel.confirmClearData() },
            onDismiss = { viewModel.dismissClearDataDialog() }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = Spacing.small)
    )
}

@Composable
private fun SettingsSwitchItem(
    setting: SettingItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(setting.iconBackgroundColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = setting.icon,
                    contentDescription = null,
                    tint = setting.iconBackgroundColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.medium))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = setting.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (setting.subtitle.isNotEmpty()) {
                    Text(
                        text = setting.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Switch(
                checked = setting.enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    setting: SettingItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(setting.iconBackgroundColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = setting.icon,
                    contentDescription = null,
                    tint = setting.iconBackgroundColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.medium))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = setting.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (setting.subtitle.isNotEmpty()) {
                    Text(
                        text = setting.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            if (setting.value.isNotEmpty()) {
                Text(
                    text = setting.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.width(Spacing.small))
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Cerrar sesión")
        },
        text = {
            Text(text = "¿Estás seguro que querés cerrar sesión? Tendrás que volver a iniciar sesión para usar Mubitt.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ClearDataConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Limpiar datos")
        },
        text = {
            Text(text = "Esta acción eliminará todos tus datos guardados (historial, favoritos, configuraciones). No se puede deshacer. ¿Continuar?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Limpiar datos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Data classes
data class SettingItem(
    val key: String,
    val title: String,
    val subtitle: String = "",
    val value: String = "",
    val icon: ImageVector,
    val iconBackgroundColor: Color,
    val type: SettingType,
    val enabled: Boolean = false
)

enum class SettingType {
    SWITCH,
    NAVIGATION
}