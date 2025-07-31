package com.mubitt.core.data.remote.api

import com.mubitt.core.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service para operaciones de viajes
 * Optimizado para matching rápido en San Juan
 */
interface TripApiService {
    
    @POST("trips/estimate-fare")
    suspend fun estimateFare(
        @Body request: CreateTripRequest
    ): Response<MubittApiResponse<FareEstimateDto>>
    
    @POST("trips/search-drivers")
    suspend fun searchAvailableDrivers(
        @Body request: TripSearchRequest
    ): Response<MubittApiResponse<List<DriverMatchDto>>>
    
    @POST("trips/create")
    suspend fun createTrip(
        @Body request: CreateTripRequest
    ): Response<MubittApiResponse<TripDto>>
    
    @GET("trips/{tripId}")
    suspend fun getTripDetails(
        @Path("tripId") tripId: String
    ): Response<MubittApiResponse<TripDto>>
    
    @POST("trips/{tripId}/cancel")
    suspend fun cancelTrip(
        @Path("tripId") tripId: String,
        @Body request: CancelTripRequest
    ): Response<MubittApiResponse<Unit>>
    
    @GET("trips/active")
    suspend fun getActiveTrip(): Response<MubittApiResponse<TripDto?>>
    
    @GET("trips/history")
    suspend fun getTripHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null
    ): Response<MubittApiResponse<List<TripDto>>>
    
    @POST("trips/{tripId}/rate")
    suspend fun rateTrip(
        @Path("tripId") tripId: String,
        @Body request: RateTripRequest
    ): Response<MubittApiResponse<Unit>>
    
    // Endpoints específicos para San Juan
    @GET("trips/san-juan/popular-destinations")
    suspend fun getPopularDestinations(): Response<MubittApiResponse<List<LocationDto>>>
    
    @GET("trips/san-juan/events")
    suspend fun getCurrentEvents(): Response<MubittApiResponse<List<EventDto>>>
    
    @POST("trips/san-juan/schedule")
    suspend fun scheduleTrip(
        @Body request: ScheduleTripRequest
    ): Response<MubittApiResponse<TripDto>>
    
    @GET("trips/san-juan/university-routes")
    suspend fun getUniversityRoutes(): Response<MubittApiResponse<List<RouteDto>>>
}

data class CancelTripRequest(
    val reason: String,
    val category: String // "user_request", "driver_unavailable", "emergency", "other"
)

data class RateTripRequest(
    val rating: Int, // 1-5
    val feedback: String?,
    val categories: List<String> = emptyList() // "punctual", "clean_car", "safe_driving", etc.
)

data class ScheduleTripRequest(
    val pickupLocation: LocationDto,
    val dropoffLocation: LocationDto,
    val scheduledTime: String, // ISO 8601 format
    val vehicleType: String,
    val notes: String? = null,
    val recurringType: String? = null // "daily", "weekly", null for one-time
)

data class EventDto(
    val id: String,
    val name: String,
    val location: LocationDto,
    val startTime: String,
    val endTime: String,
    val category: String, // "fiesta_sol", "sports", "university", "concert"
    val estimatedAttendance: Int,
    val surgeInfo: SurgeInfoDto?
)

data class SurgeInfoDto(
    val isActive: Boolean,
    val factor: Double, // Máximo 1.5x para Mubitt vs 3x de Uber
    val reason: String
)

data class RouteDto(
    val id: String,
    val name: String,
    val origin: LocationDto,
    val destination: LocationDto,
    val estimatedFare: Double,
    val estimatedDuration: Int,
    val isActive: Boolean,
    val studentDiscount: Double? = null
)