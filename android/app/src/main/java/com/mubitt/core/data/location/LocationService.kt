package com.mubitt.core.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.mubitt.core.data.local.SanJuanReferences
import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.domain.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Servicio de ubicación para Mubitt
 * Integra Google Location Services con referencias locales de San Juan
 */
@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    
    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000L)
            .setMaxUpdateDelayMillis(10000L)
            .build()
    }
    
    /**
     * Obtiene la ubicación actual del usuario
     */
    suspend fun getCurrentLocation(): ApiResult<Location> {
        return try {
            if (!hasLocationPermission()) {
                return ApiResult.Error("Permisos de ubicación no otorgados")
            }
            
            val location = getCurrentLocationInternal()
            if (location != null) {
                // Convertir a nuestro formato y agregar información de San Juan
                val sanJuanLocation = Location(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = getAddressFromLocation(location.latitude, location.longitude),
                    reference = getNearbyReference(location.latitude, location.longitude)
                )
                ApiResult.Success(sanJuanLocation)
            } else {
                ApiResult.Error("No se pudo obtener la ubicación actual")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error al obtener ubicación: ${e.message}")
        }
    }
    
    /**
     * Observa cambios en la ubicación en tiempo real
     */
    fun observeLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close(Exception("Permisos de ubicación no otorgados"))
            return@callbackFlow
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val sanJuanLocation = Location(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = getAddressFromLocation(location.latitude, location.longitude),
                        reference = getNearbyReference(location.latitude, location.longitude)
                    )
                    trySend(sanJuanLocation)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    /**
     * Busca ubicaciones por texto con inteligencia local de San Juan
     */
    suspend fun searchLocations(query: String): ApiResult<List<Location>> {
        return try {
            val results = mutableListOf<Location>()
            
            // 1. Buscar en landmarks locales de San Juan
            val landmarks = SanJuanReferences.searchLandmarks(query)
            results.addAll(landmarks.map { it.location })
            
            // 2. Buscar en barrios
            val neighborhoods = SanJuanReferences.searchNeighborhoods(query)
            neighborhoods.forEach { neighborhood ->
                val centerLat = (neighborhood.bounds.north + neighborhood.bounds.south) / 2
                val centerLng = (neighborhood.bounds.east + neighborhood.bounds.west) / 2
                results.add(
                    Location(
                        latitude = centerLat,
                        longitude = centerLng,
                        address = neighborhood.name + ", San Juan",
                        reference = "Barrio ${neighborhood.name}"
                    )
                )
            }
            
            // 3. Buscar referencias coloquiales
            val colloquialRefs = SanJuanReferences.searchColloquialReferences(query)
            // TODO: Implementar lógica para convertir referencias coloquiales a ubicaciones específicas
            
            // 4. Si no hay resultados locales, usar Google Places API
            if (results.isEmpty()) {
                // TODO: Implementar búsqueda con Google Places API
                // Por ahora retornamos resultado vacío
            }
            
            ApiResult.Success(results.take(10)) // Limitar a 10 resultados
        } catch (e: Exception) {
            ApiResult.Error("Error en búsqueda: ${e.message}")
        }
    }
    
    /**
     * Calcula distancia entre dos puntos en kilómetros
     */
    fun calculateDistance(from: Location, to: Location): Double {
        val fromLatLng = LatLng(from.latitude, from.longitude)
        val toLatLng = LatLng(to.latitude, to.longitude)
        return calculateDistanceInKm(fromLatLng, toLatLng)
    }
    
    /**
     * Verifica si una ubicación está dentro de San Juan
     */
    fun isLocationInSanJuan(location: Location): Boolean {
        // Bounds aproximados de San Juan (expandidos para incluir Gran San Juan)
        val sanJuanBounds = LocationBounds(
            north = -31.400,
            south = -31.700,
            east = -68.300,
            west = -68.700
        )
        
        return location.latitude >= sanJuanBounds.south &&
               location.latitude <= sanJuanBounds.north &&
               location.longitude >= sanJuanBounds.west &&
               location.longitude <= sanJuanBounds.east
    }
    
    // Métodos privados
    
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private suspend fun getCurrentLocationInternal(): android.location.Location? {
        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(null)
                    }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
    }
    
    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        // TODO: Implementar geocoding reverso para obtener dirección
        // Por ahora retornamos coordenadas
        return "Lat: ${String.format("%.4f", latitude)}, Lng: ${String.format("%.4f", longitude)}"
    }
    
    private fun getNearbyReference(latitude: Double, longitude: Double): String? {
        // Buscar landmark más cercano dentro de 1km
        val currentLocation = Location(latitude, longitude, "", null)
        
        return SanJuanReferences.MAIN_LANDMARKS
            .filter { calculateDistance(currentLocation, it.location) <= 1.0 }
            .minByOrNull { calculateDistance(currentLocation, it.location) }
            ?.let { "Cerca de ${it.name}" }
    }
    
    private fun calculateDistanceInKm(from: LatLng, to: LatLng): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en km
        
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(from.latitude)) * Math.cos(Math.toRadians(to.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return earthRadius * c
    }
}

// Importar desde SanJuanReferences
private typealias LocationBounds = com.mubitt.core.data.local.LocationBounds