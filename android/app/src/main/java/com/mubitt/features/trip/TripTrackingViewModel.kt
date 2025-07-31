package com.mubitt.features.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubitt.core.domain.model.Driver
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.Trip
import com.mubitt.core.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para TripTrackingScreen
 * Maneja el estado del viaje en tiempo real y la ubicación del conductor
 */
@HiltViewModel
class TripTrackingViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TripTrackingUiState())
    val uiState: StateFlow<TripTrackingUiState> = _uiState.asStateFlow()
    
    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Cargar trip desde repository
                val trip = tripRepository.getTripById(tripId)
                
                if (trip != null) {
                    _uiState.value = _uiState.value.copy(
                        trip = trip,
                        driver = createMockDriver(), // TODO: Obtener del trip
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "No se pudo cargar el viaje",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar el viaje: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun startTrackingTrip() {
        viewModelScope.launch {
            // TODO: Implementar tracking real con WebSockets
            // Por ahora simulamos actualizaciones
            simulateDriverTracking()
        }
    }
    
    fun retry() {
        _uiState.value.trip?.let { trip ->
            loadTrip(trip.id)
        }
    }
    
    private suspend fun simulateDriverTracking() {
        // Simular actualizaciones de ubicación del conductor
        val mockLocations = listOf(
            Location(-31.5400, -68.5300, "Conductor acercándose"),
            Location(-31.5385, -68.5320, "Conductor muy cerca"),
            Location(-31.5375, -68.5340, "Conductor llegando")
        )
        
        mockLocations.forEach { location ->
            kotlinx.coroutines.delay(10000) // 10 segundos entre actualizaciones
            _uiState.value = _uiState.value.copy(
                driverLocation = location,
                estimatedArrival = "5 min" // TODO: Calcular real
            )
        }
    }
    
    private fun createMockDriver(): Driver {
        return Driver(
            id = "driver_001",
            name = "Carlos Mendoza",
            phoneNumber = "+542644123456",
            rating = 4.8,
            vehicle = Driver.Vehicle(
                make = "Toyota",
                model = "Corolla",
                year = 2020,
                color = "Blanco",
                licensePlate = "ABC 123"
            ),
            location = Location(
                latitude = -31.5400,
                longitude = -68.5300,
                address = "Cerca de tu ubicación"
            ),
            isAvailable = true,
            isVerified = true
        )
    }
}

/**
 * Estado de UI para TripTrackingScreen
 */
data class TripTrackingUiState(
    val trip: Trip? = null,
    val driver: Driver? = null,
    val driverLocation: Location? = null,
    val routePolyline: List<Location> = emptyList(),
    val estimatedArrival: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)