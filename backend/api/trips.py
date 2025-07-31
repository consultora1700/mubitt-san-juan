from fastapi import APIRouter, Depends, HTTPException, status
from typing import List
import uuid
from datetime import datetime, timedelta
import random

from models.trip import (
    TripCreate, TripSearch, TripResponse, LocationModel, 
    DriverMatch, FareEstimate, VehicleInfo
)
from api.auth import get_current_user

router = APIRouter(prefix="/trips", tags=["Trips"])

# Mock data for San Juan references
SAN_JUAN_LOCATIONS = [
    {
        "name": "Hospital Rawson",
        "latitude": -31.5375,
        "longitude": -68.5364,
        "address": "Av. Ignacio de la Roza 130, San Juan"
    },
    {
        "name": "Universidad Nacional de San Juan (UNSJ)",
        "latitude": -31.5441,
        "longitude": -68.5504,
        "address": "Av. Libertador San Martín, San Juan"
    },
    {
        "name": "Plaza 25 de Mayo",
        "latitude": -31.5375,
        "longitude": -68.5289,
        "address": "Plaza 25 de Mayo, Centro, San Juan"
    },
    {
        "name": "Shopping del Sol",
        "latitude": -31.5203,
        "longitude": -68.5289,
        "address": "Av. José Ignacio de la Roza, San Juan"
    }
]

def calculate_fare_san_juan(distance_km: float, duration_minutes: int, vehicle_type: str) -> FareEstimate:
    """Calculate fare using San Juan specific algorithm."""
    
    # Base fares by vehicle type (ARS)
    base_fares = {
        "economy": 400.0,
        "comfort": 500.0,
        "xl": 650.0
    }
    
    # Per km rate (ARS)
    per_km_rate = 120.0
    
    # Per minute rate (ARS)
    per_minute_rate = 18.0
    
    # Surge factor (max 1.5x vs 3x of Uber)
    surge_factor = random.uniform(1.0, 1.5)
    
    base_fare = base_fares.get(vehicle_type.lower(), 400.0)
    distance_fare = distance_km * per_km_rate
    time_fare = duration_minutes * per_minute_rate
    
    subtotal = base_fare + distance_fare + time_fare
    total_fare = subtotal * surge_factor
    
    return FareEstimate(
        base_fare=base_fare,
        distance_fare=distance_fare,
        time_fare=time_fare,
        surge_factor=surge_factor,
        total_fare=round(total_fare, 2)
    )

def find_nearby_drivers(pickup_location: LocationModel, radius_km: float = 5.0) -> List[DriverMatch]:
    """Find nearby drivers in San Juan."""
    
    # Mock drivers data
    mock_drivers = [
        {
            "driver_id": str(uuid.uuid4()),
            "name": "Carlos Rodríguez",
            "rating": 4.8,
            "vehicle_info": VehicleInfo(
                make="Toyota",
                model="Corolla",
                color="Blanco",
                license_plate="SJU 123",
                year=2020
            ),
            "distance": random.uniform(0.5, 3.0),
            "estimated_arrival": random.randint(3, 12)
        },
        {
            "driver_id": str(uuid.uuid4()),
            "name": "María González",
            "rating": 4.9,
            "vehicle_info": VehicleInfo(
                make="Chevrolet",
                model="Onix",
                color="Gris",
                license_plate="SJU 456",
                year=2021
            ),
            "distance": random.uniform(0.3, 2.5),
            "estimated_arrival": random.randint(2, 8)
        },
        {
            "driver_id": str(uuid.uuid4()),
            "name": "Juan Martínez",
            "rating": 4.7,
            "vehicle_info": VehicleInfo(
                make="Ford",
                model="Ka",
                color="Azul",
                license_plate="SJU 789",
                year=2019
            ),
            "distance": random.uniform(1.0, 4.0),
            "estimated_arrival": random.randint(5, 15)
        }
    ]
    
    drivers = []
    for driver_data in mock_drivers:
        # Create mock location near pickup
        lat_offset = random.uniform(-0.01, 0.01)
        lng_offset = random.uniform(-0.01, 0.01)
        
        drivers.append(DriverMatch(
            driver_id=driver_data["driver_id"],
            name=driver_data["name"],
            rating=driver_data["rating"],
            vehicle_info=driver_data["vehicle_info"],
            location=LocationModel(
                latitude=pickup_location.latitude + lat_offset,
                longitude=pickup_location.longitude + lng_offset,
                address=f"Ubicación actual - {driver_data['name']}"
            ),
            estimated_arrival=driver_data["estimated_arrival"],
            distance=driver_data["distance"]
        ))
    
    return drivers

@router.post("/estimate-fare", response_model=FareEstimate)
async def estimate_fare(
    pickup_location: LocationModel,
    dropoff_location: LocationModel,
    vehicle_type: str = "economy"
):
    """Estimate fare for a trip in San Juan."""
    
    # Calculate distance (mock calculation)
    # In real app, use Google Maps Distance Matrix API
    lat_diff = abs(pickup_location.latitude - dropoff_location.latitude)
    lng_diff = abs(pickup_location.longitude - dropoff_location.longitude)
    distance_km = ((lat_diff ** 2 + lng_diff ** 2) ** 0.5) * 111  # Rough conversion
    distance_km = max(distance_km, 1.0)  # Minimum 1km
    
    # Estimate duration (assume 30 km/h average in San Juan)
    duration_minutes = int((distance_km / 30) * 60)
    duration_minutes = max(duration_minutes, 5)  # Minimum 5 minutes
    
    return calculate_fare_san_juan(distance_km, duration_minutes, vehicle_type)

