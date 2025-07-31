package com.mubitt.features.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mubitt.core.domain.model.Location
import com.mubitt.core.ui.components.maps.MubittMapView
import kotlinx.coroutines.delay

/**
 * MapScreen principal de Mubitt
 * Pantalla central que integra mapa, búsqueda y booking
 * Diseñada para competir directamente con Uber
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToBooking: (pickupLocation: Location, dropoffLocation: Location) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var activeSearchField by remember { mutableStateOf(SearchField.PICKUP) }
    
    // Obtener ubicación actual al iniciar
    LaunchedEffect(Unit) {
        viewModel.getCurrentLocation()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa principal
        MubittMapView(
            modifier = Modifier.fillMaxSize(),
            initialLocation = uiState.currentLocation,
            pickupLocation = uiState.pickupLocation,
            dropoffLocation = uiState.dropoffLocation,
            driverLocation = uiState.driverLocation,
            onLocationSelected = { location ->
                when (activeSearchField) {
                    SearchField.PICKUP -> viewModel.setPickupLocation(location)
                    SearchField.DROPOFF -> viewModel.setDropoffLocation(location)
                }
                isSearchExpanded = false
                focusManager.clearFocus()
            }
        )
        
        // Overlay con controles
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Barra de búsqueda superior
            SearchCard(
                searchQuery = searchQuery,
                onSearchQueryChange = { 
                    searchQuery = it
                    viewModel.searchLocations(it)
                },
                pickupLocation = uiState.pickupLocation,
                dropoffLocation = uiState.dropoffLocation,
                onPickupClick = {
                    activeSearchField = SearchField.PICKUP
                    isSearchExpanded = true
                },
                onDropoffClick = {
                    activeSearchField = SearchField.DROPOFF
                    isSearchExpanded = true
                },
                onSwapLocations = {
                    val pickup = uiState.pickupLocation
                    val dropoff = uiState.dropoffLocation
                    if (pickup != null && dropoff != null) {
                        viewModel.setPickupLocation(dropoff)
                        viewModel.setDropoffLocation(pickup)
                    }
                }
            )
            
            // Resultados de búsqueda
            AnimatedVisibility(
                visible = isSearchExpanded && uiState.hasSearchResults
            ) {
                SearchResultsCard(
                    results = uiState.searchResults,
                    onLocationSelected = { location ->
                        when (activeSearchField) {
                            SearchField.PICKUP -> viewModel.setPickupLocation(location)
                            SearchField.DROPOFF -> viewModel.setDropoffLocation(location)
                        }
                        searchQuery = location.address
                        isSearchExpanded = false
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Información de viaje y botón de booking
            if (uiState.canRequestTrip) {
                TripInfoCard(
                    pickupLocation = uiState.pickupLocation!!,
                    dropoffLocation = uiState.dropoffLocation!!,
                    estimatedDistance = uiState.estimatedDistance,
                    onBookRide = {
                        onNavigateToBooking(
                            uiState.pickupLocation!!,
                            uiState.dropoffLocation!!
                        )
                    }
                )
            }
        }
        
        // Botón "Mi ubicación"
        FloatingActionButton(
            onClick = { viewModel.getCurrentLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .offset(y = if (uiState.canRequestTrip) (-120).dp else 0.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Mi ubicación")
        }
        
        // Indicador de carga
        if (uiState.isLoadingLocation) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }
        
        // Mensaje de error
        uiState.locationError?.let { error ->
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    pickupLocation: Location?,
    dropoffLocation: Location?,
    onPickupClick: () -> Unit,
    onDropoffClick: () -> Unit,
    onSwapLocations: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Campo de pickup
            OutlinedTextField(
                value = pickupLocation?.address ?: "¿Desde dónde?",
                onValueChange = { },
                readOnly = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                onClick = onPickupClick
            )
            
            // Campo de dropoff
            OutlinedTextField(
                value = dropoffLocation?.address ?: "¿A dónde vas?",
                onValueChange = { },
                readOnly = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                onClick = onDropoffClick
            )
            
            // Botón intercambiar ubicaciones
            if (pickupLocation != null && dropoffLocation != null) {
                TextButton(
                    onClick = onSwapLocations,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Intercambiar")
                }
            }
        }
    }
}

@Composable
private fun SearchResultsCard(
    results: List<Location>,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            items(results) { location ->
                ListItem(
                    headlineContent = { 
                        Text(
                            text = location.address,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    supportingContent = location.reference?.let { reference ->
                        { Text(reference) }
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
private fun TripInfoCard(
    pickupLocation: Location,
    dropoffLocation: Location,
    estimatedDistance: Double?,
    onBookRide: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tu viaje",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    estimatedDistance?.let { distance ->
                        Text(
                            text = "${String.format("%.1f", distance)} km",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Button(
                    onClick = onBookRide,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Solicitar Viaje")
                }
            }
        }
    }
}

private enum class SearchField {
    PICKUP, DROPOFF
}