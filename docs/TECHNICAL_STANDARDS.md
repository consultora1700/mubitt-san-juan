# TECHNICAL_STANDARDS.md
# Estándares Técnicos Obligatorios - SAN JUAN RIDE

## ESTÁNDARES NO NEGOCIABLES

### PERFORMANCE REQUIREMENTS (CRÍTICOS):
- **App startup time**: <2 segundos desde splash hasta pantalla principal
- **Fragment/Activity transitions**: <200ms para cualquier navegación
- **API response time**: <3 segundos promedio, <5 segundos máximo
- **Map loading**: <1 segundo para mostrar ubicación actual
- **Matching time**: <5 segundos desde solicitud hasta asignación conductor
- **Memory usage**: <150MB RAM en dispositivos gama media
- **Battery consumption**: <5% por hora de uso activo
- **Crash rate**: <0.1% (1 crash por cada 1000 sesiones)
- **ANR rate**: <0.05% (Application Not Responding)

---

## ARQUITECTURA ANDROID OBLIGATORIA

### PROJECT STRUCTURE:
```
app/
├── core/
│   ├── data/          # Repositories, DataSources
│   ├── domain/        # Use Cases, Entities
│   ├── di/           # Dependency Injection
│   └── utils/        # Utilities, Extensions
├── features/
│   ├── auth/         # Authentication module
│   ├── maps/         # Maps and location
│   ├── booking/      # Trip booking
│   ├── payments/     # Payment processing
│   ├── profile/      # User profiles
│   └── chat/         # Real-time communication
├── shared/
│   ├── ui/           # Shared UI components
│   ├── theme/        # Design system
│   └── navigation/   # Navigation graphs
└── MainApplication.kt
```

### ARCHITECTURE PATTERN:
- **MVVM (Model-View-ViewModel)** estricto
- **Clean Architecture** con capas bien definidas
- **Repository Pattern** para abstracción de datos
- **Use Cases** para lógica de negocio
- **Single Activity** + Navigation Component

### DEPENDENCY INJECTION:
```kotlin
// Hilt obligatorio para DI
@HiltAndroidApp
class MainApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = // Implementation
}
```

---

## UI/UX STANDARDS PREMIUM

### DESIGN SYSTEM OBLIGATORIO:
```kotlin
// Material Design 3 estricto
@Composable
fun SanJuanRideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SanJuanTypography,
        content = content
    )
}
```

### ANIMATION STANDARDS:
```kotlin
// Todas las transiciones deben ser suaves
@Composable
fun AnimatedBookingButton() {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(),
        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically()
    ) {
        BookingButton()
    }
}

// Micro-animaciones obligatorias
val buttonScale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
)
```

### COLOR PALETTE:
```kotlin
// Colores específicos para San Juan Ride
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C4C),        // Verde San Juan
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF89F8C7),
    secondary = Color(0xFF4F6354),      // Complementario
    surface = Color(0xFFFBFDF9),
    error = Color(0xFFBA1A1A),
    // Más colores definidos...
)
```

### TYPOGRAPHY:
```kotlin
val SanJuanTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    // Toda la escala tipográfica definida...
)
```

### SPACING SYSTEM:
```kotlin
object Spacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
    val jumbo = 48.dp
}
```

### UI COMPONENTS STANDARDS:
```kotlin
// Todos los componentes deben ser reutilizables
@Composable
fun SanJuanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text)
        }
    }
}
```

---

## NETWORKING STANDARDS

### RETROFIT CONFIGURATION:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(LoggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

### API RESPONSE HANDLING:
```kotlin
// Wrapper estándar para todas las respuestas
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String, val code: Int? = null) : ApiResult<T>()
    class Loading<T> : ApiResult<T>()
}

// Uso en repositories
suspend fun getDrivers(location: LatLng): ApiResult<List<Driver>> {
    return try {
        val response = apiService.getNearbyDrivers(location.latitude, location.longitude)
        if (response.isSuccessful) {
            ApiResult.Success(response.body()?.data ?: emptyList())
        } else {
            ApiResult.Error("Error getting drivers", response.code())
        }
    } catch (e: Exception) {
        ApiResult.Error(e.message ?: "Unknown error")
    }
}
```