@router.post("/search-drivers", response_model=List[DriverMatch])
async def search_drivers(search_data: TripSearch):
    """Search for available drivers near pickup location."""
    
    return find_nearby_drivers(search_data.pickup_location, search_data.radius)

@router.post("/create", response_model=TripResponse)
async def create_trip(
    trip_data: TripCreate,
    current_user = Depends(get_current_user)
):
    """Create a new trip request."""
    
    trip_id = str(uuid.uuid4())
    
    # Calculate fare
    fare_estimate = await estimate_fare(
        trip_data.pickup_location,
        trip_data.dropoff_location,
        trip_data.vehicle_type
    )
    
    # Create trip response
    trip = TripResponse(
        id=trip_id,
        passenger_id=current_user["id"],
        driver_id=None,  # Will be assigned when driver accepts
        pickup_location=trip_data.pickup_location,
        dropoff_location=trip_data.dropoff_location,
        status="pending",
        vehicle_type=trip_data.vehicle_type,
        estimated_fare=fare_estimate.total_fare,
        actual_fare=None,
        distance=5.2,  # Mock distance
        estimated_duration=15,  # Mock duration
        actual_duration=None,
        scheduled_time=trip_data.scheduled_time,
        created_at=datetime.utcnow(),
        started_at=None,
        completed_at=None,
        cancelled_at=None,
        rating=None,
        feedback=None
    )
    
    return trip

@router.get("/{trip_id}", response_model=TripResponse)
async def get_trip(
    trip_id: str,
    current_user = Depends(get_current_user)
):
    """Get trip details by ID."""
    
    # Mock trip data
    trip = TripResponse(
        id=trip_id,
        passenger_id=current_user["id"],
        driver_id=str(uuid.uuid4()),
        pickup_location=LocationModel(
            latitude=-31.5375,
            longitude=-68.5364,
            address="Hospital Rawson, San Juan",
            reference="Frente al hospital"
        ),
        dropoff_location=LocationModel(
            latitude=-31.5441,
            longitude=-68.5504,
            address="UNSJ, San Juan",
            reference="Campus universitario"
        ),
        status="driver_assigned",
        vehicle_type="economy",
        estimated_fare=850.50,
        actual_fare=None,
        distance=3.2,
        estimated_duration=12,
        actual_duration=None,
        scheduled_time=None,
        created_at=datetime.utcnow() - timedelta(minutes=5),
        started_at=None,
        completed_at=None,
        cancelled_at=None,
        rating=None,
        feedback=None
    )
    
    return trip

@router.get("/", response_model=List[TripResponse])
async def get_user_trips(
    current_user = Depends(get_current_user),
    limit: int = 10,
    offset: int = 0
):
    """Get user's trip history."""
    
    # Mock trip history
    trips = []
    for i in range(min(limit, 5)):  # Return up to 5 mock trips
        trip = TripResponse(
            id=str(uuid.uuid4()),
            passenger_id=current_user["id"],
            driver_id=str(uuid.uuid4()),
            pickup_location=LocationModel(
                latitude=-31.5375 + random.uniform(-0.01, 0.01),
                longitude=-68.5364 + random.uniform(-0.01, 0.01),
                address=f"Ubicación {i+1}, San Juan"
            ),
            dropoff_location=LocationModel(
                latitude=-31.5441 + random.uniform(-0.01, 0.01),
                longitude=-68.5504 + random.uniform(-0.01, 0.01),
                address=f"Destino {i+1}, San Juan"
            ),
            status="completed" if i > 0 else "in_progress",
            vehicle_type=random.choice(["economy", "comfort", "xl"]),
            estimated_fare=random.uniform(400, 1200),
            actual_fare=random.uniform(400, 1200) if i > 0 else None,
            distance=random.uniform(1.0, 8.0),
            estimated_duration=random.randint(5, 25),
            actual_duration=random.randint(5, 30) if i > 0 else None,
            scheduled_time=None,
            created_at=datetime.utcnow() - timedelta(days=i+1),
            started_at=datetime.utcnow() - timedelta(days=i+1, minutes=5) if i > 0 else None,
            completed_at=datetime.utcnow() - timedelta(days=i+1, minutes=20) if i > 0 else None,
            cancelled_at=None,
            rating=random.randint(4, 5) if i > 0 else None,
            feedback="Excelente servicio" if i > 0 else None
        )
        trips.append(trip)
    
    return trips

@router.put("/{trip_id}/cancel")
async def cancel_trip(
    trip_id: str,
    current_user = Depends(get_current_user)
):
    """Cancel a trip."""
    
    return {
        "message": "Trip cancelled successfully",
        "trip_id": trip_id,
        "cancelled_at": datetime.utcnow()
    }

@router.put("/{trip_id}/rate")
async def rate_trip(
    trip_id: str,
    rating: int,
    feedback: str = None,
    current_user = Depends(get_current_user)
):
    """Rate a completed trip."""
    
    if rating < 1 or rating > 5:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Rating must be between 1 and 5"
        )
    
    return {
        "message": "Trip rated successfully",
        "trip_id": trip_id,
        "rating": rating,
        "feedback": feedback
    }