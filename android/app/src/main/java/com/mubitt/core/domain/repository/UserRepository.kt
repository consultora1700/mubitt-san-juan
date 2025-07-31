package com.mubitt.core.domain.repository

import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * User Repository Interface
 * 
 * Clean Architecture - Domain Layer
 * Standards: Repository pattern for data abstraction
 * Implementation: To be provided by data layer
 */

interface UserRepository {
    
    // Authentication - Core methods for MVP
    suspend fun login(email: String, password: String): ApiResult<User>
    suspend fun loginWithPhone(phoneNumber: String, password: String): ApiResult<User>
    suspend fun register(
        name: String,
        email: String,
        phoneNumber: String,
        password: String
    ): ApiResult<User>
    suspend fun logout(): ApiResult<Unit>
    suspend fun deleteAccount(userId: String): Result<Unit>
    
    // Profile management - Core methods for MVP
    suspend fun getCurrentUser(): ApiResult<User>
    suspend fun updateProfile(
        name: String?,
        email: String?,
        profilePictureUrl: String?
    ): ApiResult<User>
}