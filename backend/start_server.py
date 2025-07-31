#!/usr/bin/env python3
"""
Mubitt Backend Server Starter
Starts the FastAPI server with proper configuration
"""

import uvicorn
import os
import sys
from pathlib import Path

def main():
    # Add the backend directory to Python path
    backend_dir = Path(__file__).parent
    sys.path.insert(0, str(backend_dir))
    
    # Configuration
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", 8000))
    reload = os.getenv("RELOAD", "true").lower() == "true"
    log_level = os.getenv("LOG_LEVEL", "info")
    
    print(f"🚀 Starting Mubitt API Server...")
    print(f"📍 Host: {host}")
    print(f"🔌 Port: {port}")
    print(f"🔄 Reload: {reload}")
    print(f"📊 Log Level: {log_level}")
    print(f"📚 Docs: http://{host}:{port}/docs")
    print(f"🔍 ReDoc: http://{host}:{port}/redoc")
    print(f"🏥 Health: http://{host}:{port}/health")
    print("=" * 50)
    
    try:
        uvicorn.run(
            "main:app",
            host=host,
            port=port,
            reload=reload,
            log_level=log_level,
            access_log=True
        )
    except KeyboardInterrupt:
        print("\n🛑 Server stopped by user")
    except Exception as e:
        print(f"❌ Error starting server: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()