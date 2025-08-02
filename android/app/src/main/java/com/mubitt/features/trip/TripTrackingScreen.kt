package com.mubitt.features.trip

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mubitt.core.domain.model.Driver
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.Trip
import com.mubitt.core.domain.model.TripLocation
import com.mubitt.core.domain.model.TripStatus
import com.mubitt.core.ui.components.maps.MubittMapView

/**
 * TripTrackingScreen - Seguimiento en tiempo real del viaje
 * Pantalla crítica para la experiencia de usuario durante el viaje
 * Incluye información del conductor, ruta, y controles de emergencia
 */
@Composable
fun TripTrackingScreen(
    tripId: String,
    onCallDriver: (String) -> Unit,
    onMessageDriver: () -> Unit,
    onEmergency: () -> Unit,
    onShareTrip: () -> Unit,
    onTripCompleted: (Trip) -> Unit,
    viewModel: TripTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar trip al iniciar
    LaunchedEffect(tripId) {
        viewModel.loadTrip(tripId)
        viewModel.startTrackingTrip()
    }
    
    // Manejar completion del viaje
    LaunchedEffect(uiState.trip?.status) {
        if (uiState.trip?.status == TripStatus.COMPLETED) {
            uiState.trip?.let { onTripCompleted(it) }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa con ruta en tiempo real
        uiState.trip?.let { trip ->
            MubittMapView(
                modifier = Modifier.fillMaxSize(),
                initialLocation = trip.pickupLocation.toLocation(),
                pickupLocation = trip.pickupLocation.toLocation(),
                dropoffLocation = trip.dropoffLocation.toLocation(),
                driverLocation = uiState.driverLocation,
                onLocationSelected = { /* No permitir cambios durante viaje */ }
            )
        }
        
        // Overlay con información del viaje
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Estado del viaje
            TripStatusCard(
                trip = uiState.trip,
                estimatedArrival = uiState.estimatedArrival,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Información del conductor
            uiState.driver?.let { driver ->
                DriverInfoCard(
                    driver = driver,
                    onCallDriver = { onCallDriver(driver.phoneNumber) },
                    onMessageDriver = onMessageDriver,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Controles de seguridad y acciones
            TripActionsCard(
                onEmergency = onEmergency,
                onShareTrip = onShareTrip,
                tripStatus = uiState.trip?.status
            )
        }
        
        // Botón de emergencia siempre visible
        FloatingActionButton(
            onClick = onEmergency,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ) {
            Icon(Icons.Default.Warning, contentDescription = "Emergencia")
        }
        
        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Error handling
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.retry() }) {
                        Text("Reintentar")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
private fun TripStatusCard(
    trip: Trip?,
    estimatedArrival: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estado del viaje
                Column {
                    Text(
                        text = when (trip?.status) {
                            TripStatus.ACCEPTED -> "Conductor asignado"
                            TripStatus.DRIVER_ARRIVING -> "Conductor en camino"
                            TripStatus.IN_PROGRESS -> "En viaje"
                            TripStatus.DRIVER_ARRIVED -> "Conductor llegó"
                            else -> "Preparando viaje"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // ETA
                    estimatedArrival?.let { eta ->
                        Text(
                            text = "Llegada estimada: $eta",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Indicador de estado
                StatusIndicator(status = trip?.status)
            }
            
            // Información de destino
            trip?.let { tripData ->
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Destino",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = tripData.dropoffLocation.address,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: TripStatus?) {
    val color = when (status) {
        TripStatus.ACCEPTED -> MaterialTheme.colorScheme.secondary
        TripStatus.DRIVER_ARRIVING -> Color(0xFFFF9800)
        TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        TripStatus.DRIVER_ARRIVED -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.outline
    }
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, CircleShape)
    )
}

@Composable
private fun DriverInfoCard(
    driver: Driver,
    onCallDriver: () -> Unit,
    onMessageDriver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del conductor
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del conductor
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = driver.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${driver.rating}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Text(
                    text = "${driver.vehicle.make} ${driver.vehicle.model} - ${driver.vehicle.licensePlate}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Acciones de comunicación
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onCallDriver,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Llamar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                IconButton(
                    onClick = onMessageDriver,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Message,
                        contentDescription = "Mensaje",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TripActionsCard(
    onEmergency: () -> Unit,
    onShareTrip: () -> Unit,
    tripStatus: TripStatus?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Compartir viaje
            ActionButton(
                icon = Icons.Default.Share,
                label = "Compartir",
                onClick = onShareTrip,
                backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.secondary
            )
            
            // Botón específico por estado
            when (tripStatus) {
                TripStatus.DRIVER_ARRIVING -> {
                    ActionButton(
                        icon = Icons.Default.Phone,
                        label = "Llamar",
                        onClick = { /* Ya manejado en DriverInfoCard */ },
                        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                }
                TripStatus.IN_PROGRESS -> {
                    ActionButton(
                        icon = Icons.Default.Navigation,
                        label = "Ruta",
                        onClick = { /* Mostrar ruta completa */ },
                        backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        iconTint = MaterialTheme.colorScheme.tertiary
                    )
                }
                else -> {
                    ActionButton(
                        icon = Icons.Default.Info,
                        label = "Detalles",
                        onClick = { /* Mostrar detalles del viaje */ },
                        backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        iconTint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    iconTint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(backgroundColor, CircleShape)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Extension function to convert TripLocation to Location
private fun TripLocation.toLocation(): Location {
    return Location(
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address,
        reference = this.sanJuanReference
    )
}