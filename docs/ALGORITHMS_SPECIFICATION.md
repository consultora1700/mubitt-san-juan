# ALGORITHMS_SPECIFICATION.md
# Especificaciones de Algoritmos para San Juan Ride

## ALGORITMO DE MATCHING CONDUCTOR-PASAJERO

### ESPECIFICACIONES TÉCNICAS:
```python
def advanced_driver_matching_san_juan(
    passenger_location: LatLng,
    passenger_destination: LatLng,
    available_drivers: List[Driver],
    trip_urgency: UrgencyLevel,
    passenger_preferences: UserPreferences
) -> MatchingResult:
    """
    Algoritmo avanzado de matching específico para San Juan.
    
    Factores considerados:
    1. Proximidad geográfica (35%)
    2. Rating y experiencia conductor (25%)
    3. Conocimiento local San Juan (20%)
    4. Disponibilidad y patrones (15%)
    5. Preferencias usuario (5%)
    """
```

### FACTORES DE SCORING:

#### 1. Proximidad Geográfica (35%):
```python
def calculate_proximity_score(driver_location: LatLng, passenger_location: LatLng) -> float:
    distance_km = haversine_distance(driver_location, passenger_location)
    
    score_map = {
        (0.0, 1.0): 1.0,     # <1km = perfecto
        (1.0, 2.0): 0.9,     # 1-2km = excelente  
        (2.0, 3.0): 0.8,     # 2-3km = muy bueno
        (3.0, 5.0): 0.6,     # 3-5km = bueno
        (5.0, 8.0): 0.4,     # 5-8km = aceptable
        (8.0, 12.0): 0.2,    # 8-12km = pobre
        (12.0, float('inf')): 0.05  # >12km = muy pobre
    }
    
    for (min_dist, max_dist), score in score_map.items():
        if min_dist <= distance_km < max_dist:
            return score * 0.35  # 35% del score total
    
    return 0.0
```

#### 2. Rating y Experiencia (25%):
```python
def calculate_driver_quality_score(driver: Driver) -> float:
    # Rating base (60% del factor)
    rating_score = (driver.rating / 5.0) * 0.6
    
    # Experiencia total (25% del factor)
    experience_months = driver.total_experience_months
    experience_score = min(experience_months / 24.0, 1.0) * 0.25  # Max a los 2 años
    
    # Viajes completados (15% del factor)
    trips_completed = driver.trips_completed
    trips_score = min(trips_completed / 1000.0, 1.0) * 0.15  # Max a los 1000 viajes
    
    total_quality_score = (rating_score + experience_score + trips_score) * 0.25
    return total_quality_score
```

#### 3. Conocimiento Local San Juan (20%):
```python
def calculate_local_knowledge_score(driver: Driver) -> float:
    # Tiempo trabajando en San Juan (50%)
    local_months = driver.san_juan_experience_months
    local_time_score = min(local_months / 18.0, 1.0) * 0.5  # Max a los 18 meses
    
    # Viajes completados en San Juan (30%)
    local_trips = driver.san_juan_trips_completed
    local_trips_score = min(local_trips / 500.0, 1.0) * 0.3  # Max a los 500 viajes
    
    # Rating específico para conocimiento de calles (20%)
    street_knowledge_rating = driver.street_knowledge_rating
    street_score = (street_knowledge_rating / 5.0) * 0.2
    
    total_local_score = (local_time_score + local_trips_score + street_score) * 0.20
    return total_local_score
```

#### 4. Disponibilidad y Patrones (15%):
```python
def calculate_availability_score(driver: Driver, current_time: datetime) -> float:
    # Tiempo desde última actividad (40%)
    last_active_minutes = (current_time - driver.last_active_time).total_seconds() / 60
    activity_score = max(0, (15 - last_active_minutes) / 15) * 0.4  # Lineal hasta 15 min
    
    # Patrón de aceptación de viajes (30%)
    acceptance_rate = driver.acceptance_rate
    acceptance_score = acceptance_rate * 0.3
    
    # Tasa de cancelación (20% - inverso)
    cancellation_rate = driver.cancellation_rate
    cancellation_score = max(0, (1 - cancellation_rate * 2)) * 0.2  # Penaliza >50%
    
    # Consistencia horaria (10%)
    is_typical_time = is_driver_typically_active(driver, current_time)
    consistency_score = (1.0 if is_typical_time else 0.5) * 0.1
    
    total_availability_score = (activity_score + acceptance_score + cancellation_score + consistency_score) * 0.15
    return total_availability_score
```

