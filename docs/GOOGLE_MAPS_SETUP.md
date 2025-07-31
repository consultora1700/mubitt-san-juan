# Google Maps API Configuration - Mubitt

## Configuración de Google Maps API Key

### 1. Crear Proyecto en Google Cloud Console

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crear nuevo proyecto: "Mubitt San Juan"
3. Habilitar APIs necesarias:
   - **Maps SDK for Android**
   - **Places API**
   - **Directions API**
   - **Geocoding API**
   - **Distance Matrix API**

### 2. Crear API Key

1. Ir a "Credentials" → "Create Credentials" → "API Key"
2. Copiar la API key generada
3. **IMPORTANTE**: Restringir la API key:
   - Restricciones de aplicación: Android apps
   - SHA-1 del certificado de la app
   - Restricciones de API: Solo las APIs habilitadas arriba

### 3. Configurar en la App

Reemplazar en `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="TU_API_KEY_AQUI" />
```

### 4. SHA-1 Certificate Fingerprints

Para obtener SHA-1 del certificado:

#### Debug Certificate:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### Release Certificate:
```bash
keytool -list -v -keystore mubitt-release-key.keystore -alias mubitt
```

### 5. APIs y Cuotas Recomendadas

Para San Juan (estimado 5000 usuarios activos):

| API | Límite Diario | Costo Estimado/Mes |
|-----|---------------|-------------------|
| Maps SDK | 100,000 | Gratis |
| Places API | 50,000 | $200 USD |
| Directions API | 30,000 | $150 USD |
| Geocoding API | 20,000 | $100 USD |

**Total estimado**: ~$450 USD/mes con 5K usuarios activos

### 6. Optimizaciones de Costo

1. **Cache agresivo** de resultados de Places API
2. **Geocoding inverso** solo cuando sea necesario
3. **Batch requests** cuando sea posible
4. **Limitar zoom** del mapa para reducir tile requests

### 7. Configuración para Testing

Para desarrollo y testing beta:

```xml
<!-- Desarrollo -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyDEVELOPMENT_KEY_HERE" />
```

### 8. Security Best Practices

1. **Nunca** hacer commit de API keys al repositorio
2. Usar **variables de entorno** o archivos no trackeados
3. **Rotar keys** regularmente
4. **Monitorear uso** para detectar abuso
5. **Restricciones estrictas** por package name y SHA-1

### 9. Estructura de Archivos Recomendada

```
android/
├── app/
│   ├── src/
│   │   ├── debug/
│   │   │   └── res/values/google_maps_api.xml (no tracked)
│   │   ├── release/
│   │   │   └── res/values/google_maps_api.xml (no tracked)
│   │   └── main/
│   └── google-services.json (no tracked)
├── keystore/
│   └── mubitt-release.keystore (no tracked)
└── .gitignore (incluir archivos sensibles)
```

### 10. Variables de Entorno

En `build.gradle`:

```gradle
android {
    defaultConfig {
        manifestPlaceholders = [
            GOOGLE_MAPS_API_KEY: System.getenv("GOOGLE_MAPS_API_KEY") ?: "demo_key"
        ]
    }
}
```

En `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${GOOGLE_MAPS_API_KEY}" />
```

## Estado Actual

- ✅ AndroidManifest configurado con placeholder
- ⏳ **PENDIENTE**: Crear proyecto en Google Cloud Console
- ⏳ **PENDIENTE**: Generar API key real
- ⏳ **PENDIENTE**: Configurar restricciones de seguridad

## Próximos Pasos

1. Crear cuenta Google Cloud Console
2. Configurar billing account
3. Generar API keys para dev/staging/prod
4. Configurar restricciones de seguridad
5. Testing con API real en San Juan