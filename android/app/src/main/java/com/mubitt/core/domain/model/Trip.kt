package com.mubitt.core.domain.model

/**
 * Trip Domain Model
 * 
 * Clean Architecture - Domain Layer Entity
 * Standards: Complete trip lifecycle management
 * San Juan specific: Local zones, fare calculation, references
 */

data class Trip(
    val id: String,
    val passengerId: String,
    val driverId: String? = null,
    val status: TripStatus = TripStatus.REQUESTED,
    
    // Location information
    val pickupLocation: TripLocation,
    val dropoffLocation: TripLocation,
    val actualPickupLocation: TripLocation? = null,
    val actualDropoffLocation: TripLocation? = null,
    
    // Trip details
    val vehicleType: VehicleType = VehicleType.STANDARD,
    val estimatedDistance: Double = 0.0, // kilometers
    val actualDistance: Double? = null,
    val estimatedDuration: Int = 0, // minutes
    val actualDuration: Int? = null,
    
    // Fare information
    val fareBreakdown: FareBreakdown? = null,
    val finalFare: Double? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    
    // Trip timeline
    val requestedAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val pickedUpAt: Long? = null,
    val completedAt: Long? = null,
    val cancelledAt: Long? = null,
    
    // Ratings and feedback
    val passengerRating: Int? = null,
    val driverRating: Int? = null,
    val passengerFeedback: String? = null,
    val driverFeedback: String? = null,
    
    // San Juan specific
    val sanJuanZones: TripZones? = null,
    val localReferences: List<String> = emptyList(),
    val specialRequests: List<String> = emptyList(),
    
    // Real-time tracking
    val route: List<Location> = emptyList(),
    val currentDriverLocation: Location? = null,
    val estimatedArrival: Long? = null,
    
    // Additional info
    val notes: String? = null,
    val emergencyContactNotified: Boolean = false,
    val tripSharedWith: List<String> = emptyList()
) {
    
    val isActive: Boolean
        get() = status in listOf(TripStatus.ACCEPTED, TripStatus.DRIVER_ARRIVING, 
                                TripStatus.DRIVER_ARRIVED, TripStatus.IN_PROGRESS)
    
    val isCompleted: Boolean
        get() = status == TripStatus.COMPLETED
    
    val isCancelled: Boolean
        get() = status in listOf(TripStatus.CANCELLED_BY_PASSENGER, 
                               TripStatus.CANCELLED_BY_DRIVER, TripStatus.CANCELLED_BY_SYSTEM)
    
    val canBeCancelled: Boolean
        get() = status in listOf(TripStatus.REQUESTED, TripStatus.ACCEPTED, TripStatus.DRIVER_ARRIVING)
    
    val totalDuration: Int?
        get() = if (completedAt != null && pickedUpAt != null) {
            ((completedAt - pickedUpAt) / 60000).toInt() // minutes
        } else null
    
    val waitingTime: Int?
        get() = if (pickedUpAt != null && acceptedAt != null) {
            ((pickedUpAt - acceptedAt) / 60000).toInt() // minutes
        } else null
}

enum class TripStatus {
    REQUESTED,              // Trip requested by passenger
    ACCEPTED,               // Driver accepted the trip
    DRIVER_ARRIVING,        // Driver is on the way to pickup
    DRIVER_ARRIVED,         // Driver arrived at pickup location
    IN_PROGRESS,            // Trip is in progress
    COMPLETED,              // Trip completed successfully
    CANCELLED_BY_PASSENGER, // Cancelled by passenger
    CANCELLED_BY_DRIVER,    // Cancelled by driver
    CANCELLED_BY_SYSTEM,    // Cancelled by system (timeout, etc.)
    FAILED                  // Trip failed due to technical issues
}

data class TripLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val shortAddress: String? = null,
    val sanJuanReference: String? = null, // e.g., "Cerca del Hospital Rawson"
    val zone: String? = null,             // e.g., "Centro", "Desamparados"
    val landmark: String? = null,         // e.g., "Shopping del Sol"
    val instructions: String? = null      // Special pickup/dropoff instructions
)

data class TripZones(
    val pickupZone: SanJuanZone,
    val dropoffZone: SanJuanZone,
    val crossesCenter: Boolean = false,
    val usesHighway: Boolean = false
)

enum class SanJuanZone {
    CENTRO,
    DESAMPARADOS,
    RIVADAVIA,
    CHIMBAS,
    POCITO,
    RAWSON,
    SANTA_LUCIA,
    SUBURBAN
}

data class FareBreakdown(
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val surgeFare: Double = 0.0,
    val discount: Double = 0.0,
    val subtotal: Double,
    val total: Double,
    val surgeMultiplier: Double = 1.0,
    val discountReason: String? = null,
    val currency: String = "ARS"
) {
    val effectiveSurge: Double
        get() = if (surgeFare > 0) surgeMultiplier else 1.0
        
    val hasDiscount: Boolean
        get() = discount > 0
        
    val hasSurge: Boolean
        get() = surgeFare > 0
}

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    DISPUTED
}