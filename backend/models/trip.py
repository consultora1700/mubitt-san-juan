from sqlalchemy import Column, String, Float, Integer, DateTime, Text, ForeignKey, Enum
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
from datetime import datetime
from pydantic import BaseModel
from typing import Optional
import enum

Base = declarative_base()

class TripStatus(enum.Enum):
    PENDING = "pending"
    DRIVER_ASSIGNED = "driver_assigned"
    DRIVER_ARRIVING = "driver_arriving"
    IN_PROGRESS = "in_progress"
    COMPLETED = "completed"
    CANCELLED = "cancelled"

class VehicleType(enum.Enum):
    ECONOMY = "economy"
    COMFORT = "comfort"
    XL = "xl"

class Location(Base):
    __tablename__ = "locations"
    
    id = Column(String, primary_key=True)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    address = Column(String(500), nullable=False)
    reference = Column(String(200), nullable=True)  # Referencias San Juan
    postal_code = Column(String(10), nullable=True)

class Trip(Base):
    __tablename__ = "trips"
    
    id = Column(String, primary_key=True)
    passenger_id = Column(String, ForeignKey('users.id'), nullable=False)
    driver_id = Column(String, ForeignKey('drivers.id'), nullable=True)
    pickup_location_id = Column(String, ForeignKey('locations.id'), nullable=False)
    dropoff_location_id = Column(String, ForeignKey('locations.id'), nullable=False)
    status = Column(Enum(TripStatus), default=TripStatus.PENDING)
    vehicle_type = Column(Enum(VehicleType), nullable=False)
    estimated_fare = Column(Float, nullable=False)
    actual_fare = Column(Float, nullable=True)
    distance = Column(Float, nullable=False)  # km
    estimated_duration = Column(Integer, nullable=False)  # minutes
    actual_duration = Column(Integer, nullable=True)
    scheduled_time = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    started_at = Column(DateTime, nullable=True)
    completed_at = Column(DateTime, nullable=True)
    cancelled_at = Column(DateTime, nullable=True)
    rating = Column(Integer, nullable=True)
    feedback = Column(Text, nullable=True)
    notes = Column(Text, nullable=True)
    payment_method_id = Column(String, nullable=False)
    
    # Relationships
    pickup_location = relationship("Location", foreign_keys=[pickup_location_id])
    dropoff_location = relationship("Location", foreign_keys=[dropoff_location_id])

# Pydantic models
class LocationModel(BaseModel):
    latitude: float
    longitude: float
    address: str
    reference: Optional[str] = None
    postal_code: Optional[str] = None

class TripCreate(BaseModel):
    pickup_location: LocationModel
    dropoff_location: LocationModel
    vehicle_type: str
    scheduled_time: Optional[datetime] = None
    notes: Optional[str] = None
    payment_method_id: str

class TripSearch(BaseModel):
    pickup_location: LocationModel
    radius: float = 5.0
    vehicle_type: str

class VehicleInfo(BaseModel):
    make: str
    model: str
    color: str
    license_plate: str
    year: int

class DriverMatch(BaseModel):
    driver_id: str
    name: str
    rating: float
    vehicle_info: VehicleInfo
    location: LocationModel
    estimated_arrival: int  # minutes
    distance: float  # km to pickup

class FareEstimate(BaseModel):
    base_fare: float
    distance_fare: float
    time_fare: float
    surge_factor: float
    total_fare: float
    currency: str = "ARS"

class TripResponse(BaseModel):
    id: str
    passenger_id: str
    driver_id: Optional[str]
    pickup_location: LocationModel
    dropoff_location: LocationModel
    status: str
    vehicle_type: str
    estimated_fare: float
    actual_fare: Optional[float]
    distance: float
    estimated_duration: int
    actual_duration: Optional[int]
    scheduled_time: Optional[datetime]
    created_at: datetime
    started_at: Optional[datetime]
    completed_at: Optional[datetime]
    cancelled_at: Optional[datetime]
    rating: Optional[int]
    feedback: Optional[str]

    class Config:
        from_attributes = True