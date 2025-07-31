#!/bin/bash
echo "🔍 Verificando configuración del proyecto Mubitt..."
echo "📍 Ubicación actual: $(pwd)"
echo ""

# Verificar estructura de directorios
echo "📁 Verificando estructura de directorios..."
if [ -d "docs" ] && [ -d "android" ] && [ -d "backend" ] && [ -d "scripts" ]; then
    echo "✅ Estructura de directorios creada correctamente"
else
    echo "❌ Faltan directorios principales"
    exit 1
fi

# Verificar archivos de documentación
echo "📚 Verificando archivos de documentación..."
if [ -f "docs/PROJECT_CONTEXT.md" ] && [ -f "docs/DEVELOPMENT_PHASES.md" ] && [ -f "docs/TECHNICAL_STANDARDS.md" ]; then
    echo "✅ Archivos de documentación presentes"
else
    echo "❌ Faltan archivos de documentación"
    exit 1
fi

# Verificar archivos de configuración Android
echo "📱 Verificando configuración Android..."
if [ -f "android/build.gradle" ] && [ -f "android/app/build.gradle" ]; then
    echo "✅ Configuración Android presente"
else
    echo "❌ Falta configuración Android"
    exit 1
fi

# Verificar configuración Backend
echo "🔧 Verificando configuración Backend..."
if [ -f "backend/requirements.txt" ] && [ -f "backend/main.py" ]; then
    echo "✅ Configuración Backend presente"
else
    echo "❌ Falta configuración Backend"
    exit 1
fi

echo ""
echo "🎉 ¡Proyecto Mubitt configurado correctamente!"
echo "📍 Ubicación: \\wsl.localhost\\Ubuntu\\home\\consultora1600\\mubitt"
echo "🚀 Listo para comenzar el desarrollo"