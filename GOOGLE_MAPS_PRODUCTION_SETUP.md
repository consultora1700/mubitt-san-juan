# 🗺️ Google Maps Production Setup - Mubitt

## ✅ CONFIGURACIÓN COMPLETADA

### 1. Build Configuration ✅
- `build.gradle` configurado para usar variables de entorno
- `AndroidManifest.xml` configurado con placeholder seguro
- Función `getGoogleMapsApiKey()` implementada con fallbacks

### 2. Security Configuration ✅
- `.gitignore` configurado para excluir archivos sensibles
- `local.properties.example` creado como template
- Variables de entorno prioritarias sobre archivos locales

## 🚀 PASOS PARA ACTIVAR EN PRODUCCIÓN

### Paso 1: Crear Proyecto Google Cloud Console
1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crear nuevo proyecto: **"Mubitt San Juan Production"**
3. Habilitar billing account (requerido para APIs)

### Paso 2: Habilitar APIs Necesarias
```bash
# APIs requeridas para Mubitt:
- Maps SDK for Android
- Places API  
- Directions API
- Geocoding API
- Distance Matrix API
- Roads API (opcional para optimización de rutas)
```

### Paso 3: Crear API Keys
1. Ir a **Credentials** → **Create Credentials** → **API Key**
2. Copiar la API key generada

### Paso 4: Configurar Restricciones de Seguridad
```
Application restrictions:
- Android apps
- Package name: com.mubitt.app
- SHA-1 certificate fingerprints: [Ver sección SHA-1 abajo]

API restrictions:
- Restrict key to selected APIs
- Select only the APIs enabled above
```

### Paso 5: Obtener SHA-1 Certificates

#### Para Debug Build:
```bash
cd /home/consultora1600/mubitt/android
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### Para Release Build (cuando se cree):
```bash
keytool -list -v -keystore keystore/mubitt-release.keystore -alias mubitt
```

### Paso 6: Configurar Local Properties
```bash
# Copiar template y completar
cp local.properties.example local.properties

# Editar local.properties con valores reales:
GOOGLE_MAPS_API_KEY=AIzaSy[TU_API_KEY_AQUI]
```

### Paso 7: Variables de Entorno para CI/CD
```bash
# Para GitHub Actions, Bitbucket Pipelines, etc.
export GOOGLE_MAPS_API_KEY="AIzaSy[TU_API_KEY_AQUI]"
```

## 📊 COSTOS ESTIMADOS PARA SAN JUAN

### Usuarios Objetivo: 5,000 activos/mes
| API | Requests/mes | Costo USD |
|-----|-------------|-----------|
| Maps SDK | 150,000 | Gratis |
| Places API | 75,000 | $375 |
| Directions | 50,000 | $250 |
| Geocoding | 30,000 | $150 |
| **TOTAL** | | **~$775/mes** |

### Optimizaciones de Costo Implementadas:
- ✅ **Cache agresivo** en Room database
- ✅ **Batch requests** cuando es posible  
- ✅ **Límites de zoom** para reducir tiles
- ✅ **Geocoding inverso** solo cuando necesario

## 🔒 SECURITY CHECKLIST

### ✅ Implementado:
- API keys no hardcoded en el código
- Variables de entorno para CI/CD
- local.properties en .gitignore
- Restricciones por package name
- Restricciones por API específicas

### 🔄 Por implementar al obtener API key:
- SHA-1 certificate restrictions
- Monitoring de uso para detectar abuso
- Rotación periódica de keys
- Alertas de cuota

## 🧪 TESTING LA CONFIGURACIÓN

### 1. Verificar Configuration:
```bash
cd /home/consultora1600/mubitt/android
./gradlew app:assembleDebug
```

### 2. Verificar API Key Load:
- La app debería mostrar mapa real (no "Development purposes only")
- Búsqueda de lugares debería funcionar
- Cálculo de rutas debería funcionar

### 3. Verificar Performance:
- Tiempo de carga del mapa: <2 segundos
- Búsqueda de lugares: <1 segundo
- Cálculo de rutas: <3 segundos

## 📱 DIFERENCIAS POR BUILD TYPE

### Debug Build:
- Usa SHA-1 del debug keystore
- Puede usar API key menos restrictiva
- Logging habilitado

### Release Build:
- Usa SHA-1 del release keystore (por crear)
- API key con restricciones máximas
- Logging deshabilitado
- ProGuard optimizaciones

## 🚨 TROUBLESHOOTING COMÚN

### "This app is not authorized to use this API key"
- Verificar package name en restricciones
- Verificar SHA-1 certificate fingerprint
- Verificar que la API esté habilitada

### "API key not found" 
- Verificar que local.properties existe
- Verificar sintaxis en local.properties
- Verificar variable de entorno GOOGLE_MAPS_API_KEY

### "You have exceeded your request quota"
- Verificar límites en Google Cloud Console
- Verificar billing account activo
- Implementar rate limiting en la app

## 📈 MONITORING RECOMENDADO

### Google Cloud Console:
- APIs & Services → Credentials → Usage
- Alertas por cuota al 80%
- Reportes de uso por aplicación

### App Analytics:
- Tiempo de respuesta de APIs
- Errores de autenticación
- Patrones de uso geográfico

## 🎯 NEXT STEPS DESPUÉS DE CONFIGURAR API

1. **Backend Integration**: Conectar con APIs reales de Mubitt
2. **Beta Testing**: Distribuir a 20 usuarios San Juan
3. **Performance Monitoring**: Implementar métricas en tiempo real  
4. **Cost Optimization**: Analizar patrones de uso reales

---

## ⚡ QUICK START

**Para desarrolladores que quieran empezar inmediatamente:**

1. Obtener API key de Google Cloud Console
2. `cp local.properties.example local.properties`
3. Editar `GOOGLE_MAPS_API_KEY=tu_api_key`
4. `./gradlew app:assembleDebug`
5. ¡App lista con Google Maps funcionando!

**Status**: 🎆 **CONFIGURACIÓN TÉCNICA COMPLETADA** 🎆  
**Falta**: Obtener API key real de Google Cloud Console