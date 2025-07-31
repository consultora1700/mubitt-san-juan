package com.mubitt.features.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubitt.core.data.location.LocationService
import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.SanJuanReference
import com.mubitt.core.domain.usecase.SearchSanJuanLocationUseCase
import com.mubitt.core.domain.usecase.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para manejo de mapas y ubicaciones
 * Integra LocationService con UI states para Compose
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationService: LocationService,
    private val searchSanJuanLocationUseCase: SearchSanJuanLocationUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        // Observar actualizaciones de ubicación en tiempo real
        observeLocationUpdates()
    }
    
    /**
     * Obtiene la ubicación actual del usuario
     */
    fun getCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocation = true)
            
            when (val result = locationService.getCurrentLocation()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        currentLocation = result.data,
                        isLoadingLocation = false,
                        locationError = null
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        locationError = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoadingLocation = true)
                }
            }
        }
    }
    
    /**
     * Busca ubicaciones por texto con inteligencia local San Juan
     */
    fun searchLocations(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(sanJuanSearchResults = emptyList())
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            
            // Búsqueda inteligente local usando San Juan Use Case
            val localResults = searchSanJuanLocationUseCase.searchLocation(query)
            
            // También buscar con el servicio remoto si hay conexión
            when (val remoteResult = locationService.searchLocations(query)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        sanJuanSearchResults = localResults,
                        searchResults = remoteResult.data,
                        isSearching = false,
                        searchError = null
                    )
                }
                is ApiResult.Error -> {
                    // Usar solo resultados locales si falla el remoto
                    _uiState.value = _uiState.value.copy(
                        sanJuanSearchResults = localResults,
                        isSearching = false,
                        searchError = if (localResults.isEmpty()) remoteResult.message else null,
                        searchResults = emptyList()
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        sanJuanSearchResults = localResults,
                        isSearching = true
                    )
                }
            }
        }
    }
    
    /**
     * Selecciona una referencia de San Juan y la convierte a Location
     */
    fun selectSanJuanReference(reference: SanJuanReference) {
        _uiState.value = _uiState.value.copy(
            selectedReference = reference,
            sanJuanSearchResults = emptyList()
        )
    }
    
    /**
     * Establece la ubicación de pickup
     */
    fun setPickupLocation(location: Location) {
        _uiState.value = _uiState.value.copy(pickupLocation = location)
        
        // Si ya hay dropoff, calcular distancia
        _uiState.value.dropoffLocation?.let { dropoff ->
            calculateDistance(location, dropoff)
        }
    }
    
    /**
     * Establece la ubicación de dropoff
     */
    fun setDropoffLocation(location: Location) {
        _uiState.value = _uiState.value.copy(dropoffLocation = location)
        
        // Si ya hay pickup, calcular distancia
        _uiState.value.pickupLocation?.let { pickup ->
            calculateDistance(pickup, location)
        }
    }
    
    /**
     * Actualiza la posición del conductor
     */
    fun updateDriverLocation(location: Location) {
        _uiState.value = _uiState.value.copy(driverLocation = location)
    }
    
    /**
     * Limpia las ubicaciones seleccionadas
     */
    fun clearLocations() {
        _uiState.value = _uiState.value.copy(
            pickupLocation = null,
            dropoffLocation = null,
            driverLocation = null,
            estimatedDistance = null
        )
    }
    
    /**
     * Limpia los resultados de búsqueda
     */
    fun clearSearchResults() {
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            sanJuanSearchResults = emptyList(),
            selectedReference = null
        )
    }
    
    /**
     * Verifica si las ubicaciones están en San Juan
     */
    fun validateSanJuanLocations(): Boolean {
        val pickup = _uiState.value.pickupLocation
        val dropoff = _uiState.value.dropoffLocation
        
        if (pickup == null || dropoff == null) return false
        
        val pickupInSanJuan = locationService.isLocationInSanJuan(pickup)
        val dropoffInSanJuan = locationService.isLocationInSanJuan(dropoff)
        
        if (!pickupInSanJuan || !dropoffInSanJuan) {
            _uiState.value = _uiState.value.copy(
                locationError = "Los viajes deben ser dentro de San Juan"
            )
            return false
        }
        
        return true
    }
    
    // Métodos privados
    
    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationService.observeLocationUpdates()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        locationError = "Error actualizando ubicación: ${error.message}"
                    )
                }
                .collect { location ->
                    _uiState.value = _uiState.value.copy(
                        currentLocation = location,
                        locationError = null
                    )
                }
        }
    }
    
    private fun calculateDistance(from: Location, to: Location) {
        val distance = locationService.calculateDistance(from, to)
        _uiState.value = _uiState.value.copy(
            estimatedDistance = distance
        )
    }
}

/**
 * Estado de UI para el mapa
 */
data class MapUiState(
    val currentLocation: Location? = null,
    val pickupLocation: Location? = null,
    val dropoffLocation: Location? = null,
    val driverLocation: Location? = null,
    val searchResults: List<Location> = emptyList(),
    val sanJuanSearchResults: List<SearchResult> = emptyList(),
    val selectedReference: SanJuanReference? = null,
    val estimatedDistance: Double? = null,
    val isLoadingLocation: Boolean = false,
    val isSearching: Boolean = false,
    val locationError: String? = null,
    val searchError: String? = null
) {
    /**
     * Indica si se puede solicitar un viaje
     */
    val canRequestTrip: Boolean
        get() = pickupLocation != null && dropoffLocation != null && locationError == null
    
    /**
     * Indica si hay búsqueda activa
     */
    val hasSearchResults: Boolean
        get() = searchResults.isNotEmpty() || sanJuanSearchResults.isNotEmpty()
    
    /**
     * Indica si hay resultados de San Juan específicos
     */
    val hasSanJuanResults: Boolean
        get() = sanJuanSearchResults.isNotEmpty()
}