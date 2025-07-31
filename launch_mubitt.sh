#!/bin/bash

echo "🎆 LANZAMIENTO MUBITT - SCRIPT MAESTRO 🎆"
echo "========================================"
echo ""
echo "🚗 Mubitt - Competidor Local de Uber/Didi en San Juan"
echo "🏔️ Hecho específicamente para sanjuaninos"
echo ""

# Verificar directorio
if [ ! -f "android/app/build.gradle" ]; then
    echo "❌ Error: Ejecutar desde /home/consultora1600/mubitt"
    exit 1
fi

echo "📋 RESUMEN DE TAREAS PENDIENTES:"
echo "==============================="
echo "1. 🗝️  Obtener Google Maps API Key"
echo "2. 🚀 Deploy Backend a Railway"  
echo "3. 📱 Generar APK Staging"
echo "4. 🔥 Configurar Firebase"
echo "5. 👥 Reclutar Beta Testers"
echo ""

# Menu interactivo
while true; do
    echo "¿Qué quieres hacer?"
    echo ""
    echo "1) 🔐 Obtener SHA-1 para Google Maps API"
    echo "2) 🚀 Deploy Backend a Railway"
    echo "3) 📱 Generar APK Staging"
    echo "4) 📚 Ver guía paso a paso completa"
    echo "5) 🧪 Ver templates para beta testers"
    echo "6) ❌ Salir"
    echo ""
    read -p "Selecciona una opción (1-6): " choice
    
    case $choice in
        1)
            echo ""
            echo "🔐 OBTENIENDO SHA-1 CERTIFICATE..."
            echo "=================================="
            ./android/get_sha1.sh
            echo ""
            read -p "Presiona Enter para continuar..."
            echo ""
            ;;
        2)
            echo ""
            echo "🚀 DESPLEGANDO BACKEND..."
            echo "========================"
            ./deploy_backend.sh
            echo ""
            read -p "Presiona Enter para continuar..."
            echo ""
            ;;
        3)
            echo ""
            echo "📱 GENERANDO APK STAGING..."
            echo "=========================="
            ./build_apk.sh
            echo ""
            read -p "Presiona Enter para continuar..."
            echo ""
            ;;
        4)
            echo ""
            echo "📚 ABRIENDO GUÍA PASO A PASO..."
            echo "=============================="
            cat STEP_BY_STEP_LAUNCH.md
            echo ""
            read -p "Presiona Enter para continuar..."
            echo ""
            ;;
        5)
            echo ""
            echo "🧪 TEMPLATES PARA BETA TESTERS..."
            echo "================================"
            cat BETA_TESTER_OUTREACH.md
            echo ""
            read -p "Presiona Enter para continuar..."
            echo ""
            ;;
        6)
            echo ""
            echo "👋 ¡Gracias por usar el script de lanzamiento Mubitt!"
            echo "🚗 ¡Éxito con el beta testing en San Juan!"
            echo ""
            break
            ;;
        *)
            echo ""
            echo "❌ Opción inválida. Por favor selecciona 1-6."
            echo ""
            ;;
    esac
done

echo "🎆 ESTADO ACTUAL DEL PROYECTO:"
echo "============================"
echo "✅ Android App: 100% completado"
echo "✅ Backend API: 100% completado"  
echo "✅ Búsqueda Inteligente San Juan: Implementada"
echo "✅ Documentación: Completa"
echo ""
echo "📋 PRÓXIMOS PASOS:"
echo "=================="
echo "1. Configurar Google Maps API Key"
echo "2. Deploy backend a Railway"
echo "3. Generar APK y distribuir a beta testers"
echo "4. Recopilar feedback y iterar"
echo "5. ¡Lanzamiento público en San Juan!"
echo ""
echo "🏆 MUBITT ESTÁ LISTO PARA COMPETIR CON UBER/DIDI"
echo "🚗🏔️ ¡Es hora de conquistar San Juan!"