package com.mubitt.core.ui.components.maps

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.mubitt.core.domain.model.Location
import com.mubitt.shared.theme.MubittTheme

/**
 * Componente de mapa personalizado para Mubitt
 * Integra Google Maps con estilo específico de San Juan
 */
@Composable
fun MubittMapView(
    modifier: Modifier = Modifier,
    initialLocation: Location? = null,
    pickupLocation: Location? = null,
    dropoffLocation: Location? = null,
    driverLocation: Location? = null,
    onMapReady: (GoogleMap) -> Unit = {},
    onLocationSelected: (Location) -> Unit = {}
) {
    val context = LocalContext.current
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    
    // Default location: Centro de San Juan
    val defaultSanJuan = LatLng(-31.5375, -68.5364)
    
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                onCreate(null)
                onResume()
                getMapAsync { map ->
                    googleMap = map
                    setupMap(map)
                    onMapReady(map)
                    
                    // Configurar ubicación inicial
                    val initialLatLng = initialLocation?.let { 
                        LatLng(it.latitude, it.longitude) 
                    } ?: defaultSanJuan
                    
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(initialLatLng, 13f)
                    )
                    
                    // Agregar marcadores iniciales
                    updateMapMarkers(map, pickupLocation, dropoffLocation, driverLocation)
                    
                    // Configurar click listener
                    map.setOnMapClickListener { latLng ->
                        val clickedLocation = Location(
                            latitude = latLng.latitude,
                            longitude = latLng.longitude,
                            address = "Ubicación seleccionada",
                            reference = null
                        )
                        onLocationSelected(clickedLocation)
                    }
                }
            }
        },
        update = { mapView ->
            googleMap?.let { map ->
                updateMapMarkers(map, pickupLocation, dropoffLocation, driverLocation)
            }
        }
    )
}

/**
 * Configura el estilo y comportamiento inicial del mapa
 */
private fun setupMap(map: GoogleMap) {
    map.apply {
        // Configuración de UI
        uiSettings.apply {
            isZoomControlsEnabled = false
            isCompassEnabled = true
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = false
        }
        
        // Estilo del mapa - Mode noche automático según el tema
        setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                map.context,
                com.mubitt.R.raw.mubitt_map_style // TODO: Crear archivo de estilo
            )
        )
        
        // Configurar límites de San Juan
        val sanJuanBounds = LatLngBounds(
            LatLng(-31.700, -68.700), // SW
            LatLng(-31.400, -68.300)  // NE
        )
        
        // Restringir la cámara a la región de San Juan
        setLatLngBoundsForCameraTarget(sanJuanBounds)
        setMinZoomPreference(10f)
        setMaxZoomPreference(18f)
        
        // Configurar tipo de mapa
        mapType = GoogleMap.MAP_TYPE_NORMAL
    }
}

/**
 * Actualiza los marcadores en el mapa
 */
private fun updateMapMarkers(
    map: GoogleMap,
    pickupLocation: Location?,
    dropoffLocation: Location?,
    driverLocation: Location?
) {
    // Limpiar marcadores existentes
    map.clear()
    
    // Marcador de pickup (verde)
    pickupLocation?.let { location ->
        map.addMarker(
            MarkerOptions()
                .position(LatLng(location.latitude, location.longitude))
                .title("Punto de recogida")
                .snippet(location.address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
    }
    
    // Marcador de destino (rojo)
    dropoffLocation?.let { location ->
        map.addMarker(
            MarkerOptions()
                .position(LatLng(location.latitude, location.longitude))
                .title("Destino")
                .snippet(location.address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }
    
    // Marcador de conductor (azul, con icono de auto)
    driverLocation?.let { location ->
        map.addMarker(
            MarkerOptions()
                .position(LatLng(location.latitude, location.longitude))
                .title("Conductor")
                .snippet("Tu conductor se acerca")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .rotation(0f) // TODO: Usar bearing real del conductor
        )
    }
    
    // Si hay pickup y dropoff, mostrar la ruta
    if (pickupLocation != null && dropoffLocation != null) {
        drawRoute(map, pickupLocation, dropoffLocation)
    }
    
    // Ajustar cámara para mostrar todos los marcadores
    adjustCameraToShowMarkers(map, pickupLocation, dropoffLocation, driverLocation)
}

/**
 * Dibuja la ruta entre pickup y dropoff
 */
private fun drawRoute(map: GoogleMap, pickup: Location, dropoff: Location) {
    // TODO: Integrar con Google Directions API para obtener ruta real
    // Por ahora, dibujamos una línea recta
    val polylineOptions = PolylineOptions()
        .add(LatLng(pickup.latitude, pickup.longitude))
        .add(LatLng(dropoff.latitude, dropoff.longitude))
        .color(0xFF4CAF50.toInt()) // Verde Mubitt
        .width(8f)
        .pattern(listOf(Dot(), Gap(20f)))
    
    map.addPolyline(polylineOptions)
}

/**
 * Ajusta la cámara para mostrar todos los marcadores
 */
private fun adjustCameraToShowMarkers(
    map: GoogleMap,
    pickupLocation: Location?,
    dropoffLocation: Location?,
    driverLocation: Location?
) {
    val locations = listOfNotNull(pickupLocation, dropoffLocation, driverLocation)
    
    if (locations.isNotEmpty()) {
        val builder = LatLngBounds.Builder()
        locations.forEach { location ->
            builder.include(LatLng(location.latitude, location.longitude))
        }
        
        val bounds = builder.build()
        val padding = 100 // pixels
        
        try {
            map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, padding)
            )
        } catch (e: Exception) {
            // Fallback si no se puede ajustar la cámara
            locations.firstOrNull()?.let { location ->
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        14f
                    )
                )
            }
        }
    }
}