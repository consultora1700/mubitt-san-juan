package com.mubitt.core.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mubitt.MainActivity
import com.mubitt.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Firebase Cloud Messaging Service para Mubitt
 * Maneja notificaciones push críticas para viajes
 */
@AndroidEntryPoint
class MubittFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val CHANNEL_ID = "mubitt_notifications"
        private const val CHANNEL_NAME = "Mubitt Notifications"
        private const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    /**
     * Se llama cuando llega un nuevo token de FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // TODO: Enviar token al backend de Mubitt
        // Para asociar el dispositivo con el usuario
        sendTokenToServer(token)
    }
    
    /**
     * Se llama cuando llega un mensaje push
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Manejar diferentes tipos de notificaciones
        when (remoteMessage.data["type"]) {
            "trip_accepted" -> handleTripAccepted(remoteMessage)
            "driver_arrived" -> handleDriverArrived(remoteMessage)
            "trip_started" -> handleTripStarted(remoteMessage)
            "trip_completed" -> handleTripCompleted(remoteMessage)
            "trip_cancelled" -> handleTripCancelled(remoteMessage)
            "new_trip_request" -> handleNewTripRequest(remoteMessage) // Para conductores
            else -> handleGenericNotification(remoteMessage)
        }
    }
    
    private fun handleTripAccepted(remoteMessage: RemoteMessage) {
        val title = "¡Viaje confirmado!"
        val body = "Tu conductor ${remoteMessage.data["driver_name"]} viene en camino"
        val tripId = remoteMessage.data["trip_id"]
        
        showNotification(
            title = title,
            body = body,
            data = mapOf("trip_id" to tripId, "action" to "open_trip_tracking")
        )
    }
    
    private fun handleDriverArrived(remoteMessage: RemoteMessage) {
        val title = "¡Tu conductor llegó!"
        val body = "Tu conductor ${remoteMessage.data["driver_name"]} te está esperando"
        val tripId = remoteMessage.data["trip_id"]
        
        showNotification(
            title = title,
            body = body,
            data = mapOf("trip_id" to tripId, "action" to "open_trip_tracking"),
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    private fun handleTripStarted(remoteMessage: RemoteMessage) {
        val title = "Viaje iniciado"
        val body = "Tu viaje a ${remoteMessage.data["destination"]} ha comenzado"
        val tripId = remoteMessage.data["trip_id"]
        
        showNotification(
            title = title,
            body = body,
            data = mapOf("trip_id" to tripId, "action" to "open_trip_tracking")
        )
    }
    
    private fun handleTripCompleted(remoteMessage: RemoteMessage) {
        val title = "Viaje completado"
        val body = "¿Cómo fue tu experiencia? Califica tu viaje"
        val tripId = remoteMessage.data["trip_id"]
        
        showNotification(
            title = title,
            body = body,
            data = mapOf("trip_id" to tripId, "action" to "rate_trip")
        )
    }
    
    private fun handleTripCancelled(remoteMessage: RemoteMessage) {
        val title = "Viaje cancelado"
        val body = remoteMessage.data["cancellation_reason"] ?: "Tu viaje ha sido cancelado"
        
        showNotification(
            title = title,
            body = body,
            data = mapOf("action" to "open_main")
        )
    }
    
    private fun handleNewTripRequest(remoteMessage: RemoteMessage) {
        // Para conductores
        val title = "Nueva solicitud de viaje"
        val body = "Viaje desde ${remoteMessage.data["pickup_address"]} a ${remoteMessage.data["dropoff_address"]}"
        val tripId = remoteMessage.data["trip_id"]
        
        showNotification(
            title = title,
            body = body,
            data = mapOf("trip_id" to tripId, "action" to "driver_trip_request"),
            priority = NotificationCompat.PRIORITY_HIGH
        )
    }
    
    private fun handleGenericNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Mubitt"
        val body = remoteMessage.notification?.body ?: "Tienes una nueva notificación"
        
        showNotification(
            title = title,
            body = body,
            data = mapOf("action" to "open_main")
        )
    }
    
    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String?> = emptyMap(),
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Agregar data como extras
            data.forEach { (key, value) ->
                value?.let { putExtra(key, it) }
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // TODO: Crear icono
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(getColor(R.color.mubitt_primary)) // TODO: Definir color
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de viajes y actualizaciones de Mubitt"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun sendTokenToServer(token: String) {
        // TODO: Implementar envío de token al backend
        // Esto se hará cuando tengamos el backend configurado
        android.util.Log.d("FCM_TOKEN", "New token: $token")
    }
}