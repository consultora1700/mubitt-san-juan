from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import uvicorn
from datetime import datetime

# Import API routers
from api.auth import router as auth_router
from api.trips import router as trips_router
from api.drivers import router as drivers_router

app = FastAPI(
    title="Mubitt API",
    description="API para la aplicación de transporte Mubitt - San Juan, Argentina",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS middleware for Android app
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, restrict to specific origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include API routers
app.include_router(auth_router)
app.include_router(trips_router)
app.include_router(drivers_router)

@app.get("/")
async def root():
    return {
        "message": "Mubitt API - Competidor local de Uber/Didi en San Juan 🚗",
        "version": "1.0.0",
        "status": "operational",
        "timestamp": datetime.utcnow().isoformat(),
        "docs": "/docs",
        "redoc": "/redoc"
    }

@app.get("/health")
async def health_check():
    return {
        "status": "healthy", 
        "service": "mubitt-api",
        "timestamp": datetime.utcnow().isoformat(),
        "version": "1.0.0"
    }

# San Juan specific endpoints
@app.get("/san-juan/references")
async def get_san_juan_references():
    """Get popular location references in San Juan."""
    return {
        "references": [
            {
                "id": "hospital_rawson",
                "name": "Hospital Rawson",
                "address": "Av. Ignacio de la Roza 130, San Juan",
                "latitude": -31.5375,
                "longitude": -68.5364,
                "category": "hospital"
            },
            {
                "id": "unsj",
                "name": "Universidad Nacional de San Juan (UNSJ)",
                "address": "Av. Libertador San Martín, San Juan",
                "latitude": -31.5441,
                "longitude": -68.5504,
                "category": "university"
            },
            {
                "id": "plaza_25_mayo",
                "name": "Plaza 25 de Mayo",
                "address": "Plaza 25 de Mayo, Centro, San Juan",
                "latitude": -31.5375,
                "longitude": -68.5289,
                "category": "landmark"
            },
            {
                "id": "shopping_del_sol",
                "name": "Shopping del Sol",
                "address": "Av. José Ignacio de la Roza, San Juan",
                "latitude": -31.5203,
                "longitude": -68.5289,
                "category": "shopping"
            },
            {
                "id": "terminal_bus",
                "name": "Terminal de Ómnibus",
                "address": "Estados Unidos, San Juan",
                "latitude": -31.5344,
                "longitude": -68.5197,
                "category": "transport"
            },
            {
                "id": "aeropuerto",
                "name": "Aeropuerto Domingo Faustino Sarmiento",
                "address": "Pocito, San Juan",
                "latitude": -31.5714,
                "longitude": -68.4182,
                "category": "airport"
            }
        ]
    }

@app.get("/san-juan/zones")
async def get_san_juan_zones():
    """Get zones/districts in San Juan with surge pricing info."""
    return {
        "zones": [
            {
                "id": "centro",
                "name": "Centro",
                "surge_factor": 1.0,
                "demand": "medium"
            },
            {
                "id": "desamparados",
                "name": "Desamparados",
                "surge_factor": 1.1,
                "demand": "high"
            },
            {
                "id": "rivadavia",
                "name": "Rivadavia",
                "surge_factor": 1.0,
                "demand": "low"
            },
            {
                "id": "chimbas",
                "name": "Chimbas",
                "surge_factor": 1.2,
                "demand": "medium"
            },
            {
                "id": "rawson",
                "name": "Rawson",
                "surge_factor": 1.0,
                "demand": "low"
            },
            {
                "id": "pocito",
                "name": "Pocito",
                "surge_factor": 1.3,
                "demand": "high"
            }
        ]
    }

# Global exception handler
@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    return JSONResponse(
        status_code=500,
        content={
            "error": "Internal server error",
            "message": "Something went wrong. Please try again later.",
            "timestamp": datetime.utcnow().isoformat()
        }
    )

if __name__ == "__main__":
    uvicorn.run(
        app, 
        host="0.0.0.0", 
        port=8000,
        reload=True,  # For development
        log_level="info"
    )