package com.mubitt.features.maps

import com.mubitt.core.data.location.LocationService
import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.domain.model.Location
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests para MapViewModel
 * Tests críticos para funcionalidad de mapas en Mubitt
 */
@ExperimentalCoroutinesApi
class MapViewModelTest {
    
    private lateinit var locationService: LocationService
    private lateinit var mapViewModel: MapViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        locationService = mockk()
        mapViewModel = MapViewModel(locationService)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `when getCurrentLocation succeeds, should update current location`() = runTest {
        // Given
        val expectedLocation = Location(
            latitude = -31.5375,
            longitude = -68.5364,
            address = "Centro, San Juan",
            reference = "Plaza 25 de Mayo"
        )
        
        coEvery { 
            locationService.getCurrentLocation() 
        } returns ApiResult.Success(expectedLocation)
        
        // When
        mapViewModel.getCurrentLocation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = mapViewModel.uiState.value
        assertEquals(expectedLocation, uiState.currentLocation)
        assertFalse(uiState.isLoadingLocation)
        assertNull(uiState.locationError)
    }
    
    @Test
    fun `when getCurrentLocation fails, should show error message`() = runTest {
        // Given
        val errorMessage = "No se pudo obtener la ubicación"
        
        coEvery { 
            locationService.getCurrentLocation() 
        } returns ApiResult.Error(errorMessage)
        
        // When
        mapViewModel.getCurrentLocation()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = mapViewModel.uiState.value
        assertNull(uiState.currentLocation)
        assertFalse(uiState.isLoadingLocation)
        assertEquals(errorMessage, uiState.locationError)
    }
    
    @Test
    fun `when searching locations with valid query, should return results`() = runTest {
        // Given
        val query = "hospital rawson"
        val expectedResults = listOf(
            Location(
                latitude = -31.5375,
                longitude = -68.5364,
                address = "Hospital Rawson, San Juan",
                reference = "Hospital público principal"
            ),
            Location(
                latitude = -31.5380,
                longitude = -68.5370,
                address = "Clínica Rawson, San Juan",
                reference = "Clínica privada"
            )
        )
        
        coEvery { 
            locationService.searchLocations(query) 
        } returns ApiResult.Success(expectedResults)
        
        // When
        mapViewModel.searchLocations(query)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = mapViewModel.uiState.value
        assertEquals(expectedResults, uiState.searchResults)
        assertFalse(uiState.isSearching)
        assertNull(uiState.searchError)
        assertTrue(uiState.hasSearchResults)
    }
    
    @Test
    fun `when searching with empty query, should clear results`() = runTest {
        // Given
        val emptyQuery = ""
        
        // When
        mapViewModel.searchLocations(emptyQuery)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = mapViewModel.uiState.value
        assertTrue(uiState.searchResults.isEmpty())
        assertFalse(uiState.hasSearchResults)
    }
    
    @Test
    fun `when searching with blank query, should clear results`() = runTest {
        // Given
        val blankQuery = "   "
        
        // When
        mapViewModel.searchLocations(blankQuery)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = mapViewModel.uiState.value
        assertTrue(uiState.searchResults.isEmpty())
        assertFalse(uiState.hasSearchResults)
    }
    
    @Test
    fun `when setPickupLocation is called, should update pickup location`() = runTest {
        // Given
        val pickupLocation = Location(
            latitude = -31.5375,
            longitude = -68.5364,
            address = "Hospital Rawson, San Juan"
        )
        
        // When
        mapViewModel.setPickupLocation(pickupLocation)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = mapViewModel.uiState.value
        assertEquals(pickupLocation, uiState.pickupLocation)
    }
    
    @Test
    fun `when setDropoffLocation is called, should update dropoff location`() = runTest {
        // Given
        val dropoffLocation = Location(
            latitude = -31.5653,
            longitude = -68.5311,
            address = "UNSJ Campus, San Juan"
        )
        
        // When
        mapViewModel.setDropoffLocation(dropoffLocation)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = mapViewModel.uiState.value
        assertEquals(dropoffLocation, uiState.dropoffLocation)
    }
    
    @Test
    fun `when both pickup and dropoff are set, should calculate distance`() = runTest {
        // Given
        val pickupLocation = Location(-31.5375, -68.5364, "Hospital Rawson")
        val dropoffLocation = Location(-31.5653, -68.5311, "UNSJ Campus")
        
        coEvery { 
            locationService.calculateDistance(pickupLocation, dropoffLocation) 
        } returns 3.2
        
        // When
        mapViewModel.setPickupLocation(pickupLocation)
        mapViewModel.setDropoffLocation(dropoffLocation)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = mapViewModel.uiState.value
        assertEquals(pickupLocation, uiState.pickupLocation)
        assertEquals(dropoffLocation, uiState.dropoffLocation)
        assertEquals(3.2, uiState.estimatedDistance, 0.01)
        assertTrue(uiState.canRequestTrip)
    }
    
    @Test
    fun `when validateSanJuanLocations with locations outside San Juan, should return false`() = runTest {
        // Given
        val pickupOutsideSanJuan = Location(-34.6037, -58.3816, "Buenos Aires")
        val dropoffInSanJuan = Location(-31.5375, -68.5364, "Centro, San Juan")
        
        coEvery { 
            locationService.isLocationInSanJuan(pickupOutsideSanJuan) 
        } returns false
        coEvery { 
            locationService.isLocationInSanJuan(dropoffInSanJuan) 
        } returns true
        
        mapViewModel.setPickupLocation(pickupOutsideSanJuan)
        mapViewModel.setDropoffLocation(dropoffInSanJuan)
        
        // When
        val isValid = mapViewModel.validateSanJuanLocations()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(isValid)
        val uiState = mapViewModel.uiState.value
        assertEquals("Los viajes deben ser dentro de San Juan", uiState.locationError)
    }
    
    @Test
    fun `when validateSanJuanLocations with both locations in San Juan, should return true`() = runTest {
        // Given
        val pickupInSanJuan = Location(-31.5375, -68.5364, "Centro, San Juan")
        val dropoffInSanJuan = Location(-31.5653, -68.5311, "UNSJ, San Juan")
        
        coEvery { 
            locationService.isLocationInSanJuan(pickupInSanJuan) 
        } returns true
        coEvery { 
            locationService.isLocationInSanJuan(dropoffInSanJuan) 
        } returns true
        
        mapViewModel.setPickupLocation(pickupInSanJuan)
        mapViewModel.setDropoffLocation(dropoffInSanJuan)
        
        // When
        val isValid = mapViewModel.validateSanJuanLocations()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(isValid)
        val uiState = mapViewModel.uiState.value
        assertNull(uiState.locationError)
    }
    
    @Test
    fun `when clearLocations is called, should reset all locations`() = runTest {
        // Given - Set some locations first
        val pickup = Location(-31.5375, -68.5364, "Centro")
        val dropoff = Location(-31.5653, -68.5311, "UNSJ")
        val driver = Location(-31.5400, -68.5350, "Conductor")
        
        mapViewModel.setPickupLocation(pickup)
        mapViewModel.setDropoffLocation(dropoff)
        mapViewModel.updateDriverLocation(driver)
        
        // When
        mapViewModel.clearLocations()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = mapViewModel.uiState.value
        assertNull(uiState.pickupLocation)
        assertNull(uiState.dropoffLocation)
        assertNull(uiState.driverLocation)
        assertNull(uiState.estimatedDistance)
        assertFalse(uiState.canRequestTrip)
    }
}