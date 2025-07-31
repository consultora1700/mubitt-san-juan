package com.mubitt.core.data.repository

import com.mubitt.core.data.remote.api.TripApiService
import com.mubitt.core.data.remote.dto.*
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.TripStatus
import com.mubitt.core.domain.model.VehicleType
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.Response

/**
 * Integration tests para TripRepositoryImpl
 * Tests críticos para comunicación con APIs Mubitt
 */
class TripRepositoryImplTest {
    
    private lateinit var tripApiService: TripApiService
    private lateinit var tripRepository: TripRepositoryImpl
    
    @Before
    fun setup() {
        tripApiService = mockk()
        tripRepository = TripRepositoryImpl(tripApiService)
    }
    
    @Test
    fun `when createTrip API call succeeds, should return success with trip`() = runTest {
        // Given
        val pickupLocation = Location(-31.5375, -68.5364, "Hospital Rawson")
        val dropoffLocation = Location(-31.5653, -68.5311, "UNSJ Campus")
        val vehicleType = VehicleType.ECONOMY
        val paymentMethodId = "cash"
        
        val tripDto = TripDto(
            id = "trip_123",
            passengerId = "user_123",
            driverId = null,
            pickupLocation = LocationDto(-31.5375, -68.5364, "Hospital Rawson", null),
            dropoffLocation = LocationDto(-31.5653, -68.5311, "UNSJ Campus", null),
            status = "REQUESTED",
            vehicleType = "ECONOMY",
            estimatedFare = 850.0,
            actualFare = null,
            distance = 3.2,
            estimatedDuration = 8,
            actualDuration = null,
            scheduledTime = null,
            createdAt = "2025-07-28T10:00:00Z",
            startedAt = null,
            completedAt = null,
            cancelledAt = null,
            rating = null,
            feedback = null
        )
        
        val apiResponse = MubittApiResponse(
            success = true,
            data = tripDto,
            message = null,
            errorCode = null
        )
        
        coEvery {
            tripApiService.createTrip(any())
        } returns Response.success(apiResponse)
        
        // When
        val result = tripRepository.createTrip(
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation,
            vehicleType = vehicleType,
            paymentMethodId = paymentMethodId
        )
        
        // Then
        assertTrue(result is ApiResult.Success)
        val trip = (result as ApiResult.Success).data
        assertEquals("trip_123", trip.id)
        assertEquals("user_123", trip.passengerId)
        assertEquals(TripStatus.REQUESTED, trip.status)
        assertEquals(VehicleType.ECONOMY, trip.vehicleType)
        assertEquals(850.0, trip.estimatedFare, 0.01)
        assertEquals(3.2, trip.distance, 0.01)
        assertEquals(8, trip.estimatedDuration)
    }
    
    @Test
    fun `when createTrip API call fails with 400, should return error`() = runTest {
        // Given
        val pickupLocation = Location(-31.5375, -68.5364, "Hospital Rawson")
        val dropoffLocation = Location(-31.5653, -68.5311, "UNSJ Campus")
        
        coEvery {
            tripApiService.createTrip(any())
        } returns Response.error(400, "Bad Request".toResponseBody())
        
        // When
        val result = tripRepository.createTrip(
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation,
            vehicleType = VehicleType.ECONOMY,
            paymentMethodId = "cash"
        )
        
        // Then
        assertTrue(result is ApiResult.Error)
        val error = (result as ApiResult.Error)
        assertEquals(400, error.code)
        assertTrue(error.message.contains("Solicitud inválida"))
    }
    
    @Test
    fun `when estimateFare API call succeeds, should return fare amount`() = runTest {
        // Given
        val pickupLocation = Location(-31.5375, -68.5364, "Hospital Rawson")
        val dropoffLocation = Location(-31.5653, -68.5311, "UNSJ Campus")
        
        val fareEstimate = FareEstimateDto(
            baseFare = 300.0,
            distanceFare = 256.0, // 3.2km * 80 ARS/km
            timeFare = 120.0, // 8 min * 15 ARS/min
            surgeFactor = 1.0,
            totalFare = 676.0,
            currency = "ARS"
        )
        
        val apiResponse = MubittApiResponse(
            success = true,
            data = fareEstimate,
            message = null,
            errorCode = null
        )
        
        coEvery {
            tripApiService.estimateFare(any())
        } returns Response.success(apiResponse)
        
        // When
        val result = tripRepository.estimateFare(
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation,
            vehicleType = VehicleType.ECONOMY
        )
        
        // Then
        assertTrue(result is ApiResult.Success)
        val fare = (result as ApiResult.Success).data
        assertEquals(676.0, fare, 0.01)
    }
    
