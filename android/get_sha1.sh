#!/bin/bash

echo "🔐 OBTENIENDO SHA-1 CERTIFICATE PARA GOOGLE MAPS API"
echo "=================================================="

# Verificar si existe debug keystore
DEBUG_KEYSTORE="$HOME/.android/debug.keystore"

if [ ! -f "$DEBUG_KEYSTORE" ]; then
    echo "❌ Debug keystore no encontrado en: $DEBUG_KEYSTORE"
    echo "💡 Genera uno ejecutando cualquier app en Android Studio primero"
    exit 1
fi

echo "✅ Debug keystore encontrado"
echo "📍 Ubicación: $DEBUG_KEYSTORE"
echo ""

echo "🔍 EXTRAYENDO SHA-1 FINGERPRINT..."
echo "=================================="

# Extraer SHA-1
SHA1=$(keytool -list -v -keystore "$DEBUG_KEYSTORE" -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep -i "SHA1:" | cut -d' ' -f3)

if [ -z "$SHA1" ]; then
    echo "❌ No se pudo extraer SHA-1"
    echo "💡 Ejecutando comando completo para debug..."
    keytool -list -v -keystore "$DEBUG_KEYSTORE" -alias androiddebugkey -storepass android -keypass android
    exit 1
fi

echo "✅ SHA-1 Certificate Fingerprint:"
echo "📋 $SHA1"
echo ""

echo "📋 INFORMACIÓN PARA GOOGLE CLOUD CONSOLE:"
echo "========================================"
echo "Package Name: com.mubitt.app.staging"
echo "SHA-1 Fingerprint: $SHA1"
echo ""

echo "🚀 PRÓXIMOS PASOS:"
echo "=================="
echo "1. Copia el SHA-1 de arriba"
echo "2. Ve a Google Cloud Console → Credentials"
echo "3. Edita tu API Key → Application restrictions"
echo "4. Selecciona 'Android apps'"
echo "5. Agrega:"
echo "   - Package name: com.mubitt.app.staging"
echo "   - SHA-1 certificate fingerprint: $SHA1"
echo "6. Guarda los cambios"
echo ""

echo "✅ SCRIPT COMPLETADO - SHA-1 EXTRAÍDO EXITOSAMENTE"