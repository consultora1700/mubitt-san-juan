# PROJECT_CONTEXT.md
# SAN JUAN RIDE - Contexto Completo del Proyecto

## INFORMACIÓN CRÍTICA - LEER ANTES DE CUALQUIER DESARROLLO

### MERCADO Y UBICACIÓN
- **Ciudad**: San Juan, Argentina
- **Población**: 800,000 habitantes
- **Mercado objetivo**: Usuarios de transporte privado (estimado 15-20% población)
- **Competencia directa**: Uber y Didi (YA operando exitosamente)
- **Competencia indirecta**: Transporte público, remises tradicionales

### HISTORIA DEL MERCADO LOCAL
- **Antecedente de fracaso**: "Remis Oeste" operó por años con software de mala calidad
- **Momento disruptivo**: Llegada de Uber y Didi eliminó completamente a Remis Oeste
- **Lección crítica**: Los sanjuaninos abandonan inmediatamente apps de mala calidad
- **Comportamiento del usuario**: NO dan segundas oportunidades a software deficiente

### OPORTUNIDAD DE MERCADO
- **Ventaja local**: Conocimiento profundo de San Juan vs competencia global
- **Gap identificado**: Uber/Didi no están optimizados para características locales
- **Oportunidad**: Ser la alternativa local superior con mejor UX y features específicos
- **Timing**: Mercado maduro pero con espacio para competidor local de calidad

### ESTÁNDARES DE CALIDAD OBLIGATORIOS

#### UX/UI Requirements:
- **Visual**: Indistinguible de Uber en calidad y pulimiento
- **Performance**: Superior a Uber (optimizado para San Juan)
- **Animaciones**: Suaves, profesionales, sin lag
- **Responsividad**: Instantánea en todas las interacciones
- **Accesibilidad**: Funcional para todas las edades y niveles técnicos

#### Technical Requirements:
- **Crash rate**: <0.1% (tolerancia cero)
- **Load time**: <2 segundos startup
- **Response time**: <200ms UI interactions
- **API calls**: <3 segundos promedio
- **Memory**: Cero leaks, optimización constante

#### Business Requirements:
- **Rating mínimo**: 4.5 estrellas Play Store
- **Retention**: >60% weekly retention
- **Growth**: 500 users mes 1, 2K mes 3, 5K mes 6
- **Revenue**: Comisión 20-25% vs 25-30% de Uber

### DIFERENCIADORES COMPETITIVOS

#### Vs UBER:
1. **Conocimiento local**: Referencias sanjuaninas, calles, barrios
2. **Tarifas justas**: Menos surge pricing abusivo (máximo 1.5x vs 3x)
3. **Soporte local**: Atención en San Juan vs call center extranjero
4. **Agilidad**: Features nuevos en días vs meses
5. **Conductores locales**: Mejor relación con la comunidad

#### Vs DIDI:
1. **UX superior**: Interface más limpia y intuitiva
2. **Performance**: Optimizada específicamente para Argentina
3. **Features locales**: Integración con eventos, universidades, zonas específicas
4. **Pagos locales**: Mejor integración con métodos argentinos

### CARACTERÍSTICAS ESPECÍFICAS PARA SAN JUAN

#### Referencias Locales:
- Búsqueda que entienda: "Cerca del semáforo", "Por la rotonda", "Al lado del shopping"
- Integración con puntos de referencia conocidos
- Zonas por nombres locales: Centro, Desamparados, Rivadavia, etc.

#### Eventos y Temporadas:
- **Fiesta Nacional del Sol**: Surge controlado, rutas especiales
- **Eventos UNSJ**: Descuentos estudiantes, rutas campus
- **Eventos deportivos**: San Martín, Gimnasia, etc.
- **Temporada turística**: Rutas a atractivos, info turística

#### Integración Cultural:
- Soporte para jerga local en direcciones
- Horarios adaptados a costumbres locales
- Referencias geográficas sanjuaninas
- Eventos religiosos y tradicionales

### MODELO DE NEGOCIO

#### Revenue Streams:
1. **Comisión por viaje**: 20-25% (competitivo vs 25-30% Uber)
2. **Delivery integration**: Expansión futura a delivery
3. **Corporate accounts**: Empresas locales
4. **Advertising**: Promociones de comercios locales
5. **Premium features**: Conductores de confianza, programación avanzada

#### Cost Structure:
- Desarrollo y mantenimiento: 40%
- Marketing y adquisición: 30%
- Operaciones y soporte: 20%
- Infraestructura tecnológica: 10%

### MÉTRICAS DE ÉXITO

#### Métricas Técnicas:
- App Store rating: >4.5
- Crash rate: <0.1%
- Load time: <2 segundos
- API response: <3 segundos
- User retention 7 días: >60%

