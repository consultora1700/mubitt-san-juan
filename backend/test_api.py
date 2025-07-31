#!/usr/bin/env python3
"""
Mubitt API Testing Script
Tests all API endpoints to ensure they work correctly
"""

import requests
import json
import time
from datetime import datetime

# Configuration
BASE_URL = "http://localhost:8000"
HEADERS = {"Content-Type": "application/json"}

def test_endpoint(method, endpoint, data=None, expected_status=200, auth_token=None):
    """Test an API endpoint."""
    url = f"{BASE_URL}{endpoint}"
    headers = HEADERS.copy()
    
    if auth_token:
        headers["Authorization"] = f"Bearer {auth_token}"
    
    try:
        if method == "GET":
            response = requests.get(url, headers=headers)
        elif method == "POST":
            response = requests.post(url, json=data, headers=headers)
        elif method == "PUT":
            response = requests.put(url, json=data, headers=headers)
        else:
            print(f"❌ Unsupported method: {method}")
            return False
        
        success = response.status_code == expected_status
        status_icon = "✅" if success else "❌"
        
        print(f"{status_icon} {method} {endpoint} - Status: {response.status_code}")
        
        if not success:
            print(f"   Expected: {expected_status}, Got: {response.status_code}")
            try:
                print(f"   Response: {response.json()}")
            except:
                print(f"   Response: {response.text[:200]}")
        
        return success, response.json() if response.status_code < 500 else None
        
    except Exception as e:
        print(f"❌ {method} {endpoint} - Error: {e}")
        return False, None

def main():
    print("🚀 Mubitt API Testing Suite")
    print("=" * 50)
    
    # Test basic endpoints
    print("\n📋 Testing Basic Endpoints:")
    test_endpoint("GET", "/")
    test_endpoint("GET", "/health")
    test_endpoint("GET", "/docs", expected_status=200)
    
    # Test San Juan specific endpoints
    print("\n🗺️ Testing San Juan Endpoints:")
    test_endpoint("GET", "/san-juan/references")
    test_endpoint("GET", "/san-juan/zones")
    
    # Test authentication
    print("\n🔐 Testing Authentication:")
    
    # Register user
    register_data = {
        "name": "Usuario Test",
        "email": "test@mubitt.com",
        "phone_number": "+54 264 123-4567",
        "password": "testpassword123"
    }
    
    success, register_response = test_endpoint("POST", "/auth/register", register_data)
    auth_token = None
    
    if success and register_response:
        auth_token = register_response.get("access_token")
        print(f"   🎫 Token obtained: {auth_token[:20]}...")
    
    # Login user
    login_data = {
        "email": "test@mubitt.com",
        "password": "testpassword123"
    }
    
    success, login_response = test_endpoint("POST", "/auth/login", login_data)
    if success and login_response:
        auth_token = login_response.get("access_token")
    
    # Test protected endpoints
    if auth_token:
        print("\n👤 Testing Protected Endpoints:")
        test_endpoint("GET", "/auth/me", auth_token=auth_token)
        
        # Test trips
        print("\n🚗 Testing Trip Endpoints:")
        
        # Estimate fare
        fare_data = {
            "pickup_location": {
                "latitude": -31.5375,
                "longitude": -68.5364,
                "address": "Hospital Rawson, San Juan"
            },
            "dropoff_location": {
                "latitude": -31.5441,
                "longitude": -68.5504,
                "address": "UNSJ, San Juan"
            },
            "vehicle_type": "economy"
        }
        
        test_endpoint("POST", "/trips/estimate-fare", fare_data)
        
        # Search drivers
        search_data = {
            "pickup_location": {
                "latitude": -31.5375,
                "longitude": -68.5364,
                "address": "Hospital Rawson, San Juan"
            },
            "radius": 5.0,
            "vehicle_type": "economy"
        }
        
        test_endpoint("POST", "/trips/search-drivers", search_data, auth_token=auth_token)
        
        # Create trip
        trip_data = {
            "pickup_location": {
                "latitude": -31.5375,
                "longitude": -68.5364,
                "address": "Hospital Rawson, San Juan"
            },
            "dropoff_location": {
                "latitude": -31.5441,
                "longitude": -68.5504,
                "address": "UNSJ, San Juan"
            },
            "vehicle_type": "economy",
            "payment_method_id": "pm_test_123"
        }
        
        success, trip_response = test_endpoint("POST", "/trips/create", trip_data, auth_token=auth_token)
        
        if success and trip_response:
            trip_id = trip_response.get("id")
            
            # Get trip details
            test_endpoint("GET", f"/trips/{trip_id}", auth_token=auth_token)
            
            # Get user trips
            test_endpoint("GET", "/trips/", auth_token=auth_token)
        
        # Test driver endpoints
        print("\n🚛 Testing Driver Endpoints:")
        
        # Register as driver
        driver_data = {
            "license_number": "SJ123456",
            "vehicle_make": "Toyota",
            "vehicle_model": "Corolla",
            "vehicle_color": "Blanco",
            "vehicle_year": 2020,
            "license_plate": "SJU 123"
        }
        
        test_endpoint("POST", "/drivers/register", driver_data, auth_token=auth_token)
        test_endpoint("GET", "/drivers/profile", auth_token=auth_token)
        test_endpoint("GET", "/drivers/earnings", auth_token=auth_token)
        
        # Update driver location
        location_data = {
            "latitude": -31.5375,
            "longitude": -68.5364
        }
        
        test_endpoint("PUT", "/drivers/location", location_data, auth_token=auth_token)
    
    else:
        print("❌ Could not obtain auth token, skipping protected endpoint tests")
    
    print("\n" + "=" * 50)
    print("🏁 API Testing Complete!")
    print(f"⏰ Timestamp: {datetime.now().isoformat()}")

if __name__ == "__main__":
    main()