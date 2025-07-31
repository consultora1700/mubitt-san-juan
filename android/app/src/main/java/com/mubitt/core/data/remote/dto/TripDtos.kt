package com.mubitt.core.data.remote.dto

import com.mubitt.core.domain.model.Trip
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.TripStatus
import com.mubitt.core.domain.model.VehicleType

/**
 * DTOs para Trip API endpoints
 * Incluye validaciones específicas para San Juan
 */

data class CreateTripRequest(
    val pickupLocation: LocationDto,
    val dropoffLocation: LocationDto,
    val vehicleType: String,
    val scheduledTime: String? = null, // Para viajes programados
    val notes: String? = null,
    val paymentMethodId: String
)

data class TripSearchRequest(
    val pickupLocation: LocationDto,
    val radius: Double = 5.0, // Radio en km para buscar conductores
    val vehicleType: String
)

data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val reference: String? = null, // Referencias sanjuaninas como "Cerca del semáforo"
    val postalCode: String? = null
)

data class TripDto(
    val id: String,
    val passengerId: String,
    val driverId: String?,
    val pickupLocation: LocationDto,
    val dropoffLocation: LocationDto,
    val status: String,
    val vehicleType: String,
    val estimatedFare: Double,
    val actualFare: Double?,
    val distance: Double, // En kilómetros
    val estimatedDuration: Int, // En minutos
    val actualDuration: Int?,
    val scheduledTime: String?,
    val createdAt: String,
    val startedAt: String?,
    val completedAt: String?,
    val cancelledAt: String?,
    val rating: Int?,
    val feedback: String?
)

data class DriverMatchDto(
    val driverId: String,
    val name: String,
    val rating: Double,
    val vehicleInfo: VehicleInfoDto,
    val location: LocationDto,
    val estimatedArrival: Int, // Minutos hasta pickup
    val distance: Double // Distancia al pickup en km
)

data class VehicleInfoDto(
    val make: String,
    val model: String,
    val color: String,
    val licensePlate: String,
    val year: Int
)

data class FareEstimateDto(
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val surgeFactor: Double,
    val totalFare: Double,
    val currency: String = "ARS"
)

/**
 * Extension functions para conversión a domain models
 */
fun LocationDto.toDomainModel(): Location {
    return Location(
        latitude = latitude,
        longitude = longitude,
        address = address,
        reference = reference
    )
}

fun TripDto.toDomainModel(): Trip {
    return Trip(
        id = id,
        passengerId = passengerId,
        driverId = driverId,
        pickupLocation = pickupLocation.toDomainModel(),
        dropoffLocation = dropoffLocation.toDomainModel(),
        status = TripStatus.valueOf(status.uppercase()),
        vehicleType = VehicleType.valueOf(vehicleType.uppercase()),
        estimatedFare = estimatedFare,
        actualFare = actualFare,
        distance = distance,
        estimatedDuration = estimatedDuration,
        scheduledTime = scheduledTime
    )
}