#### Métricas de Negocio:
- Monthly Active Users: 500 (mes 1) → 5,000 (mes 6)
- Rides per day: 50 (mes 1) → 500 (mes 6)
- Driver acquisition: 20 (mes 1) → 150 (mes 6)
- Revenue per ride: $X ARS promedio
- Customer acquisition cost: <$Y ARS

#### Métricas Operacionales:
- Matching time: <5 segundos
- Driver acceptance rate: >80%
- Trip completion rate: >95%
- Customer satisfaction: >4.5/5.0
- Driver satisfaction: >4.0/5.0

### STACK TECNOLÓGICO PREMIUM

#### Frontend Android:
- **Lenguaje**: Kotlin 100%
- **UI Framework**: Jetpack Compose
- **Design System**: Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **Navigation**: Navigation Component
- **DI**: Hilt/Dagger
- **Networking**: Retrofit + OkHttp
- **Maps**: Google Maps SDK
- **Real-time**: WebSockets + Socket.IO
- **Storage**: Room + DataStore
- **Images**: Coil con cache
- **Animations**: Lottie + Compose Animations

#### Backend:
- **Framework**: FastAPI (Python)
- **Database**: PostgreSQL + Redis cache
- **Real-time**: WebSockets
- **Authentication**: JWT + OAuth2
- **File Storage**: AWS S3 o Google Cloud Storage
- **Payment Processing**: MercadoPago + Stripe
- **SMS/Notifications**: Twilio + Firebase
- **Monitoring**: Sentry + Datadog
- **Infrastructure**: Docker + Kubernetes
- **CI/CD**: GitHub Actions

#### Third-party Integrations:
- **Maps**: Google Maps Platform (Directions, Places, Geocoding)
- **Payments**: MercadoPago (local) + Stripe (international)
- **SMS**: Twilio para verificación
- **Push Notifications**: Firebase Cloud Messaging
- **Analytics**: Firebase Analytics + Mixpanel
- **Crash Reporting**: Firebase Crashlytics
- **Performance**: Firebase Performance Monitoring

### CONSIDERACIONES DE SEGURIDAD

#### Data Protection:
- Cumplimiento PDPA Argentina
- Encriptación end-to-end para datos sensibles
- Tokenización de datos de pago
- Anonimización de datos de ubicación históricos

#### User Safety:
- Verificación de conductores con antecedentes
- Tracking en tiempo real compartible
- Botón de emergencia integrado
- Grabación de audio/video opcional
- Sistema de calificaciones transparente

#### Technical Security:
- HTTPS obligatorio
- API rate limiting
- Input validation estricta
- SQL injection prevention
- XSS protection
- CSRF tokens

### PLAN DE LAUNCH

#### Pre-Launch (Mes 0):
- Beta testing con 50 usuarios locales
- Onboarding de 20 conductores iniciales
- Partnership con 2-3 empresas locales
- Marketing digital preparación

#### Soft Launch (Mes 1):
- Lanzamiento controlado en zona Centro
- 500 usuarios objetivo
- Soporte 12hs/día
- Iteración rápida basada en feedback

#### Full Launch (Mes 2-3):
- Expansión a toda la ciudad
- Marketing intensivo
- 2,000 usuarios objetivo
- Soporte 24/7

#### Expansion (Mes 6+):
- Features premium
- Delivery integration
- Corporate accounts
- 5,000+ usuarios objetivo

### RIESGOS Y MITIGACIÓN

#### Riesgos Técnicos:
- **Escalabilidad**: Arquitectura microservicios desde día 1
- **Performance**: Testing continuo y optimización
- **Security breaches**: Auditorías de seguridad regulares

#### Riesgos de Mercado:
- **Respuesta competitiva**: Diferenciación fuerte y agilidad
- **Regulación**: Compliance proactivo con autoridades
- **Economic downturn**: Modelo de costos variable

#### Riesgos Operacionales:
- **Driver churn**: Programa de incentivos competitivo
- **Quality issues**: QA exhaustivo y monitoring 24/7
- **Customer service**: Team local especializado

## RECORDATORIO FINAL

**ESTE PROYECTO TIENE UN SOLO OBJETIVO: CREAR UNA APLICACIÓN QUE LOS SANJUANINOS PREFIERAN USAR SOBRE UBER Y DIDI. CADA DECISIÓN TÉCNICA, CADA LÍNEA DE CÓDIGO, CADA PIXEL DE DISEÑO DEBE ESTAR ORIENTADO A SUPERAR LA EXPERIENCIA QUE BRINDAN ESTAS APPS GLOBALES.**