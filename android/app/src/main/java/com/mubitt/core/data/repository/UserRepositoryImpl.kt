package com.mubitt.core.data.repository

import com.mubitt.core.data.remote.api.UserApiService
import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.data.remote.dto.LoginRequest
import com.mubitt.core.data.remote.dto.RegisterRequest
import com.mubitt.core.data.remote.dto.toDomainModel
import com.mubitt.core.data.remote.util.safeApiCall
import com.mubitt.core.domain.model.User
import com.mubitt.core.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del UserRepository
 * Maneja autenticación y perfil de usuario con APIs Mubitt
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService
) : UserRepository {
    
    override suspend fun login(email: String, password: String): ApiResult<User> {
        return safeApiCall {
            userApiService.login(
                LoginRequest(email = email, password = password)
            )
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.user.toDomainModel())
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }
    
    override suspend fun loginWithPhone(phoneNumber: String, password: String): ApiResult<User> {
        return safeApiCall {
            userApiService.login(
                LoginRequest(phoneNumber = phoneNumber, password = password)
            )
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.user.toDomainModel())
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }
    
    override suspend fun register(
        name: String,
        email: String,
        phoneNumber: String,
        password: String
    ): ApiResult<User> {
        return safeApiCall {
            userApiService.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    password = password
                )
            )
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.user.toDomainModel())
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }
    
    override suspend fun getCurrentUser(): ApiResult<User> {
        return safeApiCall {
            userApiService.getProfile()
        }.let { result ->
            when (result) {
                is ApiResult.Success -> ApiResult.Success(result.data.toDomainModel())
                is ApiResult.Error -> ApiResult.Error(result.message, result.code)
                is ApiResult.Loading -> ApiResult.Loading()
            }
        }
    }
    
    override suspend fun updateProfile(
        name: String?,
        email: String?,
        profilePictureUrl: String?
    ): ApiResult<User> {
        return safeApiCall {
            userApiService.updateProfile(
                com.mubitt.core.data.remote.dto.UpdateProfileRequest(
                    name = name,
                    email = email,
                    profilePictureUrl = profilePictureUrl
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
    
    override suspend fun logout(): ApiResult<Unit> {
        return safeApiCall {
            userApiService.logout()
        }
    }
}