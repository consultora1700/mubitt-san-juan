#!/bin/bash

# Script para compilar APK de Mubitt
# Usar desde WSL para compilar en Windows

echo "🚀 Iniciando compilación de Mubitt APK..."
echo "📍 Directorio actual: $(pwd)"

# Configurar variables de entorno
export ANDROID_HOME="/mnt/c/Users/lucas/AppData/Local/Android/Sdk"
export ANDROID_SDK_ROOT="/mnt/c/Users/lucas/AppData/Local/Android/Sdk"
export JAVA_HOME="/mnt/c/Program Files/Java/jdk-23.0.2+7"

# Verificar que los directorios existan
if [ ! -d "$ANDROID_HOME" ]; then
    echo "❌ Error: Android SDK no encontrado en $ANDROID_HOME"
    exit 1
fi

if [ ! -d "$JAVA_HOME" ]; then
    echo "❌ Error: Java JDK no encontrado en $JAVA_HOME"
    exit 1
fi

echo "✅ Android SDK encontrado: $ANDROID_HOME"
echo "✅ Java JDK encontrado: $JAVA_HOME"

# Crear gradlew si no existe
if [ ! -f "gradlew" ]; then
    echo "📦 Creando Gradle Wrapper..."
    if command -v gradle >/dev/null 2>&1; then
        gradle wrapper --gradle-version 8.0 --distribution-type bin
    else
        echo "❌ Gradle no encontrado. Necesitas instalar Gradle o usar Android Studio."
        exit 1
    fi
fi

# Hacer gradlew ejecutable
chmod +x gradlew

# Limpiar build anterior
echo "🧹 Limpiando builds anteriores..."
./gradlew clean

# Compilar APK debug
echo "🔨 Compilando APK debug..."
./gradlew assembleDebug

# Verificar si la compilación fue exitosa
if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 ¡Compilación exitosa!"
    echo "📱 APK generado en: app/build/outputs/apk/debug/"
    
    # Buscar el APK generado
    APK_PATH=$(find app/build/outputs/apk/debug/ -name "*.apk" | head -1)
    if [ -n "$APK_PATH" ]; then
        echo "✅ APK encontrado: $APK_PATH"
        echo "📁 Tamaño: $(du -h "$APK_PATH" | cut -f1)"
        echo ""
        echo "📲 Para instalar en tu teléfono:"
        echo "   1. Conecta tu teléfono por USB"
        echo "   2. Activa 'Depuración USB' en Opciones de desarrollador"
        echo "   3. Ejecuta: adb install \"$APK_PATH\""
        echo "   O copia el APK a tu teléfono e instálalo manualmente"
    else
        echo "⚠️  APK compilado pero no encontrado en la ubicación esperada"
    fi
else
    echo "❌ Error en la compilación"
    echo "💡 Posibles soluciones:"
    echo "   1. Verificar que Android Studio esté instalado"
    echo "   2. Verificar configuración de JAVA_HOME"
    echo "   3. Ejecutar desde Android Studio directamente"
fi

echo ""
echo "🔍 Para más información, revisa los logs arriba"