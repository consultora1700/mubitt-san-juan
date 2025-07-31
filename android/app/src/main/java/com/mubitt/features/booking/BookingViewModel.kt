package com.mubitt.features.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.VehicleType
import com.mubitt.core.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para BookingScreen
 * Maneja estimación de tarifas y solicitud de viajes
 */
@HiltViewModel
class BookingViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    
    /**
     * Establece las ubicaciones del viaje
     */
    fun setTripLocations(pickupLocation: Location, dropoffLocation: Location) {
        _uiState.value = _uiState.value.copy(
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation
        )
    }
    
    /**
     * Estima la tarifa del viaje
     */
    fun estimateFare(
        pickupLocation: Location,
        dropoffLocation: Location,
        vehicleType: VehicleType
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isEstimatingFare = true,
                estimatedFare = null
            )
            
            when (val result = tripRepository.estimateFare(pickupLocation, dropoffLocation, vehicleType)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isEstimatingFare = false,
                        estimatedFare = result.data,
                        estimatedTime = calculateEstimatedTime(pickupLocation, dropoffLocation)
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isEstimatingFare = false,
                        errorMessage = "Error estimando tarifa: ${result.message}"
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isEstimatingFare = true)
                }
            }
        }
    }
    
    /**
     * Solicita un viaje
     */
    fun requestTrip(
        pickupLocation: Location,
        dropoffLocation: Location,
        vehicleType: VehicleType,
        paymentMethodId: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRequestingTrip = true,
                errorMessage = null
            )
            
            when (val result = tripRepository.createTrip(
                pickupLocation = pickupLocation,
                dropoffLocation = dropoffLocation,
                vehicleType = vehicleType,
                paymentMethodId = paymentMethodId
            )) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRequestingTrip = false,
                        isTripRequested = true,
                        createdTrip = result.data
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRequestingTrip = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isRequestingTrip = true)
                }
            }
        }
    }
    
    /**
     * Limpia mensajes de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Calcula tiempo estimado basado en distancia
     */
    private fun calculateEstimatedTime(pickup: Location, dropoff: Location): Int {
        // Cálculo simple: distancia haversine * factor de tiempo
        val distance = calculateDistance(pickup, dropoff)
        val speedKmH = 30.0 // Velocidad promedio en San Juan
        return ((distance / speedKmH) * 60).toInt() // Convertir a minutos
    }
    
    /**
     * Calcula distancia haversine entre dos puntos
     */
    private fun calculateDistance(pickup: Location, dropoff: Location): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en km
        
        val dLat = Math.toRadians(dropoff.latitude - pickup.latitude)
        val dLng = Math.toRadians(dropoff.longitude - pickup.longitude)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(pickup.latitude)) * Math.cos(Math.toRadians(dropoff.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }
}

/**
 * Estado de UI para BookingScreen
 */
data class BookingUiState(
    val pickupLocation: Location? = null,
    val dropoffLocation: Location? = null,
    val estimatedFare: Double? = null,
    val estimatedTime: Int? = null,
    val isEstimatingFare: Boolean = false,
    val isRequestingTrip: Boolean = false,
    val isTripRequested: Boolean = false,
    val createdTrip: com.mubitt.core.domain.model.Trip? = null,
    val errorMessage: String? = null
)