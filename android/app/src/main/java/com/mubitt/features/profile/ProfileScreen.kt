package com.mubitt.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mubitt.core.domain.model.User

/**
 * ProfileScreen - Gestión de perfil de usuario
 * Pantalla de configuración personal optimizada para usuarios de San Juan
 * Incluye configuración de seguridad, métodos de pago y preferencias
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToTripHistory: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar perfil al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header con información del usuario
        ProfileHeader(
            user = uiState.user,
            onNavigateBack = onNavigateBack,
            onEditProfile = { viewModel.toggleEditMode() }
        )
        
        // Contenido principal
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estadísticas del usuario
            uiState.user?.let { user ->
                UserStatsCard(
                    tripsCount = uiState.userStats.totalTrips,
                    rating = user.rating,
                    memberSince = uiState.userStats.memberSince
                )
            }
            
            // Gestión de cuenta
            ProfileSection(
                title = "Mi cuenta",
                items = listOf(
                    ProfileMenuItem(
                        icon = Icons.Default.History,
                        title = "Historial de viajes",
                        subtitle = "Ver todos mis viajes",
                        onClick = onNavigateToTripHistory
                    ),
                    ProfileMenuItem(
                        icon = Icons.Default.Payment,
                        title = "Métodos de pago",
                        subtitle = "Tarjetas, efectivo, MercadoPago",
                        onClick = onNavigateToPaymentMethods
                    ),
                    ProfileMenuItem(
                        icon = Icons.Default.Favorite,
                        title = "Lugares favoritos",
                        subtitle = "Casa, trabajo, otros",
                        onClick = { /* TODO: Implementar */ }
                    )
                )
            )
            
            // Seguridad y privacidad
            ProfileSection(
                title = "Seguridad",
                items = listOf(
                    ProfileMenuItem(
                        icon = Icons.Default.Security,
                        title = "Seguridad",
                        subtitle = "Contraseña, verificación",
                        onClick = { /* TODO: Implementar */ }
                    ),
                    ProfileMenuItem(
                        icon = Icons.Default.People,
                        title = "Contactos de emergencia",
                        subtitle = "Personas de confianza",
                        onClick = { /* TODO: Implementar */ }
                    ),
                    ProfileMenuItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacidad",
                        subtitle = "Control de datos personales",
                        onClick = { /* TODO: Implementar */ }
                    )
                )
            )
            
            // Experiencia San Juan
            ProfileSection(
                title = "Experiencia San Juan",
                items = listOf(
                    ProfileMenuItem(
                        icon = Icons.Default.Star,
                        title = "Programa de fidelidad",
                        subtitle = "Puntos y beneficios",
                        onClick = { /* TODO: Implementar */ }
                    ),
                    ProfileMenuItem(
                        icon = Icons.Default.School,
                        title = "Descuento estudiante UNSJ",
                        subtitle = "Verificar condición estudiantil",
                        onClick = { /* TODO: Implementar */ }
                    ),
                    ProfileMenuItem(
                        icon = Icons.Default.Share,
                        title = "Invitar amigos",
                        subtitle = "Comparte Mubitt y gana créditos",
                        onClick = { /* TODO: Implementar */ }
                    )
                )
            )
            
            // Soporte y configuración
            ProfileSection(
                title = "Ayuda y configuración",
                items = listOf(
                    ProfileMenuItem(
                        icon = Icons.Default.Help,
                        title = "Centro de ayuda",
                        subtitle = "Preguntas frecuentes y soporte",
                        onClick = onNavigateToSupport
                    ),
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Configuración",
                        subtitle = "Notificaciones, idioma, más",
                        onClick = onNavigateToSettings
                    ),
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "Acerca de Mubitt",
                        subtitle = "Versión, términos, política",
                        onClick = { /* TODO: Implementar */ }
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón cerrar sesión
            LogoutButton(
                onLogout = {
                    viewModel.logout()
                    onLogout()
                },
                isLoading = uiState.isLoggingOut
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Información de la app para San Juan
            SanJuanAppInfo()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileHeader(
    user: User?,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                TextButton(onClick = onEditProfile) {
                    Text(
                        text = "Editar",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información del usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del usuario
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.profileImageUrl != null) {
                        // TODO: Cargar imagen real con Coil
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name ?: "Usuario",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = user?.email ?: "email@ejemplo.com",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = user?.phoneNumber ?: "+54 264 123-4567",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserStatsCard(
    tripsCount: Int,
    rating: Double,
    memberSince: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            UserStatItem(
                icon = Icons.Default.DirectionsCar,
                value = tripsCount.toString(),
                label = "Viajes"
            )
            
            UserStatItem(
                icon = Icons.Default.Star,
                value = String.format("%.1f", rating),
                label = "Rating"
            )
            
            UserStatItem(
                icon = Icons.Default.CalendarToday,
                value = memberSince,
                label = "Miembro desde"
            )
        }
    }
}

@Composable
private fun UserStatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ProfileSection(
    title: String,
    items: List<ProfileMenuItem>
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    ProfileMenuItemRow(
                        item = item,
                        showDivider = index < items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItemRow(
    item: ProfileMenuItem,
    showDivider: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
    
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 60.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun LogoutButton(
    onLogout: () -> Unit,
    isLoading: Boolean
) {
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isLoading,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cerrar sesión",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SanJuanAppInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mubitt San Juan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Versión 1.0.0 • Hecho con ❤️ para los sanjuaninos",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "• Conductores locales",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "• Tarifas justas",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "• Soporte 24/7",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Data class para elementos del menú de perfil
 */
data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)