from sqlalchemy import Column, String, Float, Integer, Boolean, DateTime, Text
from sqlalchemy.ext.declarative import declarative_base
from datetime import datetime
from pydantic import BaseModel, EmailStr
from typing import Optional

Base = declarative_base()

class User(Base):
    __tablename__ = "users"
    
    id = Column(String, primary_key=True)
    name = Column(String(100), nullable=False)
    email = Column(String(100), unique=True, nullable=False)
    phone_number = Column(String(20), unique=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    profile_picture_url = Column(String(500), nullable=True)
    rating = Column(Float, default=5.0)
    trip_count = Column(Integer, default=0)
    is_verified = Column(Boolean, default=False)
    device_token = Column(String(500), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

# Pydantic models for API
class UserCreate(BaseModel):
    name: str
    email: EmailStr
    phone_number: str
    password: str
    device_token: Optional[str] = None

class UserLogin(BaseModel):
    email: Optional[EmailStr] = None
    phone_number: Optional[str] = None
    password: str

class UserResponse(BaseModel):
    id: str
    name: str
    email: str
    phone_number: str
    profile_picture_url: Optional[str]
    rating: float
    trip_count: int
    is_verified: bool
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

class AuthResponse(BaseModel):
    user: UserResponse
    access_token: str
    refresh_token: str
    expires_in: int

class UserUpdate(BaseModel):
    name: Optional[str] = None
    email: Optional[EmailStr] = None
    profile_picture_url: Optional[str] = None