package com.mubitt.core.domain.model

/**
 * User Domain Model
 * 
 * Clean Architecture - Domain Layer Entity
 * Standards: Immutable, business logic focused
 * San Juan specific: Phone validation for Argentina format
 */

data class User(
    val id: String,
    val email: String,
    val phoneNumber: String,
    val firstName: String,
    val lastName: String,
    val profileImageUrl: String? = null,
    val isVerified: Boolean = false,
    val userType: UserType = UserType.PASSENGER,
    val rating: Double = 5.0,
    val totalTrips: Int = 0,
    val createdAt: Long,
    val lastLoginAt: Long? = null,
    val preferences: UserPreferences = UserPreferences()
) {
    val fullName: String
        get() = "$firstName $lastName"
        
    val isActive: Boolean
        get() = isVerified && lastLoginAt != null
        
    fun isEligibleForStudentDiscount(): Boolean {
        return preferences.isStudent && isVerified
    }
    
    fun canRequestPremiumRide(): Boolean {
        return rating >= 4.5 && totalTrips >= 10
    }
}

enum class UserType {
    PASSENGER,
    DRIVER,
    ADMIN
}

data class UserPreferences(
    val isStudent: Boolean = false,
    val universityId: String? = null, // For UNSJ verification
    val preferredPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val enableNotifications: Boolean = true,
    val enableLocationSharing: Boolean = true,
    val preferredLanguage: String = "es", // Spanish default for San Juan
    val darkModeEnabled: Boolean = false,
    val favoriteLocations: List<SavedLocation> = emptyList(),
    val emergencyContacts: List<EmergencyContact> = emptyList()
)

data class SavedLocation(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val type: LocationType = LocationType.OTHER
)

enum class LocationType {
    HOME,
    WORK,
    SCHOOL, // UNSJ
    OTHER
}

data class EmergencyContact(
    val name: String,
    val phoneNumber: String,
    val relationship: String
)

enum class PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    MERCADO_PAGO,
    WALLET
}