#!/bin/bash

# 🚀 MUBITT - CONFIGURACIÓN JAVA AUTOMÁTICA
# Este script configura Java 17 portátil para desarrollo Android

echo "🔧 Configurando Java 17 para Mubitt..."

# Configurar variables de entorno (Java 17 requerido para Android)
export JAVA_HOME="$HOME/tools/java/jdk-17.0.13+11"
export PATH="$JAVA_HOME/bin:$PATH"

# Verificar configuración
echo "✅ JAVA_HOME: $JAVA_HOME"
echo "✅ Java version:"
java -version

echo ""
echo "🎯 Java configurado correctamente para Mubitt!"
echo "💡 Ahora puedes ejecutar: gradle clean && gradle app:assembleDebug"