#### 5. Preferencias Usuario (5%):
```python
def calculate_preference_score(driver: Driver, user_prefs: UserPreferences) -> float:
    score = 0.0
    
    # Conductor favorito
    if driver.id in user_prefs.favorite_drivers:
        score += 0.4
    
    # Tipo de vehículo preferido
    if driver.vehicle_type in user_prefs.preferred_vehicle_types:
        score += 0.3
    
    # Género preferido (ej: conductoras mujeres)
    if user_prefs.preferred_gender and driver.gender == user_prefs.preferred_gender:
        score += 0.2
    
    # Idioma (ej: conductores que hablen inglés para turistas)
    if user_prefs.preferred_language in driver.languages:
        score += 0.1
    
    return min(score, 1.0) * 0.05
```

### ALGORITMO COMPLETO:
```python
def find_optimal_driver(
    passenger_location: LatLng,
    passenger_destination: LatLng,
    available_drivers: List[Driver],
    user_preferences: UserPreferences,
    urgency: UrgencyLevel = UrgencyLevel.NORMAL
) -> Optional[Driver]:
    
    if not available_drivers:
        return None
    
    scored_drivers = []
    
    for driver in available_drivers:
        # Filtros básicos
        if not is_driver_eligible(driver, passenger_location):
            continue
        
        # Cálculo de scores
        proximity_score = calculate_proximity_score(driver.location, passenger_location)
        quality_score = calculate_driver_quality_score(driver)
        local_score = calculate_local_knowledge_score(driver)
        availability_score = calculate_availability_score(driver, datetime.now())
        preference_score = calculate_preference_score(driver, user_preferences)
        
        # Score total
        total_score = proximity_score + quality_score + local_score + availability_score + preference_score
        
        # Ajuste por urgencia
        if urgency == UrgencyLevel.HIGH:
            total_score += proximity_score * 0.5  # Priorizar proximidad
        elif urgency == UrgencyLevel.LOW:
            total_score += quality_score * 0.3  # Priorizar calidad
        
        scored_drivers.append({
            'driver': driver,
            'total_score': total_score,
            'breakdown': {
                'proximity': proximity_score,
                'quality': quality_score,
                'local_knowledge': local_score,
                'availability': availability_score,
                'preferences': preference_score
            }
        })
    
    # Ordenar por score y retornar el mejor
    scored_drivers.sort(key=lambda x: x['total_score'], reverse=True)
    
    if scored_drivers:
        best_match = scored_drivers[0]
        logger.info(f"Best driver match: {best_match['driver'].id} with score {best_match['total_score']}")
        return best_match['driver']
    
    return None

def is_driver_eligible(driver: Driver, passenger_location: LatLng) -> bool:
    """Filtros básicos de elegibilidad."""
    return (
        driver.is_online and
        driver.is_available and
        not driver.is_in_trip and
        driver.vehicle_is_operational and
        haversine_distance(driver.location, passenger_location) <= 15.0  # Max 15km
    )
```

---

## ALGORITMO DE CÁLCULO DE TARIFAS

