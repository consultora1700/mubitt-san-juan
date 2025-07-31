#!/bin/bash

echo "🚀 DEPLOY MUBITT BACKEND TO RAILWAY"
echo "=================================="

# Verificar que estamos en el directorio correcto
if [ ! -f "backend/main.py" ]; then
    echo "❌ Error: Ejecutar desde el directorio raíz del proyecto mubitt"
    echo "💡 cd /home/consultora1600/mubitt && ./deploy_backend.sh"
    exit 1
fi

cd backend

echo "📦 PREPARANDO BACKEND PARA DEPLOY..."
echo "===================================="

# Verificar archivos necesarios
echo "✅ Verificando archivos..."
if [ ! -f "main.py" ]; then
    echo "❌ main.py no encontrado"
    exit 1
fi

if [ ! -f "requirements.txt" ]; then
    echo "❌ requirements.txt no encontrado"
    exit 1
fi

echo "✅ Archivos verificados correctamente"
echo ""

echo "🔧 INSTALANDO RAILWAY CLI..."
echo "============================"

# Verificar si Railway CLI está instalado
if ! command -v railway &> /dev/null; then
    echo "📥 Instalando Railway CLI..."
    curl -fsSL https://railway.app/install.sh | sh
    
    # Agregar al PATH si no está
    if [[ ":$PATH:" != *":$HOME/.local/bin:"* ]]; then
        export PATH="$HOME/.local/bin:$PATH"
        echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc
    fi
else
    echo "✅ Railway CLI ya está instalado"
fi

echo ""
echo "🔐 CONFIGURANDO RAILWAY..."
echo "========================="

# Verificar si ya está logueado
if ! railway whoami &> /dev/null; then
    echo "🔑 Por favor, haz login en Railway:"
    echo "1. Se abrirá tu navegador"
    echo "2. Autoriza la aplicación"
    echo "3. Regresa aquí cuando termine"
    echo ""
    railway login
else
    echo "✅ Ya estás logueado en Railway"
fi

echo ""
echo "🚀 DESPLEGANDO BACKEND..."
echo "========================"

# Inicializar proyecto si no existe
if [ ! -f "railway.toml" ]; then
    echo "📋 Inicializando proyecto Railway..."
    railway init mubitt-backend-api
fi

# Deploy
echo "📤 Subiendo código a Railway..."
railway up

# Mostrar información del deploy
echo ""
echo "🎉 DEPLOY COMPLETADO!"
echo "===================="

# Obtener URL del servicio
SERVICE_URL=$(railway domain 2>/dev/null || echo "URL no disponible aún")

echo "✅ Backend desplegado exitosamente"
echo "🌐 URL del servicio: $SERVICE_URL"
echo "📚 Documentación API: $SERVICE_URL/docs"
echo "🏥 Health check: $SERVICE_URL/health"
echo ""

echo "⚙️  PRÓXIMOS PASOS:"
echo "=================="
echo "1. Configura las variables de entorno en Railway:"
echo "   railway variables set SECRET_KEY=tu_secret_key"
echo "   railway variables set GOOGLE_MAPS_API_KEY=tu_maps_key"
echo ""
echo "2. Actualiza la URL del backend en Android:"
echo "   - Editar android/app/build.gradle"
echo "   - staging: buildConfigField \"String\", \"BASE_URL\", \"\\\"$SERVICE_URL/\\\"\""
echo ""
echo "3. Genera el APK staging con la nueva URL"
echo ""

echo "🚀 BACKEND LISTO PARA TESTING!"

# Volver al directorio raíz
cd ..