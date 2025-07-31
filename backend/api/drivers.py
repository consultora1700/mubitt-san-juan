from fastapi import APIRouter, Depends, HTTPException, status
from typing import List
import uuid
from datetime import datetime, timedelta
import random

from models.driver import DriverCreate, DriverResponse, DriverLocationUpdate
from api.auth import get_current_user

router = APIRouter(prefix="/drivers", tags=["Drivers"])

@router.post("/register", response_model=DriverResponse)
async def register_driver(
    driver_data: DriverCreate,
    current_user = Depends(get_current_user)
):
    """Register as a driver."""
    
    driver_id = str(uuid.uuid4())
    
    # Mock driver registration
    driver = DriverResponse(
        id=driver_id,
        user_id=current_user["id"],
        license_number=driver_data.license_number,
        vehicle_make=driver_data.vehicle_make,
        vehicle_model=driver_data.vehicle_model,
        vehicle_color=driver_data.vehicle_color,
        vehicle_year=driver_data.vehicle_year,
        license_plate=driver_data.license_plate,
        rating=5.0,
        trip_count=0,
        is_active=False,  # Needs verification first
        is_verified=False,
        current_latitude=None,
        current_longitude=None,
        created_at=datetime.utcnow()
    )
    
    return driver

@router.get("/profile", response_model=DriverResponse)
async def get_driver_profile(current_user = Depends(get_current_user)):
    """Get driver profile."""
    
    # Mock driver profile
    driver = DriverResponse(
        id=str(uuid.uuid4()),
        user_id=current_user["id"],
        license_number="SJ123456",
        vehicle_make="Toyota",
        vehicle_model="Corolla",
        vehicle_color="Blanco",
        vehicle_year=2020,
        license_plate="SJU 123",
        rating=4.8,
        trip_count=145,
        is_active=True,
        is_verified=True,
        current_latitude=-31.5375,
        current_longitude=-68.5364,
        created_at=datetime.utcnow() - timedelta(days=180)
    )
    
    return driver

@router.put("/location")
async def update_location(
    location_data: DriverLocationUpdate,
    current_user = Depends(get_current_user)
):
    """Update driver's current location."""
    
    # Validate San Juan coordinates (rough bounds)
    if not (-32.0 <= location_data.latitude <= -31.0):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Location outside San Juan area"
        )
    
    if not (-69.0 <= location_data.longitude <= -68.0):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Location outside San Juan area"
        )
    
    return {
        "message": "Location updated successfully",
        "latitude": location_data.latitude,
        "longitude": location_data.longitude,
        "updated_at": datetime.utcnow()
    }

@router.put("/status")
async def toggle_driver_status(
    is_active: bool,
    current_user = Depends(get_current_user)
):
    """Toggle driver online/offline status."""
    
    status_text = "online" if is_active else "offline"
    
    return {
        "message": f"Driver status changed to {status_text}",
        "is_active": is_active,
        "updated_at": datetime.utcnow()
    }

@router.get("/earnings")
async def get_driver_earnings(
    current_user = Depends(get_current_user),
    days: int = 7
):
    """Get driver earnings summary."""
    
    # Mock earnings data
    daily_earnings = []
    total_earnings = 0
    
    for i in range(days):
        daily_amount = random.uniform(800, 2500) if random.random() > 0.2 else 0
        total_earnings += daily_amount
        
        daily_earnings.append({
            "date": (datetime.utcnow() - timedelta(days=i)).strftime("%Y-%m-%d"),
            "trips": random.randint(0, 12) if daily_amount > 0 else 0,
            "earnings": round(daily_amount, 2),
            "hours_online": random.uniform(2, 10) if daily_amount > 0 else 0
        })
    
    return {
        "period_days": days,
        "total_earnings": round(total_earnings, 2),
        "average_per_day": round(total_earnings / days, 2),
        "total_trips": sum(day["trips"] for day in daily_earnings),
        "daily_breakdown": daily_earnings[::-1]  # Reverse to show oldest first
    }

@router.get("/trips/active")
async def get_active_trip(current_user = Depends(get_current_user)):
    """Get driver's currently active trip."""
    
    # Mock active trip
    if random.random() > 0.7:  # 30% chance of having active trip
        return {
            "trip_id": str(uuid.uuid4()),
            "passenger_name": "María González",
            "pickup_address": "Hospital Rawson, San Juan",
            "dropoff_address": "UNSJ, San Juan",
            "status": "driver_arriving",
            "estimated_fare": 850.50,
            "distance_to_pickup": 1.2,
            "eta_to_pickup": 4
        }
    else:
        return None

@router.put("/trips/{trip_id}/accept")
async def accept_trip(
    trip_id: str,
    current_user = Depends(get_current_user)
):
    """Accept a trip request."""
    
    return {
        "message": "Trip accepted successfully",
        "trip_id": trip_id,
        "accepted_at": datetime.utcnow()
    }

@router.put("/trips/{trip_id}/arrive")
async def arrive_at_pickup(
    trip_id: str,
    current_user = Depends(get_current_user)
):
    """Mark arrival at pickup location."""
    
    return {
        "message": "Arrived at pickup location",
        "trip_id": trip_id,
        "arrived_at": datetime.utcnow()
    }

@router.put("/trips/{trip_id}/start")
async def start_trip(
    trip_id: str,
    current_user = Depends(get_current_user)
):
    """Start the trip."""
    
    return {
        "message": "Trip started successfully",
        "trip_id": trip_id,
        "started_at": datetime.utcnow()
    }

@router.put("/trips/{trip_id}/complete")
async def complete_trip(
    trip_id: str,
    final_fare: float,
    current_user = Depends(get_current_user)
):
    """Complete the trip."""
    
    return {
        "message": "Trip completed successfully",
        "trip_id": trip_id,
        "final_fare": final_fare,
        "completed_at": datetime.utcnow()
    }