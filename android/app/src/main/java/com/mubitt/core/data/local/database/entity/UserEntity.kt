package com.mubitt.core.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mubitt.core.domain.model.User

/**
 * Entity para persistencia local de datos de usuario
 * Optimizada para almacenamiento offline en Mubitt
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val profilePictureUrl: String?,
    val rating: Double,
    val tripCount: Int,
    val isVerified: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Extension functions para conversión entre Entity y Domain Model
 */
fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        rating = rating,
        tripCount = tripCount,
        isVerified = isVerified
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        rating = rating,
        tripCount = tripCount,
        isVerified = isVerified,
        updatedAt = System.currentTimeMillis()
    )
}