### SISTEMA DE ZONAS SAN JUAN:
```python
class SanJuanZoneSystem:
    """Sistema de zonas específico para San Juan con tarifas diferenciadas."""
    
    ZONES = {
        'CENTRO': {
            'boundaries': [(-31.525, -68.545), (-31.550, -68.525)],
            'base_fare': 400.0,
            'per_km_rate': 85.0,
            'per_minute_rate': 18.0,
            'surge_sensitivity': 1.0
        },
        'DESAMPARADOS': {
            'boundaries': [(-31.560, -68.560), (-31.580, -68.540)],
            'base_fare': 420.0,
            'per_km_rate': 90.0,
            'per_minute_rate': 20.0,
            'surge_sensitivity': 0.8
        },
        'RIVADAVIA': {
            'boundaries': [(-31.510, -68.570), (-31.530, -68.550)],
            'base_fare': 450.0,
            'per_km_rate': 95.0,
            'per_minute_rate': 22.0,
            'surge_sensitivity': 0.9
        },
        'CHIMBAS': {
            'boundaries': [(-31.480, -68.530), (-31.500, -68.510)],
            'base_fare': 480.0,
            'per_km_rate': 100.0,
            'per_minute_rate': 25.0,
            'surge_sensitivity': 0.7
        },
        'POCITO': {
            'boundaries': [(-31.620, -68.580), (-31.660, -68.560)],
            'base_fare': 520.0,
            'per_km_rate': 110.0,
            'per_minute_rate': 28.0,
            'surge_sensitivity': 0.6
        },
        'RAWSON': {
            'boundaries': [(-31.520, -68.585), (-31.540, -68.565)],
            'base_fare': 430.0,
            'per_km_rate': 88.0,
            'per_minute_rate': 19.0,
            'surge_sensitivity': 0.8
        },
        'SANTA_LUCIA': {
            'boundaries': [(-31.680, -68.620), (-31.720, -68.600)],
            'base_fare': 580.0,
            'per_km_rate': 125.0,
            'per_minute_rate': 32.0,
            'surge_sensitivity': 0.5
        },
        'SUBURBAN': {
            'base_fare': 650.0,
            'per_km_rate': 140.0,
            'per_minute_rate': 35.0,
            'surge_sensitivity': 0.4
        }
    }

def calculate_comprehensive_fare(
    pickup_location: LatLng,
    dropoff_location: LatLng,
    vehicle_type: VehicleType,
    current_demand: DemandLevel,
    trip_time: datetime,
    user_type: UserType = UserType.REGULAR,
    estimated_duration_minutes: int = None
) -> FareBreakdown:
    
    # 1. Determinar zonas
    pickup_zone = determine_zone(pickup_location)
    dropoff_zone = determine_zone(dropoff_location)
    
    # 2. Calcular distancia real usando Google Directions
    route_info = get_route_info(pickup_location, dropoff_location)
    distance_km = route_info.distance_km
    duration_minutes = estimated_duration_minutes or route_info.duration_minutes
    
    # 3. Tarifa base (promedio entre zonas)
    base_fare = (pickup_zone['base_fare'] + dropoff_zone['base_fare']) / 2
    
    # 4. Ajuste por tipo de vehículo
    vehicle_multiplier = get_vehicle_multiplier(vehicle_type)
    base_fare *= vehicle_multiplier
    
    # 5. Costo por distancia
    avg_per_km = (pickup_zone['per_km_rate'] + dropoff_zone['per_km_rate']) / 2
    distance_cost = distance_km * avg_per_km * vehicle_multiplier
    
    # 6. Costo por tiempo
    avg_per_minute = (pickup_zone['per_minute_rate'] + dropoff_zone['per_minute_rate']) / 2
    time_cost = duration_minutes * avg_per_minute * vehicle_multiplier
    
    # 7. Surge pricing controlado
    surge_multiplier = calculate_smart_surge(
        current_demand, pickup_zone, dropoff_zone, trip_time
    )
    
    # 8. Descuentos por tipo de usuario
    discount_multiplier = get_user_discount(user_type, trip_time)
    
    # 9. Cálculo final
    subtotal = base_fare + distance_cost + time_cost
    surge_amount = subtotal * (surge_multiplier - 1.0)
    total_before_discount = subtotal * surge_multiplier
    discount_amount = total_before_discount * (1.0 - discount_multiplier)
    final_total = total_before_discount * discount_multiplier
    
    return FareBreakdown(
        base_fare=base_fare,
        distance_cost=distance_cost,
        time_cost=time_cost,
        surge_multiplier=surge_multiplier,
        surge_amount=surge_amount,
        discount_multiplier=discount_multiplier,
        discount_amount=discount_amount,
        subtotal=subtotal,
        total=final_total,
        pickup_zone=pickup_zone['name'],
        dropoff_zone=dropoff_zone['name'],
        vehicle_type=vehicle_type,
        estimated_duration=duration_minutes,
        distance_km=distance_km
    )

def calculate_smart_surge(
    demand: DemandLevel, 
    pickup_zone: dict, 
    dropoff_zone: dict, 
    trip_time: datetime
) -> float:
    """Surge pricing inteligente y controlado para San Juan."""
    
    # Base surge según demanda
    base_surge = {
        DemandLevel.VERY_LOW: 0.85,    # Descuento en baja demanda
        DemandLevel.LOW: 0.95,         # Leve descuento
        DemandLevel.NORMAL: 1.0,       # Precio normal
        DemandLevel.HIGH: 1.15,        # Moderado aumento
        DemandLevel.VERY_HIGH: 1.35,   # Alto aumento
        DemandLevel.EXTREME: 1.5       # Máximo aumento (vs 3-5x Uber)
    }[demand]
    
    # Ajuste por sensibilidad de zona
    zone_sensitivity = (pickup_zone['surge_sensitivity'] + dropoff_zone['surge_sensitivity']) / 2
    adjusted_surge = 1.0 + (base_surge - 1.0) * zone_sensitivity
    
    # Ajuste por horario
    hour = trip_time.hour
    time_factor = 1.0
    if hour in [7, 8, 18, 19, 20]:  # Rush hours
        time_factor = 1.1
    elif hour in [22, 23, 0, 1, 2]:  # Vida nocturna
        time_factor = 1.05
    elif hour in [3, 4, 5, 6]:  # Madrugada
        time_factor = 0.95
    
    # Ajuste por día especial
    special_event_factor = check_special_events_surge(trip_time)
    
    final_surge = adjusted_surge * time_factor * special_event_factor
    
    # Límite máximo absoluto
    return min(final_surge, 1.5)

def get_user_discount(user_type: UserType, trip_time: datetime) -> float:
    """Descuentos específicos por tipo de usuario."""
    
    base_discount = {
        UserType.REGULAR: 1.0,
        UserType.STUDENT_UNSJ: 0.85,      # 15% descuento estudiantes
        UserType.SENIOR: 0.90,            # 10% descuento adultos mayores
        UserType.FREQUENT: 0.95,          # 5% descuento usuarios frecuentes
        UserType.PREMIUM: 1.0,            # Sin descuento, mejor servicio
        UserType.CORPORATE: 0.92          # 8% descuento corporativo
    }[user_type]
    
    # Descuento adicional para estudiantes en horario universitario
    if user_type == UserType.STUDENT_UNSJ:
        hour = trip_time.hour
        if 7 <= hour <= 22 and trip_time.weekday() < 5:  # Lun-Vie horario académico
            base_discount *= 0.95  # 5% adicional
    
    return base_discount

def get_vehicle_multiplier(vehicle_type: VehicleType) -> float:
    """Multiplicador por tipo de vehículo."""
    return {
        VehicleType.ECONOMY: 0.85,        # Vehículos básicos
        VehicleType.STANDARD: 1.0,        # Sedanes estándar
        VehicleType.COMFORT: 1.25,        # Vehículos cómodos/nuevos
        VehicleType.XL: 1.5,              # Vehículos grandes (6+ asientos)
        VehicleType.PREMIUM: 1.8,         # Vehículos de lujo
        VehicleType.ACCESSIBLE: 1.1       # Vehículos adaptados
    }[vehicle_type]
```

