package com.mubitt.features.booking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.VehicleType
import com.mubitt.core.ui.components.maps.MubittMapView

/**
 * BookingScreen para solicitar viajes
 * Pantalla optimizada para conversión rápida (3 taps máximo)
 * Diseño premium que compite con Uber
 */
@Composable
fun BookingScreen(
    pickupLocation: Location,
    dropoffLocation: Location,
    onNavigateBack: () -> Unit,
    onTripRequested: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedVehicleType by remember { mutableStateOf(VehicleType.ECONOMY) }
    var selectedPaymentMethod by remember { mutableStateOf("cash") } // Por ahora cash por defecto
    
    // Inicializar con ubicaciones
    LaunchedEffect(pickupLocation, dropoffLocation) {
        viewModel.setTripLocations(pickupLocation, dropoffLocation)
        viewModel.estimateFare(pickupLocation, dropoffLocation, selectedVehicleType)
    }
    
    // Actualizar estimación cuando cambie el tipo de vehículo
    LaunchedEffect(selectedVehicleType) {
        viewModel.estimateFare(pickupLocation, dropoffLocation, selectedVehicleType)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa en background
        MubittMapView(
            modifier = Modifier.fillMaxSize(),
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation
        )
        
        // Overlay oscuro para mejor legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        // Content principal
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con botón back
            TopAppBar(
                title = { Text("Confirmar viaje") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Card principal con información del viaje
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500)
                ) + fadeIn()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Información de ubicaciones
                        TripLocationInfo(
                            pickupLocation = pickupLocation,
                            dropoffLocation = dropoffLocation,
                            estimatedTime = uiState.estimatedTime
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Selector de tipo de vehículo
                        VehicleTypeSelector(
                            selectedType = selectedVehicleType,
                            onTypeSelected = { selectedVehicleType = it },
                            estimatedFare = uiState.estimatedFare,
                            isLoading = uiState.isEstimatingFare
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Método de pago
                        PaymentMethodSelector(
                            selectedMethod = selectedPaymentMethod,
                            onMethodSelected = { selectedPaymentMethod = it }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Botón de confirmar viaje
                        Button(
                            onClick = {
                                viewModel.requestTrip(
                                    pickupLocation = pickupLocation,
                                    dropoffLocation = dropoffLocation,
                                    vehicleType = selectedVehicleType,
                                    paymentMethodId = selectedPaymentMethod
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !uiState.isRequestingTrip && uiState.estimatedFare != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (uiState.isRequestingTrip) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Solicitar Viaje",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    uiState.estimatedFare?.let { fare ->
                                        Text(
                                            text = " • \$${String.format("%.0f", fare)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Información adicional
                        Text(
                            text = "Se buscará el conductor más cercano. Tarifa final puede variar.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
            }
        }
        
        // Mostrar error si existe
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
    
    // Navegar cuando el viaje sea solicitado exitosamente
    LaunchedEffect(uiState.isTripRequested) {
        if (uiState.isTripRequested) {
            onTripRequested()
        }
    }
}

@Composable
private fun TripLocationInfo(
    pickupLocation: Location,
    dropoffLocation: Location,
    estimatedTime: Int?
) {
    Column {
        Text(
            text = "Tu viaje",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Pickup
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "Origen",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = pickupLocation.address,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                pickupLocation.reference?.let { reference ->
                    Text(
                        text = reference,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Dropoff
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = "Destino",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = dropoffLocation.address,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                dropoffLocation.reference?.let { reference ->
                    Text(
                        text = reference,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        estimatedTime?.let { time ->
            Text(
                text = "Tiempo estimado: $time min",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun VehicleTypeSelector(
    selectedType: VehicleType,
    onTypeSelected: (VehicleType) -> Unit,
    estimatedFare: Double?,
    isLoading: Boolean
) {
    Column {
        Text(
            text = "Tipo de vehículo",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(VehicleType.values()) { vehicleType ->
                VehicleTypeCard(
                    vehicleType = vehicleType,
                    isSelected = vehicleType == selectedType,
                    estimatedFare = if (vehicleType == selectedType) estimatedFare else null,
                    isLoading = isLoading && vehicleType == selectedType,
                    onClick = { onTypeSelected(vehicleType) }
                )
            }
        }
    }
}

@Composable
private fun VehicleTypeCard(
    vehicleType: VehicleType,
    isSelected: Boolean,
    estimatedFare: Double?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TODO: Agregar iconos de vehículos
            Text(
                text = when (vehicleType) {
                    VehicleType.ECONOMY -> "🚗"
                    VehicleType.COMFORT -> "🚙"
                    VehicleType.XL -> "🚐"
                },
                fontSize = 24.sp
            )
            
            Text(
                text = when (vehicleType) {
                    VehicleType.ECONOMY -> "Económico"
                    VehicleType.COMFORT -> "Comfort"
                    VehicleType.XL -> "XL"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                estimatedFare?.let { fare ->
                    Text(
                        text = "\$${String.format("%.0f", fare)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodSelector(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit
) {
    Column {
        Text(
            text = "Método de pago",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Por ahora solo efectivo (MVP)
        Card(
            onClick = { onMethodSelected("cash") },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedMethod == "cash") 
                    MaterialTheme.colorScheme.primaryContainer
                else 
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Money,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Efectivo",
                    modifier = Modifier.padding(start = 12.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                if (selectedMethod == "cash") {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Text(
            text = "MercadoPago y tarjetas próximamente",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}