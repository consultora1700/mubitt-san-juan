# 🗝️ CONFIGURACIÓN MANUAL GOOGLE MAPS API

## 🎯 **PASO 1: CREAR PROYECTO GOOGLE CLOUD CONSOLE**

1. Ve a https://console.cloud.google.com/
2. Crear nuevo proyecto: **"Mubitt San Juan Production"**
3. Habilitar billing (necesario para APIs)

## 🎯 **PASO 2: HABILITAR APIs NECESARIAS**

En la consola, ve a "APIs & Services" → "Library" y habilita:

✅ **Maps SDK for Android**
✅ **Places API** 
✅ **Directions API**
✅ **Geocoding API**
✅ **Distance Matrix API**

## 🎯 **PASO 3: CREAR API KEY**

1. Ve a "APIs & Services" → "Credentials"
2. Click "Create Credentials" → "API Key"
3. **Copia la API key generada** - la necesitarás después

## 🎯 **PASO 4: CONFIGURAR RESTRICCIONES (CRÍTICO)**

1. Click en la API key recién creada para editarla
2. **Application restrictions**:
   - Selecciona "Android apps"
   - Click "Add an item"
   - **Package name**: `com.mubitt.app.staging`
   - **SHA-1**: Por ahora deja en blanco, lo agregaremos después

3. **API restrictions**:
   - Selecciona "Restrict key"
   - Marca solo las 5 APIs que habilitaste arriba

4. **Save**

## 🎯 **PASO 5: CONFIGURAR EN LA APP**

1. Ve a `/home/consultora1600/mubitt/android/`
2. Copia el template:
```bash
cp local.properties.example local.properties
```

3. Edita `local.properties` y agrega tu API key:
```
GOOGLE_MAPS_API_KEY=AIzaSy[TU_API_KEY_AQUI]
```

## 💰 **ESTIMACIÓN DE COSTOS PARA BETA (TRANQUILO):**

Para 20 beta testers durante 2 semanas:
- **Maps SDK**: GRATIS (hasta 100K requests/mes)
- **Places API**: ~$20 USD (estimado)
- **Directions**: ~$10 USD (estimado)
- **Total**: ~$30 USD máximo para todo el beta testing

## ⚠️ **IMPORTANTE - SHA-1 CERTIFICATE:**

**Para uso en producción necesitarás el SHA-1 certificate, pero para beta testing inicial puedes:**

**Opción A (Recomendada):** Usa un certificado temporal
- Genera una APK debug y úsala para testing inicial
- El SHA-1 se puede agregar después

**Opción B:** Si tienes Android Studio instalado:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Opción C:** Usa el SHA-1 genérico para desarrollo:
```
SHA1: 58:9D:91:1D:F0:5F:4F:0A:8A:88:8A:92:F5:8B:3C:1C:88:7F:C4:A5
```

## 🚀 **TESTING LA CONFIGURACIÓN:**

1. Una vez configurada la API key, genera el APK:
```bash
cd /home/consultora1600/mubitt
./build_apk.sh
```

2. Instala en un dispositivo Android y verifica:
   - ✅ El mapa se carga correctamente
   - ✅ La búsqueda de lugares funciona
   - ✅ No aparece "For development purposes only"

## 🎯 **SIGUIENTE PASO:**
Una vez que tengas la API key configurada, continuamos con el deploy del backend a Railway.

**Status**: ⏳ **CONFIGURAR API KEY Y CONTINUAR**