---

## ALGORITMO DE PREDICCIÓN ETA

### PREDICCIÓN INTELIGENTE PARA SAN JUAN:
```python
class SanJuanETAPredictor:
    """Predictor de ETA específico para patrones de tráfico de San Juan."""
    
    def __init__(self):
        self.traffic_patterns = self.load_san_juan_traffic_patterns()
        self.historical_data = self.load_historical_trip_data()
        
    def predict_eta(
        self,
        origin: LatLng,
        destination: LatLng,
        current_time: datetime,
        vehicle_type: VehicleType = VehicleType.STANDARD
    ) -> ETAResult:
        
        # 1. ETA base usando Google Directions
        google_eta = self.get_google_directions_eta(origin, destination)
        
        # 2. Factores específicos de San Juan
        traffic_factor = self.calculate_san_juan_traffic_factor(
            origin, destination, current_time
        )
        
        # 3. Factor por eventos especiales
        event_factor = self.check_special_events_impact(current_time)
        
        # 4. Factor por tipo de vehículo (capacidad de maniobra)
        vehicle_factor = self.get_vehicle_maneuverability_factor(vehicle_type)
        
        # 5. Factor por patrones históricos similares
        historical_factor = self.get_historical_adjustment(
            origin, destination, current_time
        )
        
        # 6. Cálculo final
        adjusted_eta = google_eta * traffic_factor * event_factor * vehicle_factor * historical_factor
        
        # 7. Confidence score basado en cantidad de datos históricos
        confidence = self.calculate_prediction_confidence(
            origin, destination, current_time
        )
        
        return ETAResult(
            estimated_minutes=int(adjusted_eta),
            confidence_score=confidence,
            google_base_eta=google_eta,
            traffic_factor=traffic_factor,
            event_factor=event_factor,
            vehicle_factor=vehicle_factor,
            historical_factor=historical_factor,
            route_description=self.generate_route_description(origin, destination)
        )
    
    def calculate_san_juan_traffic_factor(
        self, 
        origin: LatLng, 
        destination: LatLng, 
        current_time: datetime
    ) -> float:
        """Factor de tráfico específico para San Juan."""
        
        hour = current_time.hour
        day_of_week = current_time.weekday()
        
        # Patrones horarios específicos de San Juan
        hour_factors = {
            # Madrugada (muy fluido)
            range(0, 6): 0.8,
            # Rush mañana (pesado)
            range(7, 9): 1.4,
            # Mañana normal
            range(9, 12): 1.0,
            # Almuerzo (moderado)
            range(12, 14): 1.2,
            # Siesta (fluido)
            range(14, 17): 0.9,
            # Rush tarde (muy pesado)
            range(17, 20): 1.5,
            # Noche (normal)
            range(20, 24): 1.0
        }
        
        base_factor = 1.0
        for time_range, factor in hour_factors.items():
            if hour in time_range:
                base_factor = factor
                break
        
        # Ajuste por día de semana
        if day_of_week in [5, 6]:  # Sábado, Domingo
            base_factor *= 0.85  # Menos tráfico fines de semana
        
        # Rutas problemáticas conocidas
        if self.crosses_problematic_routes(origin, destination):
            base_factor *= 1.3
        
        # Zona centro (siempre más lento)
        if self.involves_centro_area(origin, destination):
            base_factor *= 1.2
        
        return base_factor
    
    def check_special_events_impact(self, current_time: datetime) -> float:
        """Impacto de eventos especiales en San Juan."""
        
        # Fiesta Nacional del Sol (Febrero)
        if current_time.month == 2 and current_time.day in range(1, 15):
            return 2.0  # Tráfico muy pesado
        
        # Vendimia (Marzo)
        if current_time.month == 3 and current_time.day in range(1, 31):
            return 1.4
        
        # Eventos UNSJ (período lectivo)
        if self.is_university_period(current_time):
            if current_time.hour in range(7, 22):
                return 1.1
        
        # Partidos San Martín (verificar calendario)
        if self.is_football_match_day(current_time):
            return 1.6
        
        return 1.0
    
    def get_historical_adjustment(
        self, 
        origin: LatLng, 
        destination: LatLng, 
        current_time: datetime
    ) -> float:
        """Ajuste basado en datos históricos de rutas similares."""
        
        similar_trips = self.find_similar_historical_trips(
            origin, destination, current_time
        )
        
        if len(similar_trips) < 5:
            return 1.0  # No hay suficientes datos
        
        # Calcular promedio de factor de desviación vs Google ETA
        avg_deviation = sum(trip.actual_time / trip.predicted_time for trip in similar_trips) / len(similar_trips)
        
        # Suavizar para evitar cambios extremos
        return 0.7 + 0.3 * avg_deviation
    
    def generate_route_description(self, origin: LatLng, destination: LatLng) -> str:
        """Genera descripción de ruta en términos sanjuaninos."""
        
        origin_ref = self.get_local_reference(origin)
        destination_ref = self.get_local_reference(destination)
        main_route = self.identify_main_route(origin, destination)
        
        return f"Desde {origin_ref} hasta {destination_ref} por {main_route}"
    
    def get_local_reference(self, location: LatLng) -> str:
        """Convierte coordenadas a referencias locales conocidas."""
        
        references = {
            'centro': (-31.5375, -68.5364),
            'hospital_rawson': (-31.5242, -68.5561),
            'unsj': (-31.5316, -68.5328),
            'shopping': (-31.5389, -68.5297),
            'aeropuerto': (-31.5712, -68.4182),
            'terminal': (-31.5447, -68.5392)
        }
        
        closest_ref = min(
            references.items(),
            key=lambda ref: haversine_distance(location, LatLng(ref[1][0], ref[1][1]))
        )
        
        distance = haversine_distance(location, LatLng(closest_ref[1][0], closest_ref[1][1]))
        
        if distance < 0.5:  # Menos de 500m
            return f"cerca de {closest_ref[0].replace('_', ' ')}"
        else:
            return f"zona {closest_ref[0].replace('_', ' ')}"
```

