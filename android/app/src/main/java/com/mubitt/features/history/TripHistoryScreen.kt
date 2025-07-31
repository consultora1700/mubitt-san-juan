package com.mubitt.features.history

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mubitt.shared.theme.Spacing
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * TripHistoryScreen - Historial de viajes
 * Características específicas San Juan:
 * - Filtros por fechas y zonas de San Juan
 * - Referencias locales en direcciones
 * - Estadísticas de uso personalizado
 * - Re-solicitar viajes frecuentes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    navController: NavController,
    viewModel: TripHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar con filtros
        TopAppBar(
            title = {
                Text(
                    text = "Mis viajes",
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
            actions = {
                IconButton(onClick = { showFilters = !showFilters }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // Panel de filtros expandible
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            TripFiltersPanel(
                currentFilter = uiState.currentFilter,
                onFilterChanged = { viewModel.updateFilter(it) }
            )
        }
        
        // Estadísticas del usuario
        if (uiState.userStats != null) {
            TripStatsCard(
                stats = uiState.userStats,
                modifier = Modifier.padding(Spacing.medium)
            )
        }
        
        // Lista de viajes
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            if (uiState.isLoading) {
                items(5) {
                    TripHistoryCardSkeleton()
                }
            } else {
                items(uiState.filteredTrips) { trip ->
                    TripHistoryCard(
                        trip = trip,
                        onClick = { viewModel.selectTrip(trip) },
                        onRepeatTrip = { viewModel.repeatTrip(trip) }
                    )
                }
                
                if (uiState.filteredTrips.isEmpty() && !uiState.isLoading) {
                    item {
                        EmptyTripHistoryState(
                            message = if (uiState.currentFilter == TripFilter.ALL) {
                                "Aún no has realizado viajes con Mubitt"
                            } else {
                                "No hay viajes que coincidan con los filtros"
                            }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }
    
    // Loading state
    if (uiState.isLoading && uiState.trips.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
    // Error handling
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
        }
    }
}

@Composable
private fun TripFiltersPanel(
    currentFilter: TripFilter,
    onFilterChanged: (TripFilter) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Text(
                text = "Filtrar viajes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            LazyColumn(
                modifier = Modifier.height(160.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                items(TripFilter.values()) { filter ->
                    FilterChip(
                        onClick = { onFilterChanged(filter) },
                        label = {
                            Text(
                                text = when (filter) {
                                    TripFilter.ALL -> "Todos los viajes"
                                    TripFilter.THIS_WEEK -> "Esta semana"
                                    TripFilter.THIS_MONTH -> "Este mes"
                                    TripFilter.COMPLETED -> "Completados"
                                    TripFilter.CANCELLED -> "Cancelados"
                                    TripFilter.FAVORITES -> "Rutas frecuentes"
                                }
                            )
                        },
                        selected = currentFilter == filter,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun TripStatsCard(
    stats: UserTripStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.DirectionsCar,
                value = stats.totalTrips.toString(),
                label = "Viajes"
            )
            
            StatItem(
                icon = Icons.Default.AttachMoney,
                value = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                    .format(stats.totalSpent),
                label = "Gastado"
            )
            
            StatItem(
                icon = Icons.Default.Star,
                value = String.format("%.1f", stats.averageRating),
                label = "Rating"
            )
            
            StatItem(
                icon = Icons.Default.LocationOn,
                value = "${stats.totalDistance}km",
                label = "Distancia"
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TripHistoryCard(
    trip: TripHistory,
    onClick: () -> Unit,
    onRepeatTrip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            // Header con fecha y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
                        .format(trip.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                TripStatusChip(status = trip.status)
            }
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            // Ruta
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(32.dp)
                            .background(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                    
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(Spacing.medium))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = trip.pickupAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = trip.dropoffAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            // Footer con precio y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
                            .format(trip.fare),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (trip.paymentMethod.isNotEmpty()) {
                        Text(
                            text = trip.paymentMethod,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                if (trip.status == TripStatus.COMPLETED) {
                    OutlinedButton(
                        onClick = onRepeatTrip,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Repetir",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripStatusChip(status: TripStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TripStatus.COMPLETED -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.1f),
            Color(0xFF4CAF50),
            "Completado"
        )
        TripStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.error,
            "Cancelado"
        )
        TripStatus.IN_PROGRESS -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.1f),
            Color(0xFFFF9800),
            "En progreso"
        )
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TripHistoryCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            // Skeleton content
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .height(16.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
            )
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            repeat(2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                )
                Spacer(modifier = Modifier.height(Spacing.small))
            }
        }
    }
}

@Composable
private fun EmptyTripHistoryState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// Data classes
data class TripHistory(
    val id: String,
    val date: Date,
    val pickupAddress: String,
    val dropoffAddress: String,
    val fare: Double,
    val paymentMethod: String,
    val status: TripStatus,
    val driverName: String = "",
    val driverRating: Float = 0f,
    val duration: Int = 0, // minutos
    val distance: Double = 0.0 // kilómetros
)

data class UserTripStats(
    val totalTrips: Int,
    val totalSpent: Double,
    val averageRating: Float,
    val totalDistance: Int
)

enum class TripStatus {
    COMPLETED,
    CANCELLED,
    IN_PROGRESS
}

enum class TripFilter {
    ALL,
    THIS_WEEK,
    THIS_MONTH,
    COMPLETED,
    CANCELLED,
    FAVORITES
}