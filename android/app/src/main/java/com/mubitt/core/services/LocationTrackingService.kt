package com.mubitt.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mubitt.MainActivity
import com.mubitt.R
import com.mubitt.core.data.location.LocationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

/**
 * Servicio de tracking de ubicación en foreground
 * Para tracking continuo durante viajes activos
 */
@AndroidEntryPoint
class LocationTrackingService : Service() {
    
    @Inject
    lateinit var locationService: LocationService
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isTracking = false
    
    companion object {
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "location_tracking"
        private const val CHANNEL_NAME = "Mubitt Location Tracking"
        
        const val ACTION_START_TRACKING = "START_TRACKING"
        const val ACTION_STOP_TRACKING = "STOP_TRACKING"
        
        fun startTracking(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START_TRACKING
            }
            context.startForegroundService(intent)
        }
        
        fun stopTracking(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP_TRACKING
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startLocationTracking()
            ACTION_STOP_TRACKING -> stopLocationTracking()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startLocationTracking() {
        if (isTracking) return
        
        isTracking = true
        startForeground(NOTIFICATION_ID, createNotification())
        
        serviceScope.launch {
            locationService.observeLocationUpdates()
                .catch { error ->
                    android.util.Log.e("LocationTracking", "Error tracking location", error)
                }
                .collect { location ->
                    // TODO: Enviar ubicación al servidor
                    android.util.Log.d("LocationTracking", "New location: ${location.latitude}, ${location.longitude}")
                }
        }
    }
    
    private fun stopLocationTracking() {
        isTracking = false
        serviceScope.cancel()
        stopForeground(true)
        stopSelf()
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Mubitt está activo")
            .setContentText("Compartiendo tu ubicación para el viaje")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setColor(getColor(R.color.mubitt_primary))
            .build()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación para tracking de ubicación durante viajes"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}