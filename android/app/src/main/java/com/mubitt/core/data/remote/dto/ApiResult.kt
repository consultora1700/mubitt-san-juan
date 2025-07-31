package com.mubitt.core.data.remote.dto

/**
 * Sealed class para manejar todos los estados de respuesta de API
 * Implementa error handling robusto para Mubitt
 */
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(
        val message: String,
        val code: Int? = null,
        val throwable: Throwable? = null
    ) : ApiResult<T>()
    class Loading<T> : ApiResult<T>()
}

/**
 * Response wrapper estándar para todas las APIs de Mubitt
 */
data class MubittApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val errorCode: String?
)

/**
 * Extension function para convertir Response a ApiResult
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> ApiResult<T>.onError(action: (String, Int?) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) {
        action(message, code)
    }
    return this
}

inline fun <T> ApiResult<T>.onLoading(action: () -> Unit): ApiResult<T> {
    if (this is ApiResult.Loading) {
        action()
    }
    return this
}