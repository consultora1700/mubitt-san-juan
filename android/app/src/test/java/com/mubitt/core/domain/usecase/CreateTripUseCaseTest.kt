package com.mubitt.core.domain.usecase

import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.Trip
import com.mubitt.core.domain.model.TripStatus
import com.mubitt.core.domain.model.VehicleType
import com.mubitt.core.domain.repository.TripRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests para CreateTripUseCase
 * Tests críticos para funcionalidad core de Mubitt
 */
class CreateTripUseCaseTest {
    
    private lateinit var tripRepository: TripRepository
    private lateinit var createTripUseCase: CreateTripUseCase
    
    @Before
    fun setup() {
        tripRepository = mockk()
        createTripUseCase = CreateTripUseCase(tripRepository)
    }
    
    @Test
    fun `when valid locations in San Juan, should create trip successfully`() = runTest {
        // Given
        val pickupLocation = Location(
            latitude = -31.5375,
            longitude = -68.5364,
            address = "Hospital Rawson, San Juan",
            reference = "Hospital público principal"
        )
        val dropoffLocation = Location(
            latitude = -31.5653,
            longitude = -68.5311,
            address = "UNSJ Campus, San Juan",
            reference = "Universidad Nacional de San Juan"
        )
        val expectedTrip = Trip(
            id = "trip_123",
            passengerId = "user_123",
            driverId = null,
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation,
            status = TripStatus.REQUESTED,
            vehicleType = VehicleType.ECONOMY,
            estimatedFare = 850.0,
            actualFare = null,
            distance = 3.2,
            estimatedDuration = 8
        )
        
        coEvery {
            tripRepository.createTrip(
                pickupLocation = pickupLocation,
                dropoffLocation = dropoffLocation,
                vehicleType = VehicleType.ECONOMY,
                paymentMethodId = "cash"
            )
        } returns ApiResult.Success(expectedTrip)
        
        // When
        val result = createTripUseCase(
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation,
            vehicleType = VehicleType.ECONOMY,
            paymentMethodId = "cash"
        )
        
        // Then
        assertTrue(result is ApiResult.Success)
        val trip = (result as ApiResult.Success).data
        assertEquals("trip_123", trip.id)
        assertEquals(TripStatus.REQUESTED, trip.status)
        assertEquals(VehicleType.ECONOMY, trip.vehicleType)
        assertEquals(850.0, trip.estimatedFare, 0.01)
    }
    
    @Test
    fun `when pickup location outside San Juan, should return validation error`() = runTest {
        // Given
        val pickupOutsideSanJuan = Location(
            latitude = -34.6037, // Buenos Aires
            longitude = -58.3816,
            address = "Buenos Aires, Argentina"
        )
        val dropoffInSanJuan = Location(
            latitude = -31.5375,
            longitude = -68.5364,
            address = "Centro, San Juan"
        )
        
        // When
        val result = createTripUseCase(
            pickupLocation = pickupOutsideSanJuan,
            dropoffLocation = dropoffInSanJuan,
            vehicleType = VehicleType.ECONOMY,
            paymentMethodId = "cash"
        )
        
        // Then
        assertTrue(result is ApiResult.Error)
        val error = (result as ApiResult.Error).message
        assertTrue(error.contains("San Juan"))
    }
    
    @Test
    fun `when dropoff location outside San Juan, should return validation error`() = runTest {
        // Given
        val pickupInSanJuan = Location(
            latitude = -31.5375,
            longitude = -68.5364,
            address = "Centro, San Juan"
        )
        val dropoffOutsideSanJuan = Location(
            latitude = -32.8895, // Mendoza
            longitude = -68.8458,
            address = "Mendoza, Argentina"
        )
        
        // When
        val result = createTripUseCase(
            pickupLocation = pickupInSanJuan,
            dropoffLocation = dropoffOutsideSanJuan,
            vehicleType = VehicleType.ECONOMY,
            paymentMethodId = "cash"
        )
        
        // Then
        assertTrue(result is ApiResult.Error)
        val error = (result as ApiResult.Error).message
        assertTrue(error.contains("San Juan"))
    }
    
    @Test
    fun `when same pickup and dropoff location, should return validation error`() = runTest {
        // Given
        val sameLocation = Location(
            latitude = -31.5375,
            longitude = -68.5364,
            address = "Centro, San Juan"
        )
        
        // When
        val result = createTripUseCase(
            pickupLocation = sameLocation,
            dropoffLocation = sameLocation,
            vehicleType = VehicleType.ECONOMY,
            paymentMethodId = "cash"
        )
        
        // Then
        assertTrue(result is ApiResult.Error)
        val error = (result as ApiResult.Error).message
        assertTrue(error.contains("diferentes"))
    }
    
    @Test
    fun `when distance too short, should return validation error`() = runTest {
        // Given - Ubicaciones muy cercanas (menos de 500m)
        val pickup = Location(
            latitude = -31.5375,
            longitude = -68.5364,
            address = "Plaza 25 de Mayo"
        )
        val dropoff = Location(
            latitude = -31.5378, // Solo 300m de diferencia
            longitude = -68.5368,
            address = "Casa de Gobierno"
        )
        
        // When
        val result = createTripUseCase(
            pickupLocation = pickup,
            dropoffLocation = dropoff,
            vehicleType = VehicleType.ECONOMY,
            paymentMethodId = "cash"
        )
        
        // Then
        assertTrue(result is ApiResult.Error)
        val error = (result as ApiResult.Error).message
        assertTrue(error.contains("distancia mínima"))
    }
    
    @Test
    fun `when repository returns error, should propagate error`() = runTest {
        // Given
        val pickupLocation = Location(-31.5375, -68.5364, "Centro, San Juan")
        val dropoffLocation = Location(-31.5653, -68.5311, "UNSJ, San Juan")
        
        coEvery {
            tripRepository.createTrip(any(), any(), any(), any())
        } returns ApiResult.Error("Network error", 500)
        
        // When
        val result = createTripUseCase(
            pickupLocation = pickupLocation,
            dropoffLocation = dropoffLocation,
            vehicleType = VehicleType.ECONOMY,
            paymentMethodId = "cash"
        )
        
        // Then
        assertTrue(result is ApiResult.Error)
        val error = (result as ApiResult.Error)
        assertEquals("Network error", error.message)
        assertEquals(500, error.code)
    }
}