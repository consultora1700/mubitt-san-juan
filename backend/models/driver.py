from sqlalchemy import Column, String, Float, Integer, Boolean, DateTime, Text, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
from datetime import datetime
from pydantic import BaseModel
from typing import Optional

Base = declarative_base()

class Driver(Base):
    __tablename__ = "drivers"
    
    id = Column(String, primary_key=True)
    user_id = Column(String, ForeignKey('users.id'), nullable=False)
    license_number = Column(String(50), unique=True, nullable=False)
    vehicle_make = Column(String(50), nullable=False)
    vehicle_model = Column(String(50), nullable=False)
    vehicle_color = Column(String(30), nullable=False)
    vehicle_year = Column(Integer, nullable=False)
    license_plate = Column(String(20), unique=True, nullable=False)
    rating = Column(Float, default=5.0)
    trip_count = Column(Integer, default=0)
    is_active = Column(Boolean, default=False)
    is_verified = Column(Boolean, default=False)
    current_latitude = Column(Float, nullable=True)
    current_longitude = Column(Float, nullable=True)
    last_location_update = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    
class DriverDocument(Base):
    __tablename__ = "driver_documents"
    
    id = Column(String, primary_key=True)
    driver_id = Column(String, ForeignKey('drivers.id'), nullable=False)
    document_type = Column(String(50), nullable=False)  # license, insurance, registration
    document_url = Column(String(500), nullable=False)
    is_verified = Column(Boolean, default=False)
    uploaded_at = Column(DateTime, default=datetime.utcnow)

# Pydantic models
class DriverCreate(BaseModel):
    license_number: str
    vehicle_make: str
    vehicle_model: str
    vehicle_color: str
    vehicle_year: int
    license_plate: str

class DriverLocationUpdate(BaseModel):
    latitude: float
    longitude: float

class DriverResponse(BaseModel):
    id: str
    user_id: str
    license_number: str
    vehicle_make: str
    vehicle_model: str
    vehicle_color: str
    vehicle_year: int
    license_plate: str
    rating: float
    trip_count: int
    is_active: bool
    is_verified: bool
    current_latitude: Optional[float]
    current_longitude: Optional[float]
    created_at: datetime

    class Config:
        from_attributes = True