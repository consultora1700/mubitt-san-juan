from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import JWTError, jwt
from passlib.context import CryptContext
from datetime import datetime, timedelta
from typing import Optional
import uuid
import os

from models.user import User, UserCreate, UserLogin, AuthResponse, UserResponse

router = APIRouter(prefix="/auth", tags=["Authentication"])

# Configuration
SECRET_KEY = os.getenv("SECRET_KEY", "mubitt-secret-key-change-in-production")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30
REFRESH_TOKEN_EXPIRE_DAYS = 7

# Security
security = HTTPBearer()
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def hash_password(password: str) -> str:
    """Hash a password for storing."""
    return pwd_context.hash(password)

def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verify a stored password against provided password."""
    return pwd_context.verify(plain_password, hashed_password)

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    """Create JWT access token."""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def create_refresh_token(data: dict):
    """Create JWT refresh token."""
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)):
    """Get current user from JWT token."""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    try:
        payload = jwt.decode(credentials.credentials, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        if user_id is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    
    # In a real app, fetch user from database
    # For now, return mock user
    return {"id": user_id, "email": payload.get("email")}

@router.post("/register", response_model=AuthResponse)
async def register(user_data: UserCreate):
    """Register a new user."""
    # In a real app, check if user exists and save to database
    
    # Simulate user creation
    user_id = str(uuid.uuid4())
    hashed_password = hash_password(user_data.password)
    
    # Create mock user response
    user = UserResponse(
        id=user_id,
        name=user_data.name,
        email=user_data.email,
        phone_number=user_data.phone_number,
        profile_picture_url=None,
        rating=5.0,
        trip_count=0,
        is_verified=False,
        created_at=datetime.utcnow(),
        updated_at=datetime.utcnow()
    )
    
    # Create tokens
    access_token = create_access_token(
        data={"sub": user_id, "email": user_data.email}
    )
    refresh_token = create_refresh_token(
        data={"sub": user_id, "email": user_data.email}
    )
    
    return AuthResponse(
        user=user,
        access_token=access_token,
        refresh_token=refresh_token,
        expires_in=ACCESS_TOKEN_EXPIRE_MINUTES * 60
    )

@router.post("/login", response_model=AuthResponse)
async def login(login_data: UserLogin):
    """Login user."""
    # In a real app, validate credentials against database
    
    # Mock validation
    if not login_data.email and not login_data.phone_number:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email or phone number is required"
        )
    
    # Simulate user lookup and password verification
    user_id = str(uuid.uuid4())
    email = login_data.email or "mock@example.com"
    
    # Create mock user response
    user = UserResponse(
        id=user_id,
        name="Usuario San Juan",
        email=email,
        phone_number=login_data.phone_number or "+54 264 123-4567",
        profile_picture_url=None,
        rating=4.8,
        trip_count=15,
        is_verified=True,
        created_at=datetime.utcnow() - timedelta(days=30),
        updated_at=datetime.utcnow()
    )
    
    # Create tokens
    access_token = create_access_token(
        data={"sub": user_id, "email": email}
    )
    refresh_token = create_refresh_token(
        data={"sub": user_id, "email": email}
    )
    
    return AuthResponse(
        user=user,
        access_token=access_token,
        refresh_token=refresh_token,
        expires_in=ACCESS_TOKEN_EXPIRE_MINUTES * 60
    )

@router.post("/refresh")
async def refresh_token(refresh_token: str):
    """Refresh access token."""
    try:
        payload = jwt.decode(refresh_token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        email: str = payload.get("email")
        
        if user_id is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid refresh token"
            )
        
        new_access_token = create_access_token(
            data={"sub": user_id, "email": email}
        )
        
        return {
            "access_token": new_access_token,
            "expires_in": ACCESS_TOKEN_EXPIRE_MINUTES * 60
        }
        
    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid refresh token"
        )

@router.post("/verify-phone")
async def verify_phone(phone_number: str, verification_code: str):
    """Verify phone number with SMS code."""
    # In a real app, validate the SMS code
    
    # Mock verification
    if verification_code == "123456":
        return {"message": "Phone number verified successfully"}
    else:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid verification code"
        )

@router.get("/me", response_model=UserResponse)
async def get_current_user_profile(current_user = Depends(get_current_user)):
    """Get current user profile."""
    # In a real app, fetch from database
    return UserResponse(
        id=current_user["id"],
        name="Usuario San Juan",
        email=current_user["email"],
        phone_number="+54 264 123-4567",
        profile_picture_url=None,
        rating=4.8,
        trip_count=15,
        is_verified=True,
        created_at=datetime.utcnow() - timedelta(days=30),
        updated_at=datetime.utcnow()
    )