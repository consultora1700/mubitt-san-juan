package com.mubitt.shared.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mubitt.core.domain.model.Location
import com.mubitt.core.domain.model.Trip
import com.mubitt.features.auth.LoginScreen
import com.mubitt.features.auth.RegisterScreen
import com.mubitt.features.booking.BookingScreen
import com.mubitt.features.home.HomeScreen
import com.mubitt.features.maps.MapScreen
import com.mubitt.features.onboarding.OnboardingScreen
import com.mubitt.features.profile.ProfileScreen
import com.mubitt.features.splash.SplashScreen
import com.mubitt.features.trip.TripCompletedScreen
import com.mubitt.features.trip.TripTrackingScreen
import com.mubitt.features.history.TripHistoryScreen
import com.mubitt.features.payments.PaymentMethodsScreen
import com.mubitt.features.support.SupportScreen
import com.mubitt.features.settings.SettingsScreen

/**
 * Mubitt Navigation System
 * 
 * Single Activity Architecture with Navigation Component
 * Standards: Smooth transitions <200ms, no navigation bugs
 * Architecture: Type-safe navigation with sealed classes
 */

sealed class MubittDestination(val route: String) {
    object Splash : MubittDestination("splash")
    object Onboarding : MubittDestination("onboarding")
    object Login : MubittDestination("login")
    object Register : MubittDestination("register")
    object Home : MubittDestination("home")
    object Map : MubittDestination("map")
    object Booking : MubittDestination("booking/{pickupLat}/{pickupLng}/{pickupAddress}/{dropoffLat}/{dropoffLng}/{dropoffAddress}") {
        fun createRoute(pickupLocation: Location, dropoffLocation: Location): String {
            return "booking/${pickupLocation.latitude}/${pickupLocation.longitude}/${pickupLocation.address}/${dropoffLocation.latitude}/${dropoffLocation.longitude}/${dropoffLocation.address}"
        }
    }
    object TripTracking : MubittDestination("trip_tracking/{tripId}") {
        fun createRoute(tripId: String): String {
            return "trip_tracking/$tripId"
        }
    }
    object TripCompleted : MubittDestination("trip_completed/{tripId}") {
        fun createRoute(tripId: String): String {
            return "trip_completed/$tripId"
        }
    }
    object Profile : MubittDestination("profile")
    object TripHistory : MubittDestination("trip_history")
    object PaymentMethods : MubittDestination("payment_methods")
    object Support : MubittDestination("support")
    object Settings : MubittDestination("settings")
}

