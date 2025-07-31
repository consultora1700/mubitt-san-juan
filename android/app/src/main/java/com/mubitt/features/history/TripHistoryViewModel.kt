package com.mubitt.features.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * TripHistoryViewModel - Gestión de historial de viajes
 * Características específicas San Juan:
 * - Filtros por zonas y referencias locales
 * - Estadísticas personalizadas para usuarios sanjuaninos
 * - Re-solicitar viajes frecuentes con rutas conocidas
 */
@HiltViewModel
class TripHistoryViewModel @Inject constructor(
    // private val tripRepository: TripRepository // Inyectar cuando esté disponible
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TripHistoryUiState())
    val uiState: StateFlow<TripHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadTripHistory()
        loadUserStats()
    }
    
    private fun loadTripHistory() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Simulación de datos - reemplazar con repository real
                val mockTrips = generateMockTrips()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    trips = mockTrips,
                    filteredTrips = mockTrips
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar historial: ${e.message}"
                )
            }
        }
    }
    
    private fun loadUserStats() {
        viewModelScope.launch {
            try {
                // Calcular estadísticas basadas en los viajes
                val trips = _uiState.value.trips
                val completedTrips = trips.filter { it.status == TripStatus.COMPLETED }
                
                if (completedTrips.isNotEmpty()) {
                    val stats = UserTripStats(
                        totalTrips = completedTrips.size,
                        totalSpent = completedTrips.sumOf { it.fare },
                        averageRating = completedTrips.map { it.driverRating }
                            .filter { it > 0 }.average().toFloat(),
                        totalDistance = completedTrips.sumOf { it.distance }.toInt()
                    )
                    
                    _uiState.value = _uiState.value.copy(userStats = stats)
                }
                
            } catch (e: Exception) {
                // Log error but don't show to user as it's not critical
            }
        }
    }
    
    fun updateFilter(filter: TripFilter) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val filteredTrips = when (filter) {
                TripFilter.ALL -> currentState.trips
                TripFilter.THIS_WEEK -> {
                    val weekAgo = Calendar.getInstance().apply {
                        add(Calendar.WEEK_OF_YEAR, -1)
                    }.time
                    currentState.trips.filter { it.date.after(weekAgo) }
                }
                TripFilter.THIS_MONTH -> {
                    val monthAgo = Calendar.getInstance().apply {
                        add(Calendar.MONTH, -1)
                    }.time
                    currentState.trips.filter { it.date.after(monthAgo) }
                }
                TripFilter.COMPLETED -> {
                    currentState.trips.filter { it.status == TripStatus.COMPLETED }
                }
                TripFilter.CANCELLED -> {
                    currentState.trips.filter { it.status == TripStatus.CANCELLED }
                }
                TripFilter.FAVORITES -> {
                    // Rutas frecuentes - viajes repetidos a mismas ubicaciones
                    val routeFrequency = currentState.trips
                        .groupBy { "${it.pickupAddress}-${it.dropoffAddress}" }
                        .filter { it.value.size > 1 }
                        .values
                        .flatten()
                    routeFrequency
                }
            }
            
            _uiState.value = currentState.copy(
                currentFilter = filter,
                filteredTrips = filteredTrips.sortedByDescending { it.date }
            )
        }
    }
    
    fun selectTrip(trip: TripHistory) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedTrip = trip)
            // TODO: Navegar a pantalla de detalles del viaje
        }
    }
    
    fun repeatTrip(trip: TripHistory) {
        viewModelScope.launch {
            try {
                // TODO: Integrar con BookingViewModel para crear nuevo viaje
                // con las mismas ubicaciones
                
                // Por ahora, simular el proceso
                _uiState.value = _uiState.value.copy(
                    isRepeatingTrip = true,
                    repeatingTripId = trip.id
                )
                
                // Simular delay de procesamiento
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isRepeatingTrip = false,
                    repeatingTripId = null
                )
                
                // TODO: Navegar a BookingScreen con ubicaciones pre-llenadas
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRepeatingTrip = false,
                    repeatingTripId = null,
                    errorMessage = "Error al repetir viaje: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun generateMockTrips(): List<TripHistory> {
        // Datos de prueba con referencias específicas de San Juan
        val sanJuanLocations = listOf(
            "Hospital Rawson, San Juan",
            "Universidad Nacional de San Juan (UNSJ)",
            "Shopping del Sol, San Juan",
            "Plaza 25 de Mayo, Centro",
            "Terminal de Ómnibus, San Juan",
            "Aeropuerto Domingo Faustino Sarmiento",
            "Parque de Mayo, San Juan",
            "Estadio San Juan del Bicentenario",
            "Centro Cívico, San Juan",
            "Catedral de San Juan",
            "Difunta Correa, Vallecito",
            "Mercado Modelo, San Juan",
            "Boulevard Madre Teresa de Calcuta",
            "Rivadavia Centro",
            "Desamparados Centro"
        )
        
        val trips = mutableListOf<TripHistory>()
        val calendar = Calendar.getInstance()
        
        repeat(15) { index ->
            calendar.add(Calendar.DAY_OF_YEAR, -index)
            
            val pickupLocation = sanJuanLocations.random()
            var dropoffLocation = sanJuanLocations.random()
            while (dropoffLocation == pickupLocation) {
                dropoffLocation = sanJuanLocations.random()
            }
            
            trips.add(
                TripHistory(
                    id = "trip_${System.currentTimeMillis()}_$index",
                    date = calendar.time,
                    pickupAddress = pickupLocation,
                    dropoffAddress = dropoffLocation,
                    fare = (800..2500).random().toDouble(),
                    paymentMethod = listOf("Efectivo", "MercadoPago", "Visa", "Mastercard").random(),
                    status = if (index < 2) TripStatus.CANCELLED else TripStatus.COMPLETED,
                    driverName = listOf("Carlos", "María", "Diego", "Ana", "Roberto", "Patricia").random(),
                    driverRating = 3.5f + (Math.random() * 1.5f).toFloat(),
                    duration = (10..45).random(),
                    distance = (2.0..15.0).random()
                )
            )
        }
        
        return trips.sortedByDescending { it.date }
    }
}

data class TripHistoryUiState(
    val isLoading: Boolean = false,
    val trips: List<TripHistory> = emptyList(),
    val filteredTrips: List<TripHistory> = emptyList(),
    val currentFilter: TripFilter = TripFilter.ALL,
    val selectedTrip: TripHistory? = null,
    val userStats: UserTripStats? = null,
    val isRepeatingTrip: Boolean = false,
    val repeatingTripId: String? = null,
    val errorMessage: String? = null
)