---

## ALGORITMO DE REBALANCEO DE FLOTA

### OPTIMIZACIÓN PROACTIVA DE CONDUCTORES:
```python
class FleetRebalancingSystem:
    """Sistema de rebalanceo inteligente para optimizar distribución de conductores."""
    
    def __init__(self):
        self.demand_predictor = DemandPredictor()
        self.incentive_calculator = IncentiveCalculator()
    
    def calculate_optimal_distribution(
        self,
        current_drivers: List[Driver],
        predicted_demand: Dict[str, DemandForecast],
        time_horizon_minutes: int = 30
    ) -> List[RebalancingAction]:
        
        actions = []
        
        # 1. Analizar distribución actual vs demanda predicha
        supply_demand_analysis = self.analyze_supply_demand_gaps(
            current_drivers, predicted_demand
        )
        
        # 2. Identificar zonas con déficit y exceso
        deficit_zones = [zone for zone, gap in supply_demand_analysis.items() if gap < -2]
        excess_zones = [zone for zone, gap in supply_demand_analysis.items() if gap > 2]
        
        # 3. Crear acciones de rebalanceo
        for deficit_zone in deficit_zones:
            needed_drivers = abs(supply_demand_analysis[deficit_zone])
            
            # Encontrar conductores cercanos que puedan moverse
            candidate_drivers = self.find_rebalancing_candidates(
                deficit_zone, current_drivers, excess_zones
            )
            
            for i, driver in enumerate(candidate_drivers[:needed_drivers]):
                incentive = self.calculate_movement_incentive(
                    driver.location, deficit_zone, predicted_demand[deficit_zone]
                )
                
                actions.append(RebalancingAction(
                    driver_id=driver.id,
                    target_zone=deficit_zone,
                    incentive_amount=incentive,
                    estimated_revenue_increase=self.estimate_revenue_increase(
                        driver, deficit_zone, time_horizon_minutes
                    ),
                    priority=self.calculate_action_priority(deficit_zone, needed_drivers - i)
                ))
        
        return sorted(actions, key=lambda x: x.priority, reverse=True)
    
    def analyze_supply_demand_gaps(
        self,
        current_drivers: List[Driver],
        predicted_demand: Dict[str, DemandForecast]
    ) -> Dict[str, int]:
        
        # Contar drivers por zona
        drivers_per_zone = defaultdict(int)
        for driver in current_drivers:
            if driver.is_available:
                zone = determine_zone(driver.location)
                drivers_per_zone[zone] += 1
        
        # Calcular gaps
        gaps = {}
        for zone, demand_forecast in predicted_demand.items():
            current_supply = drivers_per_zone.get(zone, 0)
            predicted_need = demand_forecast.expected_trips_next_30min
            optimal_supply = max(predicted_need * 1.2, 2)  # 20% buffer, mín 2 drivers
            
            gaps[zone] = current_supply - optimal_supply
        
        return gaps
    
    def calculate_movement_incentive(
        self,
        driver_location: LatLng,
        target_zone: str,
        demand_forecast: DemandForecast
    ) -> float:
        
        # Distancia a recorrer
        zone_center = get_zone_center(target_zone)
        distance_km = haversine_distance(driver_location, zone_center)
        
        # Incentivo base por distancia
        base_incentive = distance_km * 50  # $50 ARS por km
        
        # Multiplicador por urgencia de demanda
        urgency_multiplier = min(demand_forecast.urgency_level / 5.0, 2.0)
        
        # Multiplicador por probabilidad de obtener viaje
        trip_probability = min(demand_forecast.expected_trips_next_30min / 3.0, 1.0)
        
        total_incentive = base_incentive * urgency_multiplier * trip_probability
        
        return min(total_incentive, 500)  # Cap máximo $500 ARS

class DemandPredictor:
    """Predictor de demanda específico para San Juan."""
    
    def predict_demand_next_30_minutes(
        self,
        current_time: datetime,
        weather: WeatherCondition,
        special_events: List[Event]
    ) -> Dict[str, DemandForecast]:
        
        predictions = {}
        
        for zone in SanJuanZoneSystem.ZONES.keys():
            # Base histórica
            historical_demand = self.get_historical_demand(zone, current_time)
            
            # Ajuste por clima
            weather_factor = self.get_weather_impact_factor(weather)
            
            # Ajuste por eventos
            event_factor = self.get_events_impact_factor(special_events, zone)
            
            # Ajuste por tendencias recientes
            trend_factor = self.get_recent_trend_factor(zone)
            
            adjusted_demand = historical_demand * weather_factor * event_factor * trend_factor
            
            predictions[zone] = DemandForecast(
                zone=zone,
                expected_trips_next_30min=int(adjusted_demand),
                confidence_score=self.calculate_confidence(zone, current_time),
                urgency_level=self.calculate_urgency(adjusted_demand, zone),
                peak_time_estimate=self.estimate_peak_time(zone, current_time)
            )
        
        return predictions

@dataclass
class RebalancingAction:
    driver_id: str
    target_zone: str
    incentive_amount: float
    estimated_revenue_increase: float
    priority: int
    expires_at: datetime = field(default_factory=lambda: datetime.now() + timedelta(minutes=10))

@dataclass
class DemandForecast:
    zone: str
    expected_trips_next_30min: int
    confidence_score: float
    urgency_level: int  # 1-10
    peak_time_estimate: Optional[datetime]
```