### ERROR HANDLING:
```kotlin
// Manejo consistente de errores
@Composable
fun HandleApiError(
    apiResult: ApiResult<*>,
    onRetry: () -> Unit
) {
    when (apiResult) {
        is ApiResult.Error -> {
            ErrorDialog(
                message = apiResult.message,
                onRetry = onRetry,
                onDismiss = { /* handle dismiss */ }
            )
        }
        // Other states...
    }
}
```

---

## DATABASE STANDARDS

### ROOM CONFIGURATION:
```kotlin
@Database(
    entities = [
        User::class,
        Driver::class,
        Trip::class,
        Payment::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SanJuanRideDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun driverDao(): DriverDao
    abstract fun tripDao(): TripDao
    abstract fun paymentDao(): PaymentDao
}
```

### ENTITY STANDARDS:
```kotlin
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val id: String,
    val userId: String,
    val driverId: String?,
    val pickupLocation: LatLng,
    val dropoffLocation: LatLng,
    val status: TripStatus,
    val fare: Double,
    val createdAt: Long,
    val updatedAt: Long
)

// DAO estándar
@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserTrips(userId: String): Flow<List<Trip>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)
    
    @Update
    suspend fun updateTrip(trip: Trip)
}
```

---

## REAL-TIME COMMUNICATION

### WEBSOCKET STANDARDS:
```kotlin
class WebSocketManager @Inject constructor() {
    private var socket: Socket? = null
    
    fun connect(userId: String) {
        val options = IO.Options().apply {
            auth = mapOf("userId" to userId)
            timeout = 5000
        }
        
        socket = IO.socket(BuildConfig.WEBSOCKET_URL, options)
        socket?.connect()
        
        setupEventListeners()
    }
    
    private fun setupEventListeners() {
        socket?.on("trip_update") { args ->
            // Handle trip updates
            val tripData = args[0] as JSONObject
            // Process update...
        }
        
        socket?.on("driver_location") { args ->
            // Handle driver location updates
        }
    }
}
```

---

## MAPS AND LOCATION

### GOOGLE MAPS INTEGRATION:
```kotlin
@Composable
fun SanJuanMap(
    modifier: Modifier = Modifier,
    onLocationSelected: (LatLng) -> Unit
) {
    val mapView = rememberMapViewWithLifecycle()
    
    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { mapView ->
        mapView.getMapAsync { googleMap ->
            googleMap.apply {
                // San Juan default location
                val sanJuan = LatLng(-31.5375, -68.5364)
                moveCamera(CameraUpdateFactory.newLatLngZoom(sanJuan, 12f))
                
                // Custom styling
                setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
                
                // Custom markers
                addMarker(MarkerOptions().position(sanJuan).title("San Juan"))
                
                setOnMapClickListener(onLocationSelected)
            }
        }
    }
}
```

### LOCATION SERVICES:
```kotlin
class LocationRepository @Inject constructor(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): Flow<Location?> = callbackFlow {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
```

---

## SECURITY STANDARDS

### NETWORK SECURITY:
```kotlin
// Network security config
// res/xml/network_security_config.xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.sanjuanride.com</domain>
    </domain-config>
</network-security-config>
```

### DATA ENCRYPTION:
```kotlin
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val encryptedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveAuthToken(token: String) {
        encryptedPreferences.edit().putString("auth_token", token).apply()
    }
}
```

### INPUT VALIDATION:
```kotlin
// Validación estricta de inputs
object InputValidator {
    fun validateEmail(email: String): Boolean {
        return email.isNotBlank() && 
               Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun validatePhoneNumber(phone: String): Boolean {
        return phone.isNotBlank() && 
               phone.matches(Regex("^\\+?54[0-9]{10,13}$")) // Argentina format
    }
    
    fun sanitizeInput(input: String): String {
        return input.trim()
            .replace(Regex("[<>\"'&]"), "") // Basic XSS prevention
            .take(1000) // Prevent overflow
    }
}
```

---

## TESTING STANDARDS

### UNIT TESTING:
```kotlin
// Cada repository/use case/view model debe tener tests
@Test
fun `when getting nearby drivers, should return success result`() = runTest {
    // Given
    val mockDrivers = listOf(
        Driver(id = "1", name = "Driver 1", rating = 4.5),
        Driver(id = "2", name = "Driver 2", rating = 4.8)
    )
    coEvery { apiService.getNearbyDrivers(any(), any()) } returns 
        Response.success(ApiResponse(data = mockDrivers))
    
    // When
    val result = repository.getNearbyDrivers(LatLng(-31.5375, -68.5364))
    
    // Then
    assertTrue(result is ApiResult.Success)
    assertEquals(2, (result as ApiResult.Success).data.size)
}
```