@Composable
fun MubittNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = MubittDestination.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(MubittDestination.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    // TODO: Check if user is logged in and has seen onboarding
                    navController.navigate(MubittDestination.Onboarding.route) {
                        popUpTo(MubittDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Onboarding Screen
        composable(MubittDestination.Onboarding.route) {
            OnboardingScreen(
                onSkip = {
                    navController.navigate(MubittDestination.Login.route) {
                        popUpTo(MubittDestination.Onboarding.route) { inclusive = true }
                    }
                },
                onComplete = {
                    navController.navigate(MubittDestination.Login.route) {
                        popUpTo(MubittDestination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Login Screen
        composable(MubittDestination.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(MubittDestination.Register.route)
                },
                onNavigateToMain = {
                    navController.navigate(MubittDestination.Home.route) {
                        popUpTo(MubittDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Register Screen
        composable(MubittDestination.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(MubittDestination.Home.route) {
                        popUpTo(MubittDestination.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Home Screen - Main screen
        composable(MubittDestination.Home.route) {
            HomeScreen(
                onNavigateToBooking = { pickupLocation, dropoffLocation ->
                    val route = MubittDestination.Booking.createRoute(pickupLocation, dropoffLocation)
                    navController.navigate(route)
                },
                onNavigateToProfile = {
                    navController.navigate(MubittDestination.Profile.route)
                },
                onNavigateToHistory = {
                    navController.navigate(MubittDestination.TripHistory.route)
                }
            )
        }
        
        // Map Screen
        composable(MubittDestination.Map.route) {
            MapScreen(
                onNavigateToBooking = { pickupLocation, dropoffLocation ->
                    val route = MubittDestination.Booking.createRoute(pickupLocation, dropoffLocation)
                    navController.navigate(route)
                }
            )
        }
        
        // Booking Screen
        composable(MubittDestination.Booking.route) { backStackEntry ->
            val pickupLat = backStackEntry.arguments?.getString("pickupLat")?.toDoubleOrNull() ?: 0.0
            val pickupLng = backStackEntry.arguments?.getString("pickupLng")?.toDoubleOrNull() ?: 0.0
            val pickupAddress = backStackEntry.arguments?.getString("pickupAddress") ?: ""
            val dropoffLat = backStackEntry.arguments?.getString("dropoffLat")?.toDoubleOrNull() ?: 0.0
            val dropoffLng = backStackEntry.arguments?.getString("dropoffLng")?.toDoubleOrNull() ?: 0.0
            val dropoffAddress = backStackEntry.arguments?.getString("dropoffAddress") ?: ""
            
            val pickupLocation = Location(pickupLat, pickupLng, pickupAddress)
            val dropoffLocation = Location(dropoffLat, dropoffLng, dropoffAddress)
            
            BookingScreen(
                pickupLocation = pickupLocation,
                dropoffLocation = dropoffLocation,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTripRequested = { tripId ->
                    val route = MubittDestination.TripTracking.createRoute(tripId)
                    navController.navigate(route) {
                        popUpTo(MubittDestination.Home.route)
                    }
                }
            )
        }
        
        // Trip Tracking Screen
        composable(MubittDestination.TripTracking.route) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TripTrackingScreen(
                tripId = tripId,
                onCallDriver = { phoneNumber ->
                    // TODO: Implementar llamada
                },
                onMessageDriver = {
                    // TODO: Implementar mensajería
                },
                onEmergency = {
                    // TODO: Implementar emergencia
                },
                onShareTrip = {
                    // TODO: Implementar compartir viaje
                },
                onTripCompleted = { trip ->
                    val route = MubittDestination.TripCompleted.createRoute(trip.id)
                    navController.navigate(route) {
                        popUpTo(MubittDestination.TripTracking.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Trip Completed Screen
        composable(MubittDestination.TripCompleted.route) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            // TODO: Obtener trip del repository
            val mockTrip = Trip(
                id = tripId,
                userId = "user_001",
                driverId = "driver_001",
                pickupLocation = Location(-31.5375, -68.5364, "San Juan Centro"),
                dropoffLocation = Location(-31.5400, -68.5300, "Hospital Rawson"),
                status = Trip.Status.COMPLETED,
                fare = 450.0,
                createdAt = System.currentTimeMillis()
            )
            
            TripCompletedScreen(
                trip = mockTrip,
                onNavigateToHome = {
                    navController.navigate(MubittDestination.Home.route) {
                        popUpTo(MubittDestination.TripCompleted.route) { inclusive = true }
                    }
                },
                onRequestNewTrip = {
                    navController.navigate(MubittDestination.Home.route) {
                        popUpTo(MubittDestination.TripCompleted.route) { inclusive = true }
                    }
                },
                onNavigateToSupport = {
                    navController.navigate(MubittDestination.Support.route)
                }
            )
        }
        
        // Profile Screen
        composable(MubittDestination.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPaymentMethods = {
                    navController.navigate(MubittDestination.PaymentMethods.route)
                },
                onNavigateToTripHistory = {
                    navController.navigate(MubittDestination.TripHistory.route)
                },
                onNavigateToSupport = {
                    navController.navigate(MubittDestination.Support.route)
                },
                onNavigateToSettings = {
                    navController.navigate(MubittDestination.Settings.route)
                },
                onLogout = {
                    navController.navigate(MubittDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Trip History Screen
        composable(MubittDestination.TripHistory.route) {
            TripHistoryScreen(
                navController = navController
            )
        }
        
        // Payment Methods Screen
        composable(MubittDestination.PaymentMethods.route) {
            PaymentMethodsScreen(
                navController = navController
            )
        }
        
        // Support Screen
        composable(MubittDestination.Support.route) {
            SupportScreen(
                navController = navController
            )
        }
        
        // Settings Screen
        composable(MubittDestination.Settings.route) {
            SettingsScreen(
                navController = navController
            )
        }
    }
}