---

## ALGORITMO DE DETECCIÓN DE FRAUDE

### SISTEMA ANTI-FRAUDE PARA SAN JUAN:
```python
class FraudDetectionSystem:
    """Sistema de detección de fraude específico para San Juan Ride."""
    
    def __init__(self):
        self.ml_model = self.load_fraud_detection_model()
        self.rule_engine = FraudRuleEngine()
    
    def analyze_trip_for_fraud(self, trip: Trip, real_time: bool = True) -> FraudAnalysis:
        """Análisis completo de fraude para un viaje."""
        
        # 1. Análisis basado en reglas
        rule_based_score = self.rule_engine.calculate_fraud_score(trip)
        
        # 2. Análisis ML (si está disponible)
        ml_score = self.ml_model.predict_fraud_probability(trip) if self.ml_model else 0.0
        
        # 3. Análisis de patrones geográficos San Juan
        geo_score = self.analyze_geographical_patterns(trip)
        
        # 4. Análisis temporal
        temporal_score = self.analyze_temporal_patterns(trip)
        
        # 5. Análisis de comportamiento usuario/conductor
        behavior_score = self.analyze_behavior_patterns(trip)
        
        # 6. Score compuesto
        composite_score = (
            rule_based_score * 0.4 +
            ml_score * 0.3 +
            geo_score * 0.15 +
            temporal_score * 0.1 +
            behavior_score * 0.05
        )
        
        risk_level = self.classify_risk_level(composite_score)
        
        return FraudAnalysis(
            trip_id=trip.id,
            fraud_score=composite_score,
            risk_level=risk_level,
            rule_violations=self.rule_engine.get_violations(trip),
            suspicious_patterns=self.identify_suspicious_patterns(trip),
            recommended_actions=self.get_recommended_actions(risk_level),
            requires_manual_review=composite_score > 0.7,
            analysis_timestamp=datetime.now()
        )
```