### UI TESTING:
```kotlin
@Test
fun bookingFlow_completesSuccessfully() {
    composeTestRule.setContent {
        SanJuanRideTheme {
            BookingScreen()
        }
    }
    
    // Test booking flow
    composeTestRule
        .onNodeWithText("¿A dónde vamos?")
        .performClick()
    
    composeTestRule
        .onNodeWithTag("destination_input")
        .performTextInput("Hospital Rawson")
    
    composeTestRule
        .onNodeWithText("Confirmar viaje")
        .performClick()
    
    // Verify booking was created
    composeTestRule
        .onNodeWithText("Buscando conductor...")
        .assertIsDisplayed()
}
```

---

## PERFORMANCE OPTIMIZATION

### IMAGE LOADING:
```kotlin
// Coil con optimizaciones
@Composable
fun DriverAvatar(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .size(Size.ORIGINAL) // Maintain aspect ratio
            .build(),
        contentDescription = "Driver avatar",
        modifier = modifier.clip(CircleShape),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.placeholder_avatar),
        error = painterResource(R.drawable.placeholder_avatar)
    )
}
```

### MEMORY MANAGEMENT:
```kotlin
// ViewModels con manejo de lifecycle correcto
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    
    override fun onCleared() {
        super.onCleared()
        // Clean up resources
        bookingRepository.cancelActiveRequests()
    }
}
```

---

## LOGGING AND MONITORING

### LOGGING STANDARDS:
```kotlin
object Logger {
    private const val TAG = "SanJuanRide"
    
    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        Log.e(tag, message, throwable)
        // Send to Crashlytics in production
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().recordException(
                throwable ?: Exception(message)
            )
        }
    }
}
```

### ANALYTICS:
```kotlin
class AnalyticsManager @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun trackTripRequest(destination: String) {
        val bundle = Bundle().apply {
            putString("destination", destination)
            putString("user_type", "passenger")
        }
        firebaseAnalytics.logEvent("trip_requested", bundle)
    }
    
    fun trackTripCompleted(tripId: String, fare: Double, rating: Int) {
        val bundle = Bundle().apply {
            putString("trip_id", tripId)
            putDouble("fare", fare)
            putInt("rating", rating)
        }
        firebaseAnalytics.logEvent("trip_completed", bundle)
    }
}
```

---

## BUILD AND DEPLOYMENT

### GRADLE CONFIGURATION:
```kotlin
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.sanjuanride.app"
        minSdk 24  // Android 7.0+ (covers 95%+ devices)
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
        
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }
    
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "BASE_URL", "\"https://dev-api.sanjuanride.com/\"")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "BASE_URL", "\"https://api.sanjuanride.com/\"")
            
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}
```

### PROGUARD RULES:
```
# San Juan Ride specific rules
-keep class com.sanjuanride.app.data.models.** { *; }
-keep class com.sanjuanride.app.domain.entities.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Google Maps
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
```

---

## CODE QUALITY STANDARDS

### KOTLIN CODING STANDARDS:
```kotlin
// Naming conventions
class UserRepository // PascalCase for classes
val userName: String // camelCase for variables
const val MAX_RETRY_COUNT = 3 // UPPER_SNAKE_CASE for constants

// Function naming
fun getUserById(id: String): User // camelCase, descriptive
suspend fun fetchUserFromApi(id: String): ApiResult<User> // async functions clearly named

// Extension functions
fun String.isValidEmail(): Boolean = // Extensions in separate files
    this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

// Data classes
data class User(
    val id: String,
    val name: String,
    val email: String
) {
    // No logic in data classes
}
```

### DOCUMENTATION:
```kotlin
/**
 * Repository responsible for managing trip data and operations.
 * 
 * This repository handles:
 * - Creating new trip requests
 * - Updating trip status
 * - Fetching trip history
 * - Real-time trip tracking
 * 
 * @property apiService The API service for network operations
 * @property localDatabase Local database for offline support
 */
class TripRepository @Inject constructor(
    private val apiService: TripApiService,
    private val localDatabase: TripDao
) {
    
    /**
     * Creates a new trip request.
     * 
     * @param pickupLocation The pickup location coordinates
     * @param dropoffLocation The dropoff location coordinates
     * @param vehicleType The requested vehicle type
     * @return ApiResult containing the created trip or error information
     */
    suspend fun createTrip(
        pickupLocation: LatLng,
        dropoffLocation: LatLng,
        vehicleType: VehicleType
    ): ApiResult<Trip> {
        // Implementation...
    }
}
```

