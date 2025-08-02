package com.mubitt.core.data.repository

import com.mubitt.core.data.remote.api.TripApiService
import com.mubitt.core.data.remote.dto.*
import com.mubitt.core.data.remote.util.safeApiCall
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.Trip
import com.mubitt.core.domain.model.VehicleType
import com.mubitt.core.domain.repository.TripRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del TripRepository
 * Maneja creación, búsqueda y gestión de viajes
 */
@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripApiService: TripApiService
) : TripRepository {
    
    override suspend fun createTrip(
        pickupLocation: Location,
        dropoffLocation: Location,
        vehicleType: VehicleType,
        paymentMethodId: String
    ): ApiResult<Trip> {
        return safeApiCall {
            tripApiService.createTrip(
                CreateTripRequest(
                    pickupLocation = LocationDto(
                        latitude = pickupLocation.latitude,
                        longitude = pickupLocation.longitude,
                        address = pickupLocation.address,
                        reference = pickupLocation.reference
                    ),
                    dropoffLocation = LocationDto(
                        latitude = dropoffLocation.latitude,
                        longitude = dropoffLocation.longitude,
                        address = dropoffLocation.address,
                        reference = dropoffLocation.reference
                    ),
                    vehicleType = vehicleType.name,
                    paymentMethodId = paymentMethodId
                )
            )
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.toDomainModel())
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }
    
    override suspend fun getTripById(tripId: String): ApiResult<Trip> {
        return safeApiCall {
            tripApiService.getTripDetails(tripId)
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.toDomainModel())
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }
    
    override suspend fun getActiveTrip(): ApiResult<Trip?> {
        return safeApiCall {
            tripApiService.getActiveTrip()
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data?.toDomainModel())
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }
    
    override suspend fun cancelTrip(tripId: String, reason: String): ApiResult<Unit> {
        return safeApiCall {
            tripApiService.cancelTrip(
                tripId,
                CancelTripRequest(reason = reason, category = "user_request")
            )
        }
    }
    
    override suspend fun getTripHistory(page: Int, limit: Int): ApiResult<List<Trip>> {
        return safeApiCall {
            tripApiService.getTripHistory(page, limit)
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(
                    result.data.map { it.toDomainModel() }
                )
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }
    
    override suspend fun estimateFare(
        pickupLocation: Location,
        dropoffLocation: Location,
        vehicleType: VehicleType
    ): ApiResult<Double> {
        return safeApiCall {
            tripApiService.estimateFare(
                CreateTripRequest(
                    pickupLocation = LocationDto(
                        latitude = pickupLocation.latitude,
                        longitude = pickupLocation.longitude,
                        address = pickupLocation.address,
                        reference = pickupLocation.reference
                    ),
                    dropoffLocation = LocationDto(
                        latitude = dropoffLocation.latitude,
                        longitude = dropoffLocation.longitude,
                        address = dropoffLocation.address,
                        reference = dropoffLocation.reference
                    ),
                    vehicleType = vehicleType.name,
                    paymentMethodId = "" // Not needed for estimate
                )
            )
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.totalFare)
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }

    // Métodos específicos para San Juan
    suspend fun getPopularDestinations(): ApiResult<List<Location>> {
        return safeApiCall {
            tripApiService.getPopularDestinations()
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(
                    result.data.map { it.toDomainModel() }
                )
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }
    
    suspend fun getUniversityRoutes(): ApiResult<List<RouteDto>> {
        return safeApiCall {
            tripApiService.getUniversityRoutes()
        }
    }
}