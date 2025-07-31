package com.mubitt.core.data.remote.dto

import com.mubitt.core.domain.model.Driver
import com.mubitt.core.domain.model.VehicleType
import com.mubitt.core.domain.model.DriverStatus

/**
 * DTOs para Driver API endpoints
 * Incluye información específica para conductores en San Juan
 */

data class DriverDto(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val profilePictureUrl: String?,
    val rating: Double,
    val tripCount: Int,
    val vehicleInfo: VehicleInfoDto,
    val licenseNumber: String,
    val isVerified: Boolean,
    val isActive: Boolean,
    val status: String,
    val currentLocation: LocationDto?,
    val joinedAt: String,
    val lastActiveAt: String,
    val specialties: List<String> = emptyList() // Ej: "conoce_san_juan_well", "eventos_especiales"
)

data class DriverLocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val heading: Double, // Dirección en grados
    val speed: Double, // km/h
    val timestamp: String
)

data class DriverStatusUpdate(
    val status: String, // AVAILABLE, BUSY, OFFLINE
    val location: LocationDto?
)

data class DriverEarningsDto(
    val today: Double,
    val week: Double,
    val month: Double,
    val totalTrips: Int,
    val rating: Double,
    val currency: String = "ARS"
)

data class DriverRegistrationRequest(
    val personalInfo: PersonalInfoDto,
    val vehicleInfo: VehicleInfoDto,
    val documents: DocumentsDto
)

data class PersonalInfoDto(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val licenseNumber: String,
    val birthDate: String,
    val address: String
)

data class DocumentsDto(
    val licensePhotoUrl: String,
    val vehicleRegistrationUrl: String,
    val insuranceUrl: String,
    val profilePhotoUrl: String,
    val backgroundCheckUrl: String? = null
)

/**
 * Extension functions para conversión a domain models
 */
fun DriverDto.toDomainModel(): Driver {
    return Driver(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        email = email,
        profilePictureUrl = profilePictureUrl,
        rating = rating,
        tripCount = tripCount,
        vehicleType = VehicleType.valueOf(vehicleInfo.type?.uppercase() ?: "ECONOMY"),
        licensePlate = vehicleInfo.licensePlate,
        isVerified = isVerified,
        isActive = isActive,
        status = DriverStatus.valueOf(status.uppercase()),
        currentLocation = currentLocation?.toDomainModel()
    )
}

// Extensión del VehicleInfoDto para incluir type
data class VehicleInfoDto(
    val make: String,
    val model: String,
    val color: String,
    val licensePlate: String,
    val year: Int,
    val type: String? = "ECONOMY" // ECONOMY, COMFORT, XL
)