---

## ALGORITMOS ESPECÍFICOS PARA SAN JUAN

### MATCHING CONDUCTOR-PASAJERO:
```kotlin
/**
 * Algoritmo de matching optimizado para San Juan.
 * Considera proximidad, rating y conocimiento local.
 */
class DriverMatchingAlgorithm @Inject constructor() {
    
    data class DriverScore(
        val driver: Driver,
        val totalScore: Double,
        val proximityScore: Double,
        val ratingScore: Double,
        val localKnowledgeScore: Double,
        val availabilityScore: Double
    )
    
    suspend fun findBestDriver(
        passengerLocation: LatLng,
        availableDrivers: List<Driver>,
        tripType: TripType = TripType.STANDARD
    ): Driver? {
        
        if (availableDrivers.isEmpty()) return null
        
        val scoredDrivers = availableDrivers.map { driver ->
            calculateDriverScore(driver, passengerLocation, tripType)
        }.sortedByDescending { it.totalScore }
        
        // Logging para debugging
        Logger.d("Driver matching results: ${scoredDrivers.take(3)}")
        
        return scoredDrivers.firstOrNull()?.driver
    }
    
    private fun calculateDriverScore(
        driver: Driver,
        passengerLocation: LatLng,
        tripType: TripType
    ): DriverScore {
        
        // 1. Proximidad (40% del score)
        val distance = calculateDistance(driver.location, passengerLocation)
        val proximityScore = when {
            distance <= 1.0 -> 1.0  // 1km o menos = perfect
            distance <= 3.0 -> 0.8  // 3km = good
            distance <= 5.0 -> 0.6  // 5km = acceptable
            distance <= 10.0 -> 0.3 // 10km = poor
            else -> 0.1             // >10km = very poor
        } * 0.4
        
        // 2. Rating del conductor (25% del score)
        val ratingScore = (driver.rating / 5.0) * 0.25
        
        // 3. Conocimiento local de San Juan (20% del score)
        val localKnowledgeScore = calculateLocalKnowledgeScore(driver) * 0.20
        
        // 4. Disponibilidad y patrones (15% del score)
        val availabilityScore = calculateAvailabilityScore(driver) * 0.15
        
        val totalScore = proximityScore + ratingScore + localKnowledgeScore + availabilityScore
        
        return DriverScore(
            driver = driver,
            totalScore = totalScore,
            proximityScore = proximityScore,
            ratingScore = ratingScore,
            localKnowledgeScore = localKnowledgeScore,
            availabilityScore = availabilityScore
        )
    }
    
    private fun calculateLocalKnowledgeScore(driver: Driver): Double {
        return when {
            driver.localExperienceMonths >= 24 -> 1.0  // 2+ años en San Juan
            driver.localExperienceMonths >= 12 -> 0.8  // 1+ año
            driver.localExperienceMonths >= 6 -> 0.6   // 6+ meses
            driver.localExperienceMonths >= 3 -> 0.4   // 3+ meses
            else -> 0.2                                // Nuevo
        }
    }
    
    private fun calculateAvailabilityScore(driver: Driver): Double {
        val now = System.currentTimeMillis()
        return when {
            now - driver.lastActiveTime < 60_000 -> 1.0      // Activo hace <1min
            now - driver.lastActiveTime < 300_000 -> 0.8     // Activo hace <5min
            now - driver.lastActiveTime < 900_000 -> 0.6     // Activo hace <15min
            else -> 0.3                                       // Activo hace >15min
        }
    }
    
    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        // Implementación Haversine formula para distancia precisa
        val earthRadius = 6371.0 // km
        
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLng = Math.toRadians(point2.longitude - point1.longitude)
        
        val a = sin(deltaLat / 2).pow(2) + 
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
}
```

