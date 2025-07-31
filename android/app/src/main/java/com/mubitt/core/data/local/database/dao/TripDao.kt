package com.mubitt.core.data.local.database.dao

import androidx.room.*
import com.mubitt.core.data.local.database.entity.TripEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de viajes
 * Incluye queries optimizadas para historial y viajes activos
 */
@Dao
interface TripDao {
    
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: String): TripEntity?
    
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun observeTripById(tripId: String): Flow<TripEntity?>
    
    @Query("SELECT * FROM trips WHERE passengerId = :userId AND status IN ('REQUESTED', 'ACCEPTED', 'IN_PROGRESS') LIMIT 1")
    suspend fun getActiveTrip(userId: String): TripEntity?
    
    @Query("SELECT * FROM trips WHERE passengerId = :userId AND status IN ('REQUESTED', 'ACCEPTED', 'IN_PROGRESS') LIMIT 1")
    fun observeActiveTrip(userId: String): Flow<TripEntity?>
    
    @Query("SELECT * FROM trips WHERE passengerId = :userId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getTripHistory(userId: String, limit: Int, offset: Int): List<TripEntity>
    
    @Query("SELECT * FROM trips WHERE passengerId = :userId ORDER BY createdAt DESC")
    fun observeTripHistory(userId: String): Flow<List<TripEntity>>
    
    @Query("SELECT * FROM trips WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getTripsByStatus(status: String): List<TripEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)
    
    @Update
    suspend fun updateTrip(trip: TripEntity)
    
    @Query("UPDATE trips SET status = :status, updatedAt = :updatedAt WHERE id = :tripId")
    suspend fun updateTripStatus(tripId: String, status: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE trips SET driverId = :driverId, status = 'ACCEPTED', updatedAt = :updatedAt WHERE id = :tripId")
    suspend fun assignDriver(tripId: String, driverId: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE trips SET actualFare = :actualFare, status = 'COMPLETED', updatedAt = :updatedAt WHERE id = :tripId")
    suspend fun completeTrip(tripId: String, actualFare: Double, updatedAt: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteTrip(trip: TripEntity)
    
    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTripById(tripId: String)
    
    @Query("DELETE FROM trips WHERE passengerId = :userId")
    suspend fun deleteUserTrips(userId: String)
    
    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()
    
    // Queries para estadísticas
    @Query("SELECT COUNT(*) FROM trips WHERE passengerId = :userId AND status = 'COMPLETED'")
    suspend fun getCompletedTripCount(userId: String): Int
    
    @Query("SELECT AVG(actualFare) FROM trips WHERE passengerId = :userId AND status = 'COMPLETED' AND actualFare IS NOT NULL")
    suspend fun getAverageFare(userId: String): Double?
    
    @Query("SELECT SUM(distance) FROM trips WHERE passengerId = :userId AND status = 'COMPLETED'")
    suspend fun getTotalDistance(userId: String): Double?
    
    @Query("SELECT * FROM trips WHERE passengerId = :userId AND status = 'COMPLETED' ORDER BY createdAt DESC LIMIT 5")
    suspend fun getRecentCompletedTrips(userId: String): List<TripEntity>
    
    // Queries para sync offline
    @Query("SELECT * FROM trips WHERE updatedAt > :lastSyncTime")
    suspend fun getTripsModifiedAfter(lastSyncTime: Long): List<TripEntity>
    
    @Query("UPDATE trips SET updatedAt = :syncTime WHERE id IN (:tripIds)")
    suspend fun markTripsAsSynced(tripIds: List<String>, syncTime: Long)
}