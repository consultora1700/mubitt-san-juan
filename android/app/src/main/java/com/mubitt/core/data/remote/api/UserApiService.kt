package com.mubitt.core.data.remote.api

import com.mubitt.core.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service para operaciones de usuarios
 * Endpoints optimizados para experiencia San Juan
 */
interface UserApiService {
    
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<MubittApiResponse<AuthResponse>>
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<MubittApiResponse<AuthResponse>>
    
    @POST("auth/verify-phone")
    suspend fun verifyPhone(
        @Body request: VerifyPhoneRequest
    ): Response<MubittApiResponse<Unit>>
    
    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<MubittApiResponse<AuthResponse>>
    
    @POST("auth/logout")
    suspend fun logout(): Response<MubittApiResponse<Unit>>
    
    @GET("users/profile")
    suspend fun getProfile(): Response<MubittApiResponse<UserDto>>
    
    @PUT("users/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<MubittApiResponse<UserDto>>
    
    @POST("users/upload-avatar")
    @Multipart
    suspend fun uploadAvatar(
        @Part("image") image: okhttp3.MultipartBody.Part
    ): Response<MubittApiResponse<String>> // Returns image URL
    
    @GET("users/trip-history")
    suspend fun getTripHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<MubittApiResponse<List<TripDto>>>
    
    @POST("users/rate-driver")
    suspend fun rateDriver(
        @Body request: RateDriverRequest
    ): Response<MubittApiResponse<Unit>>
    
    @POST("users/report-issue")
    suspend fun reportIssue(
        @Body request: ReportIssueRequest
    ): Response<MubittApiResponse<Unit>>
    
    @GET("users/favorites")
    suspend fun getFavoriteLocations(): Response<MubittApiResponse<List<LocationDto>>>
    
    @POST("users/favorites")
    suspend fun addFavoriteLocation(
        @Body location: LocationDto
    ): Response<MubittApiResponse<Unit>>
    
    @DELETE("users/favorites/{locationId}")
    suspend fun removeFavoriteLocation(
        @Path("locationId") locationId: String
    ): Response<MubittApiResponse<Unit>>
}

data class RateDriverRequest(
    val tripId: String,
    val rating: Int, // 1-5
    val feedback: String?
)

data class ReportIssueRequest(
    val tripId: String?,
    val category: String, // "driver", "app", "payment", "other"
    val description: String,
    val priority: String = "medium" // "low", "medium", "high"
)