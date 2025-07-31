package com.mubitt.core.data.remote.util

import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.data.remote.dto.MubittApiResponse
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utilities para manejo de respuestas de red
 * Convierte Response<T> a ApiResult<T> con error handling robusto
 */

suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<MubittApiResponse<T>>
): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true && body.data != null) {
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(
                    message = body?.message ?: "Unknown error occurred",
                    code = response.code()
                )
            }
        } else {
            ApiResult.Error(
                message = getErrorMessage(response.code()),
                code = response.code()
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(
            message = when (e) {
                is UnknownHostException -> "Sin conexión a internet. Verifica tu conexión."
                is SocketTimeoutException -> "La conexión tardó demasiado. Intenta nuevamente."
                is IOException -> "Error de conexión. Revisa tu conexión a internet."
                else -> "Error inesperado: ${e.message}"
            },
            throwable = e
        )
    }
}

private fun getErrorMessage(code: Int): String {
    return when (code) {
        400 -> "Solicitud inválida. Verifica los datos ingresados."
        401 -> "Sesión expirada. Por favor inicia sesión nuevamente."
        403 -> "No tienes permisos para realizar esta acción."
        404 -> "El recurso solicitado no fue encontrado."
        408 -> "Tiempo de espera agotado. Intenta nuevamente."
        429 -> "Demasiadas solicitudes. Espera un momento e intenta nuevamente."
        in 500..599 -> "Error del servidor. Estamos trabajando para solucionarlo."
        else -> "Error desconocido (código: $code)"
    }
}

/**
 * Extension function para convertir Response básico a ApiResult
 */
suspend fun <T> Response<T>.toApiResult(): ApiResult<T> {
    return if (isSuccessful && body() != null) {
        ApiResult.Success(body()!!)
    } else {
        ApiResult.Error(
            message = getErrorMessage(code()),
            code = code()
        )
    }
}