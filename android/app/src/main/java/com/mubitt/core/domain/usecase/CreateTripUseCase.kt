package com.mubitt.core.domain.usecase

import com.mubitt.core.domain.model.*
import com.mubitt.core.domain.repository.TripRepository
import com.mubitt.core.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Create Trip Use Case
 * 
 * Clean Architecture - Domain Layer Use Case
 * Standards: Business logic encapsulation, validation
 * Features: Fare calculation, driver matching, San Juan optimization
 */

class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
    private val userRepository: UserRepository
) {
    
    suspend operator fun invoke(request: CreateTripRequest): Result<CreateTripResponse> {
        return try {
            // 1. Validate user
            val currentUser = userRepository.getCurrentUser().getOrNull()
                ?: return Result.failure(Exception("User not authenticated"))
            
            // 2. Validate trip request
            val validationResult = validateTripRequest(request)
            if (!validationResult.isValid) {
                return Result.failure(Exception(validationResult.errorMessage))
            }
            
            // 3. Calculate fare
            val fareResult = tripRepository.calculateFare(
                pickupLocation = request.pickupLocation,
                dropoffLocation = request.dropoffLocation,
                vehicleType = request.vehicleType,
                currentDemand = request.demandLevel,
                userType = if (currentUser.isEligibleForStudentDiscount()) UserType.PASSENGER else UserType.PASSENGER
            )
            
            val fareBreakdown = fareResult.getOrNull()
                ?: return Result.failure(Exception("Unable to calculate fare"))
            
            // 4. Check user can afford the trip
            if (!canUserAffordTrip(currentUser, fareBreakdown)) {
                return Result.failure(Exception("Insufficient funds or payment method required"))
            }
            
            // 5. Find available drivers
            val driversResult = tripRepository.findAvailableDrivers(
                pickupLocation = Location(
                    latitude = request.pickupLocation.latitude,
                    longitude = request.pickupLocation.longitude
                ),
                vehicleType = request.vehicleType,
                maxRadius = 15.0 // 15km for San Juan
            )
            
            val availableDrivers = driversResult.getOrNull() ?: emptyList()
            if (availableDrivers.isEmpty()) {
                return Result.failure(Exception("No drivers available in your area"))
            }
            
            // 6. Create trip
            val tripResult = tripRepository.createTrip(
                passengerId = currentUser.id,
                pickupLocation = request.pickupLocation,
                dropoffLocation = request.dropoffLocation,
                vehicleType = request.vehicleType,
                paymentMethod = request.paymentMethod,
                specialRequests = request.specialRequests
            )
            
            val trip = tripResult.getOrNull()
                ?: return Result.failure(Exception("Failed to create trip"))
            
            // 7. Update trip with fare
            tripRepository.updateTripFare(trip.id, fareBreakdown)
            
            // 8. Request driver assignment
            val driversForRequest = tripRepository.requestDriverForTrip(trip.id)
            val requestedDrivers = driversForRequest.getOrNull() ?: emptyList()
            
            // 9. Calculate ETA
            val etaResult = tripRepository.calculateETA(
                origin = Location(
                    latitude = request.pickupLocation.latitude,
                    longitude = request.pickupLocation.longitude
                ),
                destination = Location(
                    latitude = request.dropoffLocation.latitude,
                    longitude = request.dropoffLocation.longitude
                ),
                vehicleType = request.vehicleType
            )
            
            val eta = etaResult.getOrNull()
            
            Result.success(
                CreateTripResponse(
                    trip = trip.copy(fareBreakdown = fareBreakdown),
                    availableDrivers = requestedDrivers,
                    estimatedFare = fareBreakdown,
                    estimatedArrival = eta,
                    matchingTimeSeconds = 5 // Target: <5 seconds
                )
            )
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun validateTripRequest(request: CreateTripRequest): ValidationResult {
        // Validate pickup location
        if (!isValidSanJuanLocation(request.pickupLocation)) {
            return ValidationResult(false, "Pickup location must be within San Juan service area")
        }
        
        // Validate dropoff location
        if (!isValidSanJuanLocation(request.dropoffLocation)) {
            return ValidationResult(false, "Dropoff location must be within San Juan service area")
        }
        
        // Validate distance (minimum and maximum)
        val distance = calculateDistance(request.pickupLocation, request.dropoffLocation)
        if (distance < 0.5) {
            return ValidationResult(false, "Trip distance too short (minimum 500m)")
        }
        if (distance > 50.0) {
            return ValidationResult(false, "Trip distance too long (maximum 50km)")
        }
        
        // Validate pickup != dropoff
        if (request.pickupLocation.latitude == request.dropoffLocation.latitude &&
            request.pickupLocation.longitude == request.dropoffLocation.longitude) {
            return ValidationResult(false, "Pickup and dropoff locations cannot be the same")
        }
        
        return ValidationResult(true, null)
    }
    
    private fun isValidSanJuanLocation(location: TripLocation): Boolean {
        // San Juan approximate boundaries
        val sanJuanBounds = SanJuanBounds(
            northLat = -31.4,
            southLat = -31.8,
            eastLng = -68.3,
            westLng = -68.8
        )
        
        return location.latitude >= sanJuanBounds.southLat &&
                location.latitude <= sanJuanBounds.northLat &&
                location.longitude >= sanJuanBounds.westLng &&
                location.longitude <= sanJuanBounds.eastLng
    }
    
    private fun calculateDistance(location1: TripLocation, location2: TripLocation): Double {
        // Haversine formula for distance calculation
        val earthRadius = 6371.0 // km
        
        val lat1Rad = Math.toRadians(location1.latitude)
        val lat2Rad = Math.toRadians(location2.latitude)
        val deltaLat = Math.toRadians(location2.latitude - location1.latitude)
        val deltaLng = Math.toRadians(location2.longitude - location1.longitude)
        
        val a = kotlin.math.sin(deltaLat / 2) * kotlin.math.sin(deltaLat / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(deltaLng / 2) * kotlin.math.sin(deltaLng / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return earthRadius * c
    }
    
    private fun canUserAffordTrip(user: User, fare: FareBreakdown): Boolean {
        // For MVP, allow all trips (payment validation will be in Phase 2)
        // TODO: Implement payment method validation
        return true
    }
}

data class CreateTripRequest(
    val pickupLocation: TripLocation,
    val dropoffLocation: TripLocation,
    val vehicleType: VehicleType = VehicleType.STANDARD,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val specialRequests: List<String> = emptyList(),
    val demandLevel: DemandLevel = DemandLevel.NORMAL,
    val scheduledTime: Long? = null // For future scheduled trips
)

data class CreateTripResponse(
    val trip: Trip,
    val availableDrivers: List<Driver>,
    val estimatedFare: FareBreakdown,
    val estimatedArrival: ETAResult?,
    val matchingTimeSeconds: Int
)

private data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)

private data class SanJuanBounds(
    val northLat: Double,
    val southLat: Double,
    val eastLng: Double,
    val westLng: Double
)