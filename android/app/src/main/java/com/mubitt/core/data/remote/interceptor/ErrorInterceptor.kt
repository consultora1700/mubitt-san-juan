package com.mubitt.core.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor para manejar errores de red y logging
 * Proporciona información detallada para debugging
 */
@Singleton
class ErrorInterceptor @Inject constructor() : Interceptor {
    
    companion object {
        private const val TAG = "MubittNetworkError"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        return try {
            val response = chain.proceed(request)
            
            // Log errores HTTP
            if (!response.isSuccessful) {
                Log.e(TAG, "HTTP Error: ${response.code} ${response.message} for ${request.url}")
                
                // Log específico para errores críticos
                when (response.code) {
                    401 -> Log.e(TAG, "Authentication failed - token may be expired")
                    403 -> Log.e(TAG, "Access forbidden - insufficient permissions")
                    404 -> Log.e(TAG, "Endpoint not found: ${request.url}")
                    429 -> Log.e(TAG, "Rate limit exceeded for ${request.url}")
                    in 500..599 -> Log.e(TAG, "Server error - backend issue")
                }
            }
            
            response
        } catch (e: IOException) {
            Log.e(TAG, "Network error for ${request.url}: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error for ${request.url}: ${e.message}", e)
            throw e
        }
    }
}