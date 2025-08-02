package com.mubitt.core.domain.model

/**
 * Driver Domain Model
 * 
 * Clean Architecture - Domain Layer Entity
 * Standards: San Juan specific fields for local knowledge
 * Features: Rating system, experience tracking, vehicle info
 */

data class Driver(
    val id: String,
    val userId: String, // Reference to User entity
    val licenseNumber: String,
    val rating: Double = 5.0,
    val totalTrips: Int = 0,
    val isOnline: Boolean = false,
    val isAvailable: Boolean = false,
    val currentLocation: Location? = null,
    val lastActiveTime: Long,
    val joinedAt: Long,
    
    // San Juan specific fields
    val sanJuanExperienceMonths: Int = 0,
    val sanJuanTripsCompleted: Int = 0,
    val streetKnowledgeRating: Double = 5.0, // Separate rating for local knowledge
    val localExperienceMonths: Int = 0,
    
    // Vehicle information
    val vehicle: Vehicle,
    
    // Driver metrics
    val acceptanceRate: Double = 1.0,
    val cancellationRate: Double = 0.0,
    val averageResponseTime: Int = 30, // seconds
    
    // Verification status
    val isVerified: Boolean = false,
    val documentsVerified: Boolean = false,
    val backgroundCheckPassed: Boolean = false,
    
    // Performance metrics
    val onTimePercentage: Double = 100.0,
    val customerSatisfactionScore: Double = 5.0,
    
    // Work patterns
    val preferredWorkingHours: WorkingHours? = null,
    val preferredAreas: List<String> = emptyList(), // San Juan zones
) {
    val isEligible: Boolean
        get() = isVerified && documentsVerified && backgroundCheckPassed && vehicle.isOperational
        
    val isHighPerforming: Boolean
        get() = rating >= 4.7 && acceptanceRate >= 0.9 && cancellationRate <= 0.05
        
    fun getLocalKnowledgeScore(): Double {
        val timeScore = minOf(sanJuanExperienceMonths / 18.0, 1.0) * 0.5
        val tripsScore = minOf(sanJuanTripsCompleted / 500.0, 1.0) * 0.3
        val streetScore = (streetKnowledgeRating / 5.0) * 0.2
        return (timeScore + tripsScore + streetScore)
    }
    
    fun canAcceptTrip(): Boolean {
        return isOnline && isAvailable && !isInTrip() && isEligible
    }
    
    private fun isInTrip(): Boolean {
        // TODO: Check current trip status
        return false
    }
}

data class Vehicle(
    val id: String,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val licensePlate: String,
    val type: VehicleType = VehicleType.STANDARD,
    val capacity: Int = 4,
    val isOperational: Boolean = true,
    val lastInspection: Long? = null,
    val insurance: VehicleInsurance
) {
    val displayName: String
        get() = "$make $model $year"
        
    val isInspectionValid: Boolean
        get() = lastInspection?.let {
            System.currentTimeMillis() - it < INSPECTION_VALIDITY_PERIOD
        } ?: false
        
    companion object {
        private const val INSPECTION_VALIDITY_PERIOD = 365L * 24 * 60 * 60 * 1000 // 1 year
    }
}

enum class VehicleType {
    ECONOMY,    // Basic vehicles
    STANDARD,   // Standard sedans
    COMFORT,    // Comfortable/newer vehicles
    XL,         // Large vehicles (6+ seats)
    PREMIUM,    // Luxury vehicles
    ACCESSIBLE  // Wheelchair accessible
}

data class VehicleInsurance(
    val provider: String,
    val policyNumber: String,
    val expiryDate: Long,
    val isValid: Boolean = true
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiryDate
}

data class WorkingHours(
    val monday: DaySchedule? = null,
    val tuesday: DaySchedule? = null,
    val wednesday: DaySchedule? = null,
    val thursday: DaySchedule? = null,
    val friday: DaySchedule? = null,
    val saturday: DaySchedule? = null,
    val sunday: DaySchedule? = null
)

data class DaySchedule(
    val startTime: String, // "HH:mm" format
    val endTime: String,   // "HH:mm" format
    val isAvailable: Boolean = true
)

