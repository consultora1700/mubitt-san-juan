package com.mubitt.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PermissionManager para Mubitt
 * Maneja permisos críticos: ubicación, notificaciones, cámara
 * Diseñado para UX sin fricción
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val _permissionStates = MutableStateFlow(PermissionStates())
    val permissionStates: StateFlow<PermissionStates> = _permissionStates.asStateFlow()
    
    /**
     * Verifica si tiene permisos de ubicación
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Verifica si tiene permisos de ubicación en background
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No requerido en versiones anteriores
        }
    }
    
    /**
     * Verifica si tiene permisos de notificaciones
     */
    fun hasNotificationPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No requerido en versiones anteriores
        }
    }
    
    /**
     * Verifica si tiene permisos de cámara
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Actualiza el estado de todos los permisos
     */
    fun updatePermissionStates() {
        _permissionStates.value = PermissionStates(
            hasLocationPermission = hasLocationPermission(),
            hasBackgroundLocationPermission = hasBackgroundLocationPermission(),
            hasNotificationPermission = hasNotificationPermission(),
            hasCameraPermission = hasCameraPermission()
        )
    }
    
    /**
     * Obtiene los permisos requeridos para la funcionalidad básica
     */
    fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        
        // Ubicación (crítico)
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        
        // Notificaciones (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        return permissions.toTypedArray()
    }
    
    /**
     * Obtiene permisos opcionales
     */
    fun getOptionalPermissions(): Array<String> {
        val permissions = mutableListOf<String>()
        
        // Cámara para fotos de perfil
        permissions.add(Manifest.permission.CAMERA)
        
        // Ubicación en background (para conductores)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        
        return permissions.toTypedArray()
    }
    
    /**
     * Verifica si todos los permisos críticos están otorgados
     */
    fun hasAllCriticalPermissions(): Boolean {
        return hasLocationPermission() && hasNotificationPermission()
    }
}

/**
 * Helper para manejo de permisos en Activities
 */
class ActivityPermissionHelper(
    private val activity: ComponentActivity,
    private val permissionManager: PermissionManager
) {
    
    private var onPermissionsResult: ((Boolean) -> Unit)? = null
    
    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionManager.updatePermissionStates()
        val allGranted = permissions.values.all { it }
        onPermissionsResult?.invoke(allGranted)
    }
    
    /**
     * Solicita permisos críticos
     */
    fun requestCriticalPermissions(onResult: (Boolean) -> Unit) {
        onPermissionsResult = onResult
        val permissions = permissionManager.getRequiredPermissions()
        permissionLauncher.launch(permissions)
    }
    
    /**
     * Solicita permisos opcionales
     */
    fun requestOptionalPermissions(onResult: (Boolean) -> Unit) {
        onPermissionsResult = onResult
        val permissions = permissionManager.getOptionalPermissions()
        permissionLauncher.launch(permissions)
    }
    
    /**
     * Verifica y solicita permisos si es necesario
     */
    fun checkAndRequestPermissions(onResult: (Boolean) -> Unit) {
        if (permissionManager.hasAllCriticalPermissions()) {
            onResult(true)
        } else {
            requestCriticalPermissions(onResult)
        }
    }
}

/**
 * Estados de permisos
 */
data class PermissionStates(
    val hasLocationPermission: Boolean = false,
    val hasBackgroundLocationPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasCameraPermission: Boolean = false
) {
    val hasAllCriticalPermissions: Boolean
        get() = hasLocationPermission && hasNotificationPermission
}