---

## MÉTRICAS Y KPIs ESPECÍFICOS PARA SAN JUAN

### MÉTRICAS DE PERFORMANCE ALGORITMOS:
```python
class AlgorithmMetrics:
    """Métricas específicas para monitorear performance de algoritmos."""
    
    # Matching Algorithm
    MATCHING_SUCCESS_RATE_TARGET = 0.95    # 95% de requests deben encontrar conductor
    MATCHING_TIME_TARGET = 5.0             # 5 segundos máximo
    MATCHING_ACCURACY_TARGET = 0.85        # 85% usuarios satisfechos con match
    
    # Fare Calculation
    FARE_PREDICTION_ACCURACY = 0.90        # 90% estimaciones dentro del 10% real
    SURGE_CUSTOMER_SATISFACTION = 0.75     # 75% aceptación del surge pricing
    
    # ETA Prediction  
    ETA_ACCURACY_TARGET = 0.80             # 80% predicciones dentro del 20% real
    ETA_CONFIDENCE_TARGET = 0.70           # 70% confianza promedio
    
    # Fraud Detection
    FRAUD_DETECTION_PRECISION = 0.85       # 85% de alerts son fraude real
    FRAUD_DETECTION_RECALL = 0.75          # 75% de fraudes son detectados
    FALSE_POSITIVE_RATE_MAX = 0.05         # Máximo 5% falsos positivos
    
    # Fleet Rebalancing
    REBALANCING_EFFECTIVENESS = 0.60       # 60% conductores siguen sugerencias
    REVENUE_INCREASE_TARGET = 0.15         # 15% aumento revenue por rebalancing
```

---

## CONFIGURACIÓN Y TUNNING PARA SAN JUAN

