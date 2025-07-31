package com.mubitt.core.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mubitt.core.domain.model.*

/**
 * Entity para persistencia local de viajes
 * Incluye información completa para funcionalidad offline
 */
@Entity(tableName = "trips")
@TypeConverters(TripConverters::class)
data class TripEntity(
    @PrimaryKey
    val id: String,
    val passengerId: String,
    val driverId: String?,
    val pickupLocation: LocationEntity,
    val dropoffLocation: LocationEntity,
    val status: String, // TripStatus as String
    val vehicleType: String, // VehicleType as String
    val estimatedFare: Double,
    val actualFare: Double?,
    val distance: Double,
    val estimatedDuration: Int,
    val scheduledTime: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entity embebida para ubicaciones
 */
data class LocationEntity(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val reference: String?
)

/**
 * Type converters para Room
 */
class TripConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromLocationEntity(location: LocationEntity): String {
        return gson.toJson(location)
    }
    
    @TypeConverter
    fun toLocationEntity(locationString: String): LocationEntity {
        return gson.fromJson(locationString, LocationEntity::class.java)
    }
}

/**
 * Extension functions para conversión
 */
fun TripEntity.toDomainModel(): Trip {
    return Trip(
        id = id,
        passengerId = passengerId,
        driverId = driverId,
        pickupLocation = pickupLocation.toDomainModel(),
        dropoffLocation = dropoffLocation.toDomainModel(),
        status = TripStatus.valueOf(status.uppercase()),
        vehicleType = VehicleType.valueOf(vehicleType.uppercase()),
        estimatedFare = estimatedFare,
        actualFare = actualFare,
        distance = distance,
        estimatedDuration = estimatedDuration,
        scheduledTime = scheduledTime
    )
}

fun Trip.toEntity(): TripEntity {
    return TripEntity(
        id = id,
        passengerId = passengerId,
        driverId = driverId,
        pickupLocation = pickupLocation.toEntity(),
        dropoffLocation = dropoffLocation.toEntity(),
        status = status.name,
        vehicleType = vehicleType.name,
        estimatedFare = estimatedFare,
        actualFare = actualFare,
        distance = distance,
        estimatedDuration = estimatedDuration,
        scheduledTime = scheduledTime,
        updatedAt = System.currentTimeMillis()
    )
}

fun LocationEntity.toDomainModel(): Location {
    return Location(
        latitude = latitude,
        longitude = longitude,
        address = address,
        reference = reference
    )
}

fun Location.toEntity(): LocationEntity {
    return LocationEntity(
        latitude = latitude,
        longitude = longitude,
        address = address,
        reference = reference
    )
}