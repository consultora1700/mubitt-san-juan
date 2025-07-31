package com.mubitt.core.data.remote.dto

import com.mubitt.core.domain.model.User

/**
 * DTOs para User API endpoints
 * Separados del modelo de dominio para flexibilidad
 */

data class LoginRequest(
    val email: String? = null,
    val phoneNumber: String? = null,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val deviceToken: String? = null
)

data class UpdateProfileRequest(
    val name: String?,
    val email: String?,
    val profilePictureUrl: String?
)

data class VerifyPhoneRequest(
    val phoneNumber: String,
    val verificationCode: String
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val profilePictureUrl: String?,
    val rating: Double,
    val tripCount: Int,
    val isVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class AuthResponse(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

/**
 * Extension functions para convertir DTOs a domain models
 */
fun UserDto.toDomainModel(): User {
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