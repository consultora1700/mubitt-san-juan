package com.mubitt.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mubitt.core.domain.model.Location
import com.mubitt.core.ui.components.maps.MubittMapView
import com.mubitt.features.maps.MapViewModel

/**
 * HomeScreen principal de Mubitt
 * Pantalla inicial optimizada para usuarios de San Juan
 * Incluye accesos rápidos a ubicaciones frecuentes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToBooking: (pickupLocation: Location, dropoffLocation: Location?) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    // Obtener ubicación actual al iniciar
    LaunchedEffect(Unit) {
        viewModel.getCurrentLocation()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa principal centrado en San Juan
        MubittMapView(
            modifier = Modifier.fillMaxSize(),
            initialLocation = uiState.currentLocation ?: Location(
                latitude = -31.5375,
                longitude = -68.5364,
                address = "San Juan, Argentina"
            ),
            pickupLocation = uiState.currentLocation,
            dropoffLocation = null,
            driverLocation = null,
            onLocationSelected = { location ->
                // Navegar directamente a booking con ubicación actual como pickup
                onNavigateToBooking(
                    uiState.currentLocation ?: location,
                    location
                )
            }
        )
        
        // Overlay con controles
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header con saludo y perfil
            HomeHeader(
                userName = "Usuario", // TODO: Obtener del perfil
                onProfileClick = onNavigateToProfile
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de búsqueda principal
            MainSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { 
                    searchQuery = it
                    viewModel.searchLocations(it)
                    isSearchExpanded = it.isNotEmpty()
                },
                onSearchClick = {
                    isSearchExpanded = true
                },
                placeholder = "¿A dónde vamos hoy?"
            )
            
            // Resultados de búsqueda
            AnimatedVisibility(
                visible = isSearchExpanded && uiState.hasSearchResults
            ) {
                HomeSearchResults(
                    results = uiState.searchResults,
                    onLocationSelected = { location ->
                        onNavigateToBooking(
                            uiState.currentLocation ?: location,
                            location
                        )
                        searchQuery = ""
                        isSearchExpanded = false
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Accesos rápidos San Juan (cuando no hay búsqueda activa)
            AnimatedVisibility(visible = !isSearchExpanded) {
                QuickAccessSanJuan(
                    currentLocation = uiState.currentLocation,
                    onDestinationSelected = { destination ->
                        onNavigateToBooking(
                            uiState.currentLocation ?: destination,
                            destination
                        )
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Acciones rápidas del usuario
            AnimatedVisibility(visible = !isSearchExpanded) {
                UserQuickActions(
                    onHistoryClick = onNavigateToHistory,
                    onScheduleRideClick = { 
                        // TODO: Implementar viajes programados
                    }
                )
            }
        }
        
        // Botón "Mi ubicación"
        FloatingActionButton(
            onClick = { viewModel.getCurrentLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
        }
        
        // Indicador de carga
        if (uiState.isLoadingLocation) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Mensaje de error
        uiState.locationError?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(
                        onClick = { viewModel.getCurrentLocation() }
                    ) {
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
private fun HomeHeader(
    userName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "¡Hola, $userName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "¿A dónde vamos hoy?",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        
        // Avatar del usuario
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Perfil",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    placeholder: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun HomeSearchResults(
    results: List<Location>,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            results.take(5).forEach { location ->
                ListItem(
                    headlineContent = { 
                        Text(
                            text = location.address,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    supportingContent = location.reference?.let { reference ->
                        { Text(reference, color = MaterialTheme.colorScheme.primary) }
                    },
                    leadingContent = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLocationSelected(location) }
                )
                if (location != results.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun QuickAccessSanJuan(
    currentLocation: Location?,
    onDestinationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickDestinations = remember {
        listOf(
            QuickDestination(
                name = "Hospital Rawson",
                icon = Icons.Default.LocalHospital,
                location = Location(
                    latitude = -31.5401,
                    longitude = -68.5298,
                    address = "Hospital Rawson, San Juan",
                    reference = "Hospital público principal"
                )
            ),
            QuickDestination(
                name = "UNSJ",
                icon = Icons.Default.School,
                location = Location(
                    latitude = -31.5344,
                    longitude = -68.5258,
                    address = "Universidad Nacional de San Juan",
                    reference = "Campus universitario"
                )
            ),
            QuickDestination(
                name = "Shopping del Sol",
                icon = Icons.Default.ShoppingCart,
                location = Location(
                    latitude = -31.5420,
                    longitude = -68.5280,
                    address = "Shopping del Sol, San Juan",
                    reference = "Centro comercial principal"
                )
            ),
            QuickDestination(
                name = "Centro",
                icon = Icons.Default.LocationCity,
                location = Location(
                    latitude = -31.5375,
                    longitude = -68.5364,
                    address = "Centro de San Juan",
                    reference = "Plaza 25 de Mayo"
                )
            ),
            QuickDestination(
                name = "Aeropuerto",
                icon = Icons.Default.Flight,
                location = Location(
                    latitude = -31.5712,
                    longitude = -68.4182,
                    address = "Aeropuerto Domingo Faustino Sarmiento",
                    reference = "Aeropuerto de San Juan"
                )
            )
        )
    }
    
    Column(modifier = modifier) {
        Text(
            text = "Destinos populares en San Juan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(quickDestinations) { destination ->
                QuickDestinationCard(
                    destination = destination,
                    onClick = { onDestinationSelected(destination.location) }
                )
            }
        }
    }
}

@Composable
private fun QuickDestinationCard(
    destination: QuickDestination,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                destination.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = destination.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun UserQuickActions(
    onHistoryClick: () -> Unit,
    onScheduleRideClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.History,
                label = "Historial",
                onClick = onHistoryClick
            )
            
            QuickActionButton(
                icon = Icons.Default.Schedule,
                label = "Programar",
                onClick = onScheduleRideClick
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class QuickDestination(
    val name: String,
    val icon: ImageVector,
    val location: Location
)