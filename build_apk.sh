#!/bin/bash

echo "📱 GENERANDO APK STAGING - MUBITT"
echo "================================="

# Verificar que estamos en el directorio correcto
if [ ! -f "android/app/build.gradle" ]; then
    echo "❌ Error: Ejecutar desde el directorio raíz del proyecto mubitt"
    echo "💡 cd /home/consultora1600/mubitt && ./build_apk.sh"
    exit 1
fi

cd android

echo "🔍 VERIFICANDO CONFIGURACIÓN..."
echo "==============================="

# Verificar local.properties
if [ ! -f "local.properties" ]; then
    echo "⚠️  local.properties no encontrado"
    echo "📋 Creando desde template..."
    cp local.properties.example local.properties
    echo ""
    echo "❗ IMPORTANTE: Edita local.properties y agrega tu Google Maps API key:"
    echo "   GOOGLE_MAPS_API_KEY=AIzaSy[TU_API_KEY_AQUI]"
    echo ""
    read -p "¿Has configurado la API key? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Configura la API key primero y vuelve a ejecutar el script"
        exit 1
    fi
else
    echo "✅ local.properties encontrado"
fi

# Verificar que gradle wrapper existe
if [ ! -f "gradlew" ]; then
    echo "❌ gradlew no encontrado"
    echo "💡 Ejecuta este script desde el directorio android/ o verifica la instalación"
    exit 1
fi

echo "✅ Configuración verificada"
echo ""

echo "🧹 LIMPIANDO BUILD ANTERIOR..."
echo "============================="
./gradlew clean

echo ""
echo "🔨 GENERANDO APK STAGING..."
echo "=========================="

# Build staging APK
./gradlew app:assembleStaging

# Verificar si el build fue exitoso
if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 APK GENERADO EXITOSAMENTE!"
    echo "============================"
    
    APK_PATH="app/build/outputs/apk/staging/app-staging.apk"
    
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "✅ APK ubicado en: $APK_PATH"
        echo "📏 Tamaño: $APK_SIZE"
        echo ""
        
        # Mostrar información del APK
        echo "📋 INFORMACIÓN DEL APK:"
        echo "======================"
        echo "🏷️  Nombre: app-staging.apk"
        echo "📦 Variant: staging"
        echo "🆔 Package: com.mubitt.app.staging"
        echo "📱 Min SDK: 24 (Android 7.0)"
        echo "🎯 Target SDK: 34 (Android 14)"
        echo ""
        
        echo "🚀 PRÓXIMOS PASOS:"
        echo "=================="
        echo "1. Distribuir APK a beta testers:"
        echo "   - Subir a Google Drive/Dropbox"
        echo "   - Enviar por WhatsApp/Email"
        echo "   - Usar Firebase App Distribution"
        echo ""
        echo "2. Instrucciones para beta testers:"
        echo "   - Habilitar 'Fuentes desconocidas' en Android"
        echo "   - Descargar e instalar APK"
        echo "   - Reportar bugs y feedback"
        echo ""
        echo "3. Crear enlace de distribución:"
        echo "   - Subir APK a almacenamiento en la nube"
        echo "   - Crear link de descarga directo"
        echo "   - Agregar instrucciones de instalación"
        echo ""
        
        # Copiar APK a ubicación más accesible
        cp "$APK_PATH" "../mubitt-staging-beta.apk"
        echo "📋 APK copiado a: /home/consultora1600/mubitt/mubitt-staging-beta.apk"
        echo ""
        
        echo "✅ APK STAGING LISTO PARA DISTRIBUCIÓN BETA!"
        
    else
        echo "❌ APK no encontrado en la ubicación esperada"
        echo "💡 Verifica la salida del build para más detalles"
        exit 1
    fi
else
    echo ""
    echo "❌ ERROR AL GENERAR APK"
    echo "======================"
    echo "💡 Revisa los errores de compilación arriba"
    echo "🔧 Posibles soluciones:"
    echo "   - Verifica que local.properties tenga la API key correcta"
    echo "   - Asegúrate de tener Android SDK instalado"
    echo "   - Limpia el proyecto: ./gradlew clean"
    exit 1
fi

# Volver al directorio raíz
cd ..