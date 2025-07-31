# 🧪 Beta Testing Setup - Mubitt San Juan

## ✅ PREPARACIÓN PARA BETA TESTING

### 📱 **APK GENERATION:**

#### **Debug APK (Para Testing Inmediato):**
```bash
cd /home/consultora1600/mubitt/android
./gradlew app:assembleDebug

# APK generado en:
# app/build/outputs/apk/debug/app-debug.apk
```

#### **Staging APK (Para Beta Testers):**
```bash
./gradlew app:assembleStaging

# APK generado en:
# app/build/outputs/apk/staging/app-staging.apk
```

### 🗝️ **CONFIGURACIÓN API KEYS:**

#### **Google Maps API Key:**
1. **Crear proyecto** en Google Cloud Console: "Mubitt Beta San Juan"
2. **Habilitar APIs:**
   - Maps SDK for Android
   - Places API
   - Directions API
   - Geocoding API
3. **Crear API Key** y configurar restricciones:
   - Package name: `com.mubitt.app.staging`
   - SHA-1 del certificado debug
4. **Configurar en local.properties:**
```properties
GOOGLE_MAPS_API_KEY=AIzaSy[TU_API_KEY_AQUI]
```

### 🖥️ **BACKEND DEPLOYMENT:**

#### **Opción 1: Backend Local (Testing Rápido):**
```bash
# Iniciar backend local
cd /home/consultora1600/mubitt/backend
source venv/bin/activate
python3 start_server.py

# Configurar Android para usar IP local:
# En NetworkModule.kt cambiar BASE_URL a:
# "http://192.168.1.X:8000/" (tu IP local)
```

#### **Opción 2: Deploy en Cloud (Recomendado):**
**Railway, Heroku, o DigitalOcean:**
```bash
# Deploy a Railway (gratis para testing)
railway login
railway init mubitt-api
railway up
```

### 👥 **BETA TESTERS TARGET (San Juan):**

#### **Perfil de Beta Testers:**
- **Cantidad**: 20-30 usuarios iniciales
- **Ubicación**: San Juan capital y alrededores
- **Perfil**: 
  - Usuarios actuales de Uber/Didi
  - Edades 18-45 años
  - Uso frecuente de smartphones
  - Residentes de San Juan mínimo 1 año

#### **Criterios de Selección:**
- ✅ Conocimiento de referencias locales
- ✅ Uso actual de apps de transporte
- ✅ Disponibilidad para dar feedback
- ✅ Conexión a WiFi/datos estables

### 📋 **DISTRIBUCIÓN BETA:**

#### **Google Play Console (Recommended):**
```
1. Crear cuenta Google Play Developer ($25 USD)
2. Subir APK staging a "Internal Testing"
3. Crear lista de beta testers por email
4. Enviar invitaciones automáticas
```

#### **Distribución Directa APK:**
```
1. Generar APK staging firmado
2. Subir a Google Drive/Dropbox
3. Enviar link directo a beta testers
4. Incluir instrucciones de instalación
```

#### **Firebase App Distribution (Gratis):**
```bash
# Configurar Firebase App Distribution
firebase login
firebase init appdistribution
firebase appdistribution:distribute app-staging.apk \
  --app 1:123456789:android:abcd1234 \
  --groups "beta-testers-san-juan"
```

### 🎯 **TESTING SCENARIOS:**

#### **Casos de Uso Críticos:**
1. **Registro de usuario** con número argentino
2. **Login** con email y teléfono
3. **Solicitar viaje** de Hospital Rawson a UNSJ
4. **Búsqueda de ubicaciones** por referencias San Juan
5. **Estimación de tarifa** comparar con Uber
6. **Cancelación de viaje** dentro de 2 minutos
7. **Historial de viajes** navegación

#### **Testing de Referencias Locales:**
- ✅ "Hospital Rawson" → debe autocompletar
- ✅ "UNSJ" → debe encontrar Universidad
- ✅ "Plaza 25 de Mayo" → centro de San Juan
- ✅ "Shopping del Sol" → mall principal
- ✅ "Cerca del semáforo" → búsqueda inteligente

### 📊 **MÉTRICAS A MEDIR:**

#### **Technical Metrics:**
- App startup time: <2 segundos
- Login success rate: >95%
- Trip creation time: <30 segundos
- Maps loading time: <3 segundos
- Crash rate: <0.1%

#### **User Experience Metrics:**
- Task completion rate: >90%
- User satisfaction: >4.0/5.0
- Feature adoption: >60%
- Daily active users retention: >70%

#### **San Juan Specific Metrics:**
- Local reference recognition: >80%
- Fare comparison vs Uber: within 15%
- Zone coverage: all 6 districts

### 📝 **FEEDBACK COLLECTION:**

#### **In-App Feedback:**
```kotlin
// Implementar feedback dialog después de cada viaje
FeedbackDialog(
    onSubmit = { rating, comment ->
        // Enviar a analytics
    }
)
```

#### **External Feedback:**
- **Google Form** para feedback detallado
- **WhatsApp group** para beta testers
- **Weekly video calls** con usuarios activos

### 🛠️ **DEBUGGING & SUPPORT:**

#### **Debug Information:**
```kotlin
// Habilitar logs detallados en staging
BuildConfig.DEBUG = true
BuildConfig.ENABLE_ANALYTICS = true
BuildConfig.ENABLE_CRASH_REPORTING = true
```

#### **Support Channels:**
- **Email**: beta@mubitt.com
- **WhatsApp**: +54 264 XXX-XXXX
- **Telegram**: @MubittSanJuan

### 🎆 **LAUNCH CHECKLIST:**

#### **Pre-Launch (1 semana antes):**
- [ ] APK staging generado y testing
- [ ] Google Maps API configurado y funcionando
- [ ] Backend deployed y estable
- [ ] Beta testers contactados y confirmados
- [ ] Documentación de testing creada
- [ ] Support channels configurados

#### **Launch Day:**
- [ ] APK distribuido a todos los beta testers
- [ ] Instrucciones de instalación enviadas
- [ ] Backend monitoring activado
- [ ] Support team en standby
- [ ] First day feedback collection

#### **Post-Launch (primera semana):**
- [ ] Daily usage metrics monitoring
- [ ] Bug reports collection y fixing
- [ ] User feedback analysis
- [ ] Performance optimization
- [ ] Preparation for public launch

## 🚀 **BETA TESTING TIMELINE:**

### **Week 1: Setup**
- Google Maps API setup
- Backend deployment
- APK generation
- Beta tester recruitment

### **Week 2: Distribution**
- APK distribution to beta testers
- Installation support
- First usage monitoring
- Critical bug fixes

### **Week 3: Feedback**
- Feature testing completion
- Feedback collection and analysis
- Performance optimization
- UI/UX improvements

### **Week 4: Iteration**
- Bug fixes implementation
- New features based on feedback
- Preparation for public launch
- App Store submission preparation

---

## 🎯 **OBJETIVO BETA TESTING:**

**✅ Validar producto-mercado fit en San Juan**  
**✅ Optimizar UX para usuarios locales**  
**✅ Asegurar performance en dispositivos reales**  
**✅ Preparar para competir con Uber/Didi**  

**🏆 SUCCESS CRITERIA: >4.0 rating, <5% crash rate, 70% retention**