### CÁLCULO DE TARIFAS PARA SAN JUAN:
```kotlin
/**
 * Sistema de tarifas específico para San Juan.
 * Tarifas fijas por zona, surge controlado.
 */
class SanJuanFareCalculator @Inject constructor() {
    
    // Zonas definidas para San Juan
    enum class SanJuanZone(val baseRate: Double) {
        CENTRO(350.0),           // Centro de San Juan
        DESAMPARADOS(380.0),     // Desamparados
        RIVADAVIA(400.0),        // Rivadavia
        CHIMBAS(420.0),          // Chimbas
        POCITO(450.0),           // Pocito
        RAWSON(380.0),           // Rawson
        SANTA_LUCIA(500.0),      // Santa Lucía
        SUBURBAN(550.0)          // Zonas suburbanas
    }
    
    data class FareBreakdown(
        val baseFare: Double,
        val distanceFare: Double,
        val timeFare: Double,
        val surgeFare: Double,
        val total: Double,
        val surgeMultiplier: Double,
        val pickupZone: SanJuanZone,
        val dropoffZone: SanJuanZone
    )
    
    suspend fun calculateFare(
        pickupLocation: LatLng,
        dropoffLocation: LatLng,
        vehicleType: VehicleType,
        currentDemand: DemandLevel = DemandLevel.NORMAL
    ): FareBreakdown {
        
        val pickupZone = determineZone(pickupLocation)
        val dropoffZone = determineZone(dropoffLocation)
        
        // Base fare promedio entre zonas
        val baseFare = (pickupZone.baseRate + dropoffZone.baseRate) / 2
        
        // Cálculo por distancia
        val distance = calculateRouteDistance(pickupLocation, dropoffLocation)
        val distanceFare = distance * getDistanceRate(vehicleType)
        
        // Cálculo por tiempo estimado
        val estimatedTime = calculateEstimatedTime(pickupLocation, dropoffLocation)
        val timeFare = estimatedTime * getTimeRate(vehicleType)
        
        // Surge pricing controlado (máximo 1.5x vs 3x de Uber)
        val surgeMultiplier = calculateSurgeMultiplier(currentDemand, pickupZone)
        
        val subtotal = baseFare + distanceFare + timeFare
        val surgeFare = subtotal * (surgeMultiplier - 1.0)
        val total = subtotal * surgeMultiplier
        
        return FareBreakdown(
            baseFare = baseFare,
            distanceFare = distanceFare,
            timeFare = timeFare,
            surgeFare = surgeFare,
            total = total,
            surgeMultiplier = surgeMultiplier,
            pickupZone = pickupZone,
            dropoffZone = dropoffZone
        )
    }
    
    private fun calculateSurgeMultiplier(demand: DemandLevel, zone: SanJuanZone): Double {
        val baseSurge = when (demand) {
            DemandLevel.LOW -> 0.9      // Descuento en baja demanda
            DemandLevel.NORMAL -> 1.0   // Precio normal
            DemandLevel.HIGH -> 1.2     // Moderado aumento
            DemandLevel.VERY_HIGH -> 1.4 // Alto aumento
            DemandLevel.EXTREME -> 1.5   // Máximo aumento (vs 3x de Uber)
        }
        
        // Ajuste por zona (algunas zonas son más sensibles al surge)
        val zoneMultiplier = when (zone) {
            SanJuanZone.CENTRO -> 1.0        // Centro normal
            SanJuanZone.SUBURBAN -> 0.8      // Suburbanas menos surge
            else -> 0.9                      // Otras zonas moderado
        }
        
        return baseSurge * zoneMultiplier
    }
    
    private fun determineZone(location: LatLng): SanJuanZone {
        // Coordenadas aproximadas de las zonas de San Juan
        return when {
            isInCentro(location) -> SanJuanZone.CENTRO
            isInDesamparados(location) -> SanJuanZone.DESAMPARADOS
            isInRivadavia(location) -> SanJuanZone.RIVADAVIA
            isInChimbas(location) -> SanJuanZone.CHIMBAS
            isInPocito(location) -> SanJuanZone.POCITO
            isInRawson(location) -> SanJuanZone.RAWSON
            isInSantaLucia(location) -> SanJuanZone.SANTA_LUCIA
            else -> SanJuanZone.SUBURBAN
        }
    }
    
    private fun isInCentro(location: LatLng): Boolean {
        // Centro de San Juan: aproximadamente -31.5375, -68.5364
        val centerLat = -31.5375
        val centerLng = -68.5364
        val radius = 0.02 // ~2km radius
        
        return abs(location.latitude - centerLat) < radius &&
               abs(location.longitude - centerLng) < radius
    }
    
    // Más métodos para determinar otras zonas...
}
```