### PARÁMETROS ESPECÍFICOS DEL MERCADO:
```python
class SanJuanAlgorithmConfig:
    """Configuración específica de algoritmos para el mercado de San Juan."""
    
    # Matching Algorithm Tuning
    MATCHING_CONFIG = {
        'max_search_radius_km': 15.0,        # Radio máximo búsqueda
        'proximity_weight': 0.35,            # Peso proximidad
        'rating_weight': 0.25,               # Peso rating
        'local_knowledge_weight': 0.20,      # Peso conocimiento local
        'availability_weight': 0.15,         # Peso disponibilidad
        'preference_weight': 0.05,           # Peso preferencias usuario
        'max_matching_time_seconds': 5,      # Tiempo máximo matching
        'fallback_to_further_drivers': True, # Buscar más lejos si no hay cerca
        'prefer_local_drivers': True         # Preferir conductores locales
    }
    
    # Fare Calculation Tuning
    FARE_CONFIG = {
        'base_fare_centro': 400.0,           # Tarifa base centro
        'per_km_rate_standard': 85.0,        # Tarifa por km estándar
        'per_minute_rate': 18.0,             # Tarifa por minuto
        'max_surge_multiplier': 1.5,         # Surge máximo vs 3x Uber
        'student_discount': 0.15,            # 15% descuento estudiantes
        'senior_discount': 0.10,             # 10% descuento adultos mayores
        'frequent_user_discount': 0.05,      # 5% descuento usuarios frecuentes
        'minimum_fare': 300.0,               # Tarifa mínima
        'airport_surcharge': 100.0           # Recargo aeropuerto
    }
    
    # ETA Prediction Tuning
    ETA_CONFIG = {
        'google_api_weight': 0.60,           # Peso predicción Google
        'historical_data_weight': 0.25,      # Peso datos históricos
        'real_time_traffic_weight': 0.15,    # Peso tráfico tiempo real
        'san_juan_traffic_patterns': {       # Patrones específicos SJ
            'rush_morning': (7, 9, 1.4),
            'lunch_time': (12, 14, 1.2),
            'rush_evening': (17, 20, 1.5),
            'night_time': (22, 6, 0.8)
        },
        'confidence_threshold': 0.70,        # Umbral confianza mínima
        'max_prediction_horizon_minutes': 60 # Máximo tiempo predicción
    }
    
    # Fraud Detection Tuning
    FRAUD_CONFIG = {
        'rule_based_weight': 0.40,           # Peso reglas de negocio
        'ml_model_weight': 0.30,             # Peso modelo ML
        'geo_analysis_weight': 0.15,         # Peso análisis geográfico
        'temporal_analysis_weight': 0.10,    # Peso análisis temporal
        'behavior_analysis_weight': 0.05,    # Peso análisis comportamiento
        'auto_block_threshold': 0.85,        # Bloqueo automático
        'manual_review_threshold': 0.70,     # Revisión manual
        'alert_threshold': 0.50,             # Alerta básica
        'max_speed_kmh_city': 80,            # Velocidad máxima ciudad
        'max_location_jump_km': 1.0,         # Salto ubicación máximo
        'suspicious_circle_ratio': 0.3       # Ratio ruta circular sospechosa
    }

# Función para ajustar parámetros dinámicamente
def tune_algorithms_for_san_juan_conditions(
    current_performance: Dict[str, float],
    market_conditions: MarketConditions
) -> Dict[str, Any]:
    """Ajuste dinámico de parámetros basado en condiciones del mercado."""
    
    adjustments = {}
    
    # Ajustar matching si hay problemas de tiempo de respuesta
    if current_performance.get('avg_matching_time', 0) > 5.0:
        adjustments['matching_max_radius'] = min(
            SanJuanAlgorithmConfig.MATCHING_CONFIG['max_search_radius_km'] * 0.8,
            10.0
        )
        adjustments['matching_proximity_weight'] = 0.45  # Más peso a proximidad
    
    # Ajustar surge si hay quejas de usuarios
    if market_conditions.user_satisfaction < 0.75:
        adjustments['max_surge_multiplier'] = min(
            SanJuanAlgorithmConfig.FARE_CONFIG['max_surge_multiplier'] * 0.9,
            1.3
        )
    
    # Ajustar ETA si hay baja precisión
    if current_performance.get('eta_accuracy', 0) < 0.80:
        adjustments['eta_google_weight'] = 0.70  # Más peso a Google
        adjustments['eta_historical_weight'] = 0.20
    
    return adjustments
```

**ESTE DOCUMENTO CONTIENE LAS ESPECIFICACIONES COMPLETAS DE TODOS LOS ALGORITMOS CRÍTICOS PARA SAN JUAN RIDE. CADA ALGORITMO ESTÁ OPTIMIZADO ESPECÍFICAMENTE PARA LAS CARACTERÍSTICAS DEL MERCADO SANJUANINO Y DEBE SER IMPLEMENTADO CON ESTOS PARÁMETROS PARA ASEGURAR COMPETITIVIDAD CONTRA UBER Y DIDI.**