    @Test
    fun `when getTripById API call succeeds, should return trip details`() = runTest {
        // Given
        val tripId = "trip_123"
        val tripDto = TripDto(
            id = tripId,
            passengerId = "user_123",
            driverId = "driver_456",
            pickupLocation = LocationDto(-31.5375, -68.5364, "Hospital Rawson", null),
            dropoffLocation = LocationDto(-31.5653, -68.5311, "UNSJ Campus", null),
            status = "IN_PROGRESS",
            vehicleType = "ECONOMY",
            estimatedFare = 850.0,
            actualFare = null,
            distance = 3.2,
            estimatedDuration = 8,
            actualDuration = null,
            scheduledTime = null,
            createdAt = "2025-07-28T10:00:00Z",
            startedAt = "2025-07-28T10:05:00Z",
            completedAt = null,
            cancelledAt = null,
            rating = null,
            feedback = null
        )
        
        val apiResponse = MubittApiResponse(
            success = true,
            data = tripDto,
            message = null,
            errorCode = null
        )
        
        coEvery {
            tripApiService.getTripDetails(tripId)
        } returns Response.success(apiResponse)
        
        // When
        val result = tripRepository.getTripById(tripId)
        
        // Then
        assertTrue(result is ApiResult.Success)
        val trip = (result as ApiResult.Success).data
        assertEquals(tripId, trip.id)
        assertEquals("driver_456", trip.driverId)
        assertEquals(TripStatus.IN_PROGRESS, trip.status)
    }
    
    @Test
    fun `when getTripById API call fails with 404, should return not found error`() = runTest {
        // Given
        val tripId = "nonexistent_trip"
        
        coEvery {
            tripApiService.getTripDetails(tripId)
        } returns Response.error(404, "Not Found".toResponseBody())
        
        // When
        val result = tripRepository.getTripById(tripId)
        
        // Then
        assertTrue(result is ApiResult.Error)
        val error = (result as ApiResult.Error)
        assertEquals(404, error.code)
        assertTrue(error.message.contains("não encontrado") || error.message.contains("not found"))
    }
    
    @Test
    fun `when cancelTrip API call succeeds, should return success`() = runTest {
        // Given
        val tripId = "trip_123"
        val reason = "Usuario canceló el viaje"
        
        val apiResponse = MubittApiResponse(
            success = true,
            data = Unit,
            message = "Viaje cancelado exitosamente",
            errorCode = null
        )
        
        coEvery {
            tripApiService.cancelTrip(tripId, any())
        } returns Response.success(apiResponse)
        
        // When
        val result = tripRepository.cancelTrip(tripId, reason)
        
        // Then
        assertTrue(result is ApiResult.Success)
    }
    
    @Test
    fun `when getActiveTrip API call succeeds with no active trip, should return null`() = runTest {
        // Given
        val apiResponse = MubittApiResponse(
            success = true,
            data = null,
            message = "No hay viajes activos",
            errorCode = null
        )
        
        coEvery {
            tripApiService.getActiveTrip()
        } returns Response.success(apiResponse)
        
        // When
        val result = tripRepository.getActiveTrip()
        
        // Then
        assertTrue(result is ApiResult.Success)
        val activeTrip = (result as ApiResult.Success).data
        assertNull(activeTrip)
    }
    
    @Test
    fun `when getTripHistory API call succeeds, should return list of trips`() = runTest {
        // Given
        val page = 1
        val limit = 10
        
        val tripsDto = listOf(
            TripDto(
                id = "trip_1",
                passengerId = "user_123",
                driverId = "driver_1",
                pickupLocation = LocationDto(-31.5375, -68.5364, "Location A", null),
                dropoffLocation = LocationDto(-31.5653, -68.5311, "Location B", null),
                status = "COMPLETED",
                vehicleType = "ECONOMY",
                estimatedFare = 500.0,
                actualFare = 520.0,
                distance = 2.1,
                estimatedDuration = 6,
                actualDuration = 7,
                scheduledTime = null,
                createdAt = "2025-07-27T15:00:00Z",
                startedAt = "2025-07-27T15:05:00Z",
                completedAt = "2025-07-27T15:12:00Z",
                cancelledAt = null,
                rating = 5,
                feedback = "Excelente viaje"
            ),
            TripDto(
                id = "trip_2",
                passengerId = "user_123",
                driverId = "driver_2",
                pickupLocation = LocationDto(-31.5400, -68.5300, "Location C", null),
                dropoffLocation = LocationDto(-31.5600, -68.5250, "Location D", null),
                status = "COMPLETED",
                vehicleType = "COMFORT",
                estimatedFare = 750.0,
                actualFare = 740.0,
                distance = 3.5,
                estimatedDuration = 10,
                actualDuration = 9,
                scheduledTime = null,
                createdAt = "2025-07-26T18:30:00Z",
                startedAt = "2025-07-26T18:35:00Z",
                completedAt = "2025-07-26T18:44:00Z",
                cancelledAt = null,
                rating = 4,
                feedback = "Buen servicio"
            )
        )
        
        val apiResponse = MubittApiResponse(
            success = true,
            data = tripsDto,
            message = null,
            errorCode = null
        )
        
        coEvery {
            tripApiService.getTripHistory(page, limit, null)
        } returns Response.success(apiResponse)
        
        // When
        val result = tripRepository.getTripHistory(page, limit)
        
        // Then
        assertTrue(result is ApiResult.Success)
        val trips = (result as ApiResult.Success).data
        assertEquals(2, trips.size)
        assertEquals("trip_1", trips[0].id)
        assertEquals("trip_2", trips[1].id)
        assertEquals(TripStatus.COMPLETED, trips[0].status)
        assertEquals(VehicleType.COMFORT, trips[1].vehicleType)
    }
}