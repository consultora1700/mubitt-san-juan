package com.mubitt.core.data.remote.api

import com.mubitt.core.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service para operaciones de conductores
 * Para app de conductores y funcionalidades de tracking
 */
interface DriverApiService {
    
    @POST("drivers/register")
    suspend fun registerDriver(
        @Body request: DriverRegistrationRequest
    ): Response<MubittApiResponse<DriverDto>>
    
    @GET("drivers/profile")
    suspend fun getDriverProfile(): Response<MubittApiResponse<DriverDto>>
    
    @PUT("drivers/status")
    suspend fun updateDriverStatus(
        @Body request: DriverStatusUpdate
    ): Response<MubittApiResponse<Unit>>
    
    @POST("drivers/location")
    suspend fun updateLocation(
        @Body request: DriverLocationUpdate
    ): Response<MubittApiResponse<Unit>>
    
    @GET("drivers/nearby")
    suspend fun getNearbyDrivers(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusKm: Double = 5.0,
        @Query("vehicleType") vehicleType: String? = null
    ): Response<MubittApiResponse<List<DriverDto>>>
    
    @GET("drivers/earnings")
    suspend fun getEarnings(
        @Query("period") period: String = "today" // "today", "week", "month"
    ): Response<MubittApiResponse<DriverEarningsDto>>
    
    @GET("drivers/trip-requests")
    suspend fun getPendingTripRequests(): Response<MubittApiResponse<List<TripRequestDto>>>
    
    @POST("drivers/trip-requests/{requestId}/accept")
    suspend fun acceptTripRequest(
        @Path("requestId") requestId: String
    ): Response<MubittApiResponse<TripDto>>
    
    @POST("drivers/trip-requests/{requestId}/decline")
    suspend fun declineTripRequest(
        @Path("requestId") requestId: String,
        @Body request: DeclineTripRequest
    ): Response<MubittApiResponse<Unit>>
    
    @POST("drivers/trips/{tripId}/start")
    suspend fun startTrip(
        @Path("tripId") tripId: String
    ): Response<MubittApiResponse<Unit>>
    
    @POST("drivers/trips/{tripId}/complete")
    suspend fun completeTrip(
        @Path("tripId") tripId: String,
        @Body request: CompleteTripRequest
    ): Response<MubittApiResponse<Unit>>
    
    @GET("drivers/documents/status")
    suspend fun getDocumentVerificationStatus(): Response<MubittApiResponse<DocumentStatusDto>>
    
    @POST("drivers/documents/upload")
    @Multipart
    suspend fun uploadDocument(
        @Part("document") document: okhttp3.MultipartBody.Part,
        @Part("type") type: okhttp3.RequestBody // "license", "registration", "insurance"
    ): Response<MubittApiResponse<String>>
    
    // Endpoints específicos para conductores San Juan
    @GET("drivers/san-juan/hot-spots")
    suspend fun getHotSpots(): Response<MubittApiResponse<List<HotSpotDto>>>
    
    @GET("drivers/san-juan/events")
    suspend fun getDriverEvents(): Response<MubittApiResponse<List<DriverEventDto>>>
    
    @POST("drivers/san-juan/specialty")
    suspend fun updateSpecialties(
        @Body request: UpdateSpecialtiesRequest
    ): Response<MubittApiResponse<Unit>>
}

data class TripRequestDto(
    val id: String,
    val passengerId: String,
    val passengerName: String,
    val passengerRating: Double,
    val pickupLocation: LocationDto,
    val dropoffLocation: LocationDto,
    val estimatedFare: Double,
    val distance: Double,
    val estimatedDuration: Int,
    val requestedAt: String,
    val expiresAt: String
)

data class DeclineTripRequest(
    val reason: String // "too_far", "going_offline", "passenger_rating", "other"
)

data class CompleteTripRequest(
    val actualFare: Double,
    val actualDistance: Double,
    val actualDuration: Int,
    val endLocation: LocationDto
)

data class DocumentStatusDto(
    val license: DocumentVerificationDto,
    val registration: DocumentVerificationDto,
    val insurance: DocumentVerificationDto,
    val backgroundCheck: DocumentVerificationDto?,
    val overall: String // "pending", "approved", "rejected"
)

data class DocumentVerificationDto(
    val status: String, // "pending", "approved", "rejected"
    val uploadedAt: String?,
    val reviewedAt: String?,
    val notes: String?
)

data class HotSpotDto(
    val id: String,
    val name: String,
    val location: LocationDto,
    val demandLevel: String, // "low", "medium", "high"
    val avgWaitTime: Int, // Minutes
    val suggestedTimes: List<String>,
    val bonus: Double? = null // Bonus por ARS por viaje en esta zona
)

data class DriverEventDto(
    val id: String,
    val title: String,
    val description: String,
    val location: LocationDto?,
    val startTime: String,
    val endTime: String,
    val incentive: IncentiveDto?
)

data class IncentiveDto(
    val type: String, // "per_trip", "hourly", "completion"
    val amount: Double,
    val description: String
)

data class UpdateSpecialtiesRequest(
    val specialties: List<String> // "eventos_deportivos", "aeropuerto", "turismo", "zona_universitaria"
)