package com.mubitt.core.domain.repository

import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.domain.model.*

/**
 * Trip Repository Interface - MVP Version
 * 
 * Clean Architecture - Domain Layer
 * Standards: Simplified for MVP, focused on core trip functionality
 */

interface TripRepository {
    
    // Trip lifecycle management - MVP Core
    suspend fun createTrip(
        pickupLocation: Location,
        dropoffLocation: Location,
        vehicleType: VehicleType,
        paymentMethodId: String
    ): ApiResult<Trip>
    
    suspend fun cancelTrip(tripId: String, reason: String): ApiResult<Unit>
    suspend fun getTripById(tripId: String): ApiResult<Trip>
    suspend fun getActiveTrip(): ApiResult<Trip?>
    
    // Trip queries and history - MVP Core
    suspend fun getTripHistory(page: Int, limit: Int): ApiResult<List<Trip>>
    suspend fun estimateFare(
        pickupLocation: Location,
        dropoffLocation: Location,
        vehicleType: VehicleType
    ): ApiResult<Double>
}