### ETA PREDICTION PARA SAN JUAN:
```kotlin
/**
 * Predicción de ETA específica para patrones de tráfico de San Juan.
 */
class SanJuanETACalculator @Inject constructor(
    private val googleDirectionsApi: GoogleDirectionsApi,
    private val trafficDataRepository: TrafficDataRepository
) {
    
    data class ETAResult(
        val estimatedMinutes: Int,
        val confidence: Double,
        val trafficFactor: Double,
        val baseTime: Int,
        val adjustedTime: Int
    )
    
    suspend fun calculateETA(
        from: LatLng,
        to: LatLng,
        currentTime: Long = System.currentTimeMillis()
    ): ETAResult {
        
        // 1. Obtener tiempo base de Google Directions
        val googleETA = googleDirectionsApi.getDirections(from, to)
        val baseTimeMinutes = googleETA.duration.inMinutes
        
        // 2. Aplicar factores específicos de San Juan
        val trafficFactor = calculateSanJuanTrafficFactor(from, to, currentTime)
        val adjustedTime = (baseTimeMinutes * trafficFactor).roundToInt()
        
        // 3. Calcular confianza basada en datos históricos
        val confidence = calculatePredictionConfidence(from, to, currentTime)
        
        return ETAResult(
            estimatedMinutes = adjustedTime,
            confidence = confidence,
            trafficFactor = trafficFactor,
            baseTime = baseTimeMinutes,
            adjustedTime = adjustedTime
        )
    }
    
    private suspend fun calculateSanJuanTrafficFactor(
        from: LatLng, 
        to: LatLng, 
        timestamp: Long
    ): Double {
        
        val hour = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp), 
            ZoneId.of("America/Argentina/San_Juan")
        ).hour
        
        // Patrones de tráfico específicos de San Juan
        val hourFactor = when (hour) {
            in 7..9 -> 1.4    // Rush mañana
            in 12..14 -> 1.2  // Almuerzo
            in 18..20 -> 1.5  // Rush tarde
            in 21..23 -> 0.9  // Noche tranquila
            in 0..6 -> 0.8    // Madrugada
            else -> 1.0       // Normal
        }
        
        // Factor por rutas conocidas problemáticas
        val routeFactor = if (crossesProblematicAreas(from, to)) 1.3 else 1.0
        
        // Factor por eventos especiales
        val eventFactor = checkSpecialEvents(timestamp)
        
        return hourFactor * routeFactor * eventFactor
    }
    
    private fun crossesProblematicAreas(from: LatLng, to: LatLng): Boolean {
        // Áreas conocidas con tráfico pesado en San Juan
        val problematicZones = listOf(
            LatLng(-31.5375, -68.5364), // Centro
            LatLng(-31.5180, -68.5287), // Av. Libertador
            // Más zonas problemáticas...
        )
        
        return problematicZones.any { zone ->
            routePassesThroughZone(from, to, zone, radiusKm = 0.5)
        }
    }
    
    private suspend fun checkSpecialEvents(timestamp: Long): Double {
        val specialEvents = trafficDataRepository.getSpecialEvents(timestamp)
        return when {
            specialEvents.any { it.type == EventType.FIESTA_SOL } -> 1.8
            specialEvents.any { it.type == EventType.FOOTBALL_MATCH } -> 1.4
            specialEvents.any { it.type == EventType.UNIVERSITY_EVENT } -> 1.2
            else -> 1.0
        }
    }
}
```

---

## VALIDACIÓN Y TESTING OBLIGATORIOS

### TEST COVERAGE REQUIREMENTS:
- **Unit Tests**: >90% coverage para business logic
- **Integration Tests**: 100% coverage para APIs críticas
- **UI Tests**: 100% coverage para flujos principales
- **Performance Tests**: Todas las operaciones críticas

### TESTING PYRAMID:
```kotlin
// Unit Tests (70% de todos los tests)
@Test
fun `calculateFare should return correct breakdown for centro zone`() = runTest {
    // Given
    val pickupLocation = LatLng(-31.5375, -68.5364) // Centro
    val dropoffLocation = LatLng(-31.5400, -68.5300) // También centro
    
    // When
    val result = fareCalculator.calculateFare(
        pickupLocation, dropoffLocation, VehicleType.STANDARD
    )
    
    // Then
    assertEquals(SanJuanZone.CENTRO, result.pickupZone)
    assertEquals(SanJuanZone.CENTRO, result.dropoffZone)
    assertTrue(result.total > 0)
    assertTrue(result.surgeMultiplier <= 1.5) // Máximo surge
}

// Integration Tests (20% de todos los tests)
@Test
fun `full booking flow should complete successfully`() = runTest {
    // Test completo de booking desde request hasta payment
}

// UI Tests (10% de todos los tests)
@Test
fun `user can complete booking flow without errors`() {
    // Test E2E de UI
}
```

### PERFORMANCE TESTING:
```kotlin
@Test
fun `driver matching should complete within 5 seconds`() = runTest {
    val startTime = System.currentTimeMillis()
    
    val result = driverMatchingAlgorithm.findBestDriver(
        passengerLocation = testLocation,
        availableDrivers = generateTestDrivers(100)
    )
    
    val duration = System.currentTimeMillis() - startTime
    assertTrue("Matching took ${duration}ms, should be <5000ms", duration < 5000)
    assertNotNull(result)
}
```

---

## MONITORING Y ALERTAS

### MÉTRICAS CRÍTICAS A MONITOREAR:
```kotlin
object CriticalMetrics {
    // Performance metrics
    const val MAX_APP_STARTUP_TIME = 2000L // ms
    const val MAX_API_RESPONSE_TIME = 3000L // ms
    const val MAX_MATCHING_TIME = 5000L // ms
    
    // Quality metrics
    const val MAX_CRASH_RATE = 0.001 // 0.1%
    const val MIN_RATING = 4.0
    const val MIN_RETENTION_7_DAY = 0.6 // 60%
    
    // Business metrics
    const val MIN_DRIVER_ACCEPTANCE_RATE = 0.8 // 80%
    const val MAX_TRIP_CANCELLATION_RATE = 0.1 // 10%
}
```

### ALERTAS AUTOMÁTICAS:
```kotlin
class AlertingSystem @Inject constructor() {
    
    fun checkCriticalMetrics() {
        // Performance alerts
        if (getCurrentAppStartupTime() > CriticalMetrics.MAX_APP_STARTUP_TIME) {
            sendAlert(AlertType.PERFORMANCE_DEGRADATION, "App startup time exceeded")
        }
        
        // Quality alerts
        if (getCurrentCrashRate() > CriticalMetrics.MAX_CRASH_RATE) {
            sendAlert(AlertType.QUALITY_ISSUE, "Crash rate exceeded threshold")
        }
        
        // Business alerts
        if (getDriverAcceptanceRate() < CriticalMetrics.MIN_DRIVER_ACCEPTANCE_RATE) {
            sendAlert(AlertType.BUSINESS_ISSUE, "Driver acceptance rate too low")
        }
    }
}
```

---

## RECORDATORIOS FINALES

### NUNCA COMPROMETER EN:
1. **Performance**: Los targets son obligatorios, no opcionales
2. **UX/UI Quality**: Debe rivalizar con Uber en todo momento
3. **Security**: Zero tolerance para vulnerabilidades
4. **Testing**: Sin tests = sin merge al main branch
5. **Documentation**: Todo debe estar documentado

### PRINCIPIOS DE DESARROLLO:
- **"San Juan First"**: Cada decisión optimizada para el mercado local
- **"Quality over Speed"**: Mejor tardarse y hacerlo bien
- **"User Experience is King"**: UX es más importante que features nuevos
- **"No Surprises"**: Performance predecible y consistente
- **"Fail Fast"**: Detectar problemas lo antes posible

### ESCALATION PROCESS:
- **Performance issues**: Escalar inmediatamente a tech lead
- **UX/UI concerns**: Design review obligatorio antes de continuar
- **Security vulnerabilities**: Stop everything, fix immediately
- **API failures**: Implement fallbacks, never fail silently

**ESTOS ESTÁNDARES NO SON NEGOCIABLES. CADA LÍNEA DE CÓDIGO DEBE CUMPLIR ESTOS CRITERIOS ANTES DE SER CONSIDERADA COMPLETA.**