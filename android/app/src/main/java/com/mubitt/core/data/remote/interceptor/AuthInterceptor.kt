package com.mubitt.core.data.remote.interceptor

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor para manejar autenticación automática
 * Inyecta JWT tokens en todos los requests que lo requieran
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : Interceptor {
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val TOKEN_PREFIX = "Bearer "
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for login/register endpoints
        if (shouldSkipAuth(originalRequest.url.encodedPath)) {
            return chain.proceed(originalRequest)
        }
        
        val accessToken = getAccessToken()
        
        val authenticatedRequest = if (accessToken != null) {
            originalRequest.newBuilder()
                .header(HEADER_AUTHORIZATION, "$TOKEN_PREFIX$accessToken")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(authenticatedRequest)
    }
    
    private fun shouldSkipAuth(path: String): Boolean {
        val skipAuthPaths = listOf(
            "/auth/login",
            "/auth/register",
            "/auth/refresh-token",
            "/auth/verify-phone"
        )
        return skipAuthPaths.any { path.contains(it) }
    }
    
    private fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun saveAccessToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }
    
    fun clearAccessToken() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .apply()
    }
}