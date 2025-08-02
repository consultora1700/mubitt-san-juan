package com.mubitt.core.domain.model

/**
 * Location Data Model for Mubitt
 * 
 * Represents geographical locations used throughout the app
 * Standards: Optimized for San Juan, Argentina coordinates
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val name: String? = null,
    val reference: String? = null,
    val type: LocationType = LocationType.GENERAL
) {
    
    enum class LocationType {
        GENERAL,
        HOME,
        WORK,
        HOSPITAL,
        UNIVERSITY,
        SHOPPING,
        AIRPORT,
        GOVERNMENT,
        TOURIST_ATTRACTION
    }
    
    companion object {
        // San Juan city center as default
        val SAN_JUAN_CENTER = Location(
            latitude = -31.5375,
            longitude = -68.5364,
            address = "Centro de San Juan",
            name = "Centro",
            type = LocationType.GENERAL
        )
        
        // Common San Juan locations
        val HOSPITAL_RAWSON = Location(
            latitude = -31.5400,
            longitude = -68.5300,
            address = "Hospital Dr. Guillermo Rawson",
            name = "Hospital Rawson",
            type = LocationType.HOSPITAL
        )
        
        val UNSJ = Location(
            latitude = -31.5314,
            longitude = -68.5258,
            address = "Universidad Nacional de San Juan",
            name = "UNSJ",
            type = LocationType.UNIVERSITY
        )
    }
}