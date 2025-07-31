package com.mubitt.features.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubitt.core.domain.model.Driver
import com.mubitt.core.domain.model.Trip
import com.mubitt.core.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para TripCompletedScreen
 * Maneja calificaciones, propinas y finalización del viaje
 */
@HiltViewModel
class TripCompletedViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TripCompletedUiState())
    val uiState: StateFlow<TripCompletedUiState> = _uiState.asStateFlow()
    
    fun loadTripDetails(trip: Trip) {
        viewModelScope.launch {
            try {
                // Calcular duración y distancia del viaje
                val duration = calculateTripDuration(trip)
                val distance = calculateTripDistance(trip)
                val driver = getDriverForTrip(trip.driverId)
                
                _uiState.value = _uiState.value.copy(
                    trip = trip,
                    driver = driver,
                    tripDuration = duration,
                    tripDistance = distance
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cargar detalles del viaje: ${e.message}"
                )
            }
        }
    }
    
    fun updateDriverRating(rating: Int) {
        _uiState.value = _uiState.value.copy(
            driverRating = rating,
            canSubmitRating = rating > 0
        )
    }
    
    fun updateRatingComment(comment: String) {
        _uiState.value = _uiState.value.copy(
            ratingComment = comment
        )
    }
    
    fun updateTip(tipAmount: Double) {
        _uiState.value = _uiState.value.copy(
            selectedTip = tipAmount
        )
    }
    
    fun updateCustomTip(customTip: String) {
        // Validar que solo contenga números y punto decimal
        val cleanTip = customTip.filter { it.isDigit() || it == '.' }
        _uiState.value = _uiState.value.copy(
            customTip = cleanTip
        )
    }
    
    fun submitRating(onSuccess: () -> Unit) {
        if (!_uiState.value.canSubmitRating) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingRating = true)
            
            try {
                val state = _uiState.value
                val finalTip = if (state.customTip.isNotEmpty()) {
                    state.customTip.toDoubleOrNull() ?: 0.0
                } else {
                    state.selectedTip
                }
                
                // Enviar calificación al backend
                val success = tripRepository.submitTripRating(
                    tripId = state.trip?.id ?: "",
                    driverRating = state.driverRating,
                    comment = state.ratingComment,
                    tip = finalTip
                )
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingRating = false,
                        isRatingSubmitted = true
                    )
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmittingRating = false,
                        error = "Error al enviar la calificación. Inténtalo de nuevo."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmittingRating = false,
                    error = "Error de conexión: ${e.message}"
                )
            }
        }
    }
    
    private fun calculateTripDuration(trip: Trip): String {
        // TODO: Calcular duración real basada en timestamps
        // Por ahora retornamos un valor simulado
        val durationMinutes = (15..45).random()
        return "$durationMinutes min"
    }
    
    private fun calculateTripDistance(trip: Trip): String {
        // TODO: Calcular distancia real basada en coordenadas
        // Por ahora retornamos un valor simulado
        val distanceKm = (2.5..15.8).random()
        return "${String.format("%.1f", distanceKm)} km"
    }
    
    private suspend fun getDriverForTrip(driverId: String?): Driver? {
        // TODO: Obtener driver real del repository
        return createMockDriver()
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
            location = com.mubitt.core.domain.model.Location(
                latitude = -31.5400,
                longitude = -68.5300,
                address = "San Juan Centro"
            ),
            isAvailable = true,
            isVerified = true
        )
    }
}

/**
 * Estado de UI para TripCompletedScreen
 */
data class TripCompletedUiState(
    val trip: Trip? = null,
    val driver: Driver? = null,
    val tripDuration: String? = null,
    val tripDistance: String? = null,
    val driverRating: Int = 0,
    val ratingComment: String = "",
    val selectedTip: Double = 0.0,
    val customTip: String = "",
    val isSubmittingRating: Boolean = false,
    val isRatingSubmitted: Boolean = false,
    val canSubmitRating: Boolean = false,
    val error: String? = null
)