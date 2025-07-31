package com.mubitt.features.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SupportViewModel - Gestión de soporte y ayuda
 * Características específicas San Juan:
 * - FAQs localizados para San Juan
 * - Soporte en español con modismos argentinos
 * - Integración con oficinas locales
 */
@HiltViewModel
class SupportViewModel @Inject constructor(
    // private val supportRepository: SupportRepository // Inyectar cuando esté disponible
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()
    
    init {
        loadFAQs()
    }
    
    private fun loadFAQs() {
        viewModelScope.launch {
            try {
                // FAQs específicos para San Juan
                val faqs = generateSanJuanFAQs()
                
                _uiState.value = _uiState.value.copy(
                    faqItems = faqs
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar preguntas frecuentes: ${e.message}"
                )
            }
        }
    }
    
    fun toggleFAQ(faqId: String) {
        val currentExpanded = _uiState.value.expandedFAQs.toMutableSet()
        
        if (currentExpanded.contains(faqId)) {
            currentExpanded.remove(faqId)
        } else {
            currentExpanded.add(faqId)
        }
        
        _uiState.value = _uiState.value.copy(
            expandedFAQs = currentExpanded
        )
    }
    
    fun openReportDialog(reportType: String) {
        _uiState.value = _uiState.value.copy(
            showReportDialog = true,
            reportType = reportType
        )
    }
    
    fun closeReportDialog() {
        _uiState.value = _uiState.value.copy(
            showReportDialog = false,
            reportType = null
        )
    }
    
    fun submitReport(description: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSubmittingReport = true)
                
                // TODO: Enviar reporte al backend
                val reportData = mapOf(
                    "type" to (_uiState.value.reportType ?: "GENERAL"),
                    "description" to description,
                    "timestamp" to System.currentTimeMillis(),
                    "location" to "San Juan, Argentina"
                )
                
                // Simular envío
                kotlinx.coroutines.delay(1000)
                
                _uiState.value = _uiState.value.copy(
                    isSubmittingReport = false,
                    showReportDialog = false,
                    reportType = null,
                    successMessage = "Reporte enviado exitosamente. Te contactaremos pronto."
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmittingReport = false,
                    errorMessage = "Error al enviar reporte: ${e.message}"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    private fun generateSanJuanFAQs(): List<FAQItem> {
        return listOf(
            FAQItem(
                id = "faq_1",
                question = "¿Mubitt funciona en toda la provincia de San Juan?",
                answer = "Actualmente operamos en San Juan Capital y los departamentos cercanos: Rivadavia, Chimbas, Santa Lucía y Rawson. Pronto expandiremos a toda la provincia.",
                category = "COVERAGE"
            ),
            FAQItem(
                id = "faq_2",
                question = "¿Puedo pagar en efectivo?",
                answer = "¡Sí! A diferencia de otras apps, en Mubitt aceptamos efectivo sin comisiones adicionales. También aceptamos MercadoPago, tarjetas de crédito y débito.",
                category = "PAYMENT"
            ),
            FAQItem(
                id = "faq_3",
                question = "¿Cómo busco ubicaciones en San Juan?",
                answer = "Puedes usar referencias locales como 'Hospital Rawson', 'UNSJ', 'Shopping del Sol' o descripciones como 'cerca del semáforo de 25 de Mayo'. Nuestro sistema entiende las referencias sanjuaninas.",
                category = "LOCATIONS"
            ),
            FAQItem(
                id = "faq_4",
                question = "¿Los conductores son de San Juan?",
                answer = "Sí, todos nuestros conductores son locales y conocen perfectamente las calles, barrios y referencias de San Juan. Esto garantiza rutas más eficientes y un servicio personalizado.",
                category = "DRIVERS"
            ),
            FAQItem(
                id = "faq_5",
                question = "¿Qué hago si tengo un problema durante el viaje?",
                answer = "Usa el botón de emergencia en la app, llama al conductor directamente, o contactanos al WhatsApp +54 264 123-4567. También puedes compartir tu viaje en tiempo real con contactos de confianza.",
                category = "SAFETY"
            ),
            FAQItem(
                id = "faq_6",
                question = "¿Cuáles son las tarifas en San Juan?",
                answer = "Tarifa base: $300. Por kilómetro: $80. Por minuto: $15. Durante eventos o alta demanda aplicamos un recargo máximo de 1.5x (mucho menor que otras apps).",
                category = "PRICING"
            ),
            FAQItem(
                id = "faq_7",
                question = "¿Hay descuentos para estudiantes de la UNSJ?",
                answer = "Sí, ofrecemos descuentos especiales para estudiantes universitarios. Verifica tu cuenta estudiantil en tu perfil para acceder a tarifas preferenciales.",
                category = "DISCOUNTS"
            ),
            FAQItem(
                id = "faq_8",
                question = "¿Puedo programar viajes con anticipación?",
                answer = "Sí, puedes programar viajes hasta con 7 días de anticipación. Ideal para viajes al aeropuerto, citas médicas en el Hospital Rawson, o clases en la UNSJ.",
                category = "SCHEDULING"
            ),
            FAQItem(
                id = "faq_9",
                question = "¿Qué pasa durante la Fiesta Nacional del Sol?",
                answer = "Durante eventos masivos como la Fiesta del Sol, activamos rutas especiales y zonas de pickup/dropoff para evitar congestión. Las tarifas pueden tener un recargo mínimo.",
                category = "EVENTS"
            ),
            FAQItem(
                id = "faq_10",
                question = "¿Cómo cancelo un viaje?",
                answer = "Puedes cancelar desde la app hasta 2 minutos después de la confirmación sin cargo. Después de ese tiempo puede aplicar una pequeña tarifa de cancelación.",
                category = "CANCELLATION"
            ),
            FAQItem(
                id = "faq_11",
                question = "¿Mubitt está disponible 24/7?",
                answer = "Sí, operamos las 24 horas todos los días. Durante las madrugadas (2:00-6:00 AM) puede haber menos conductores disponibles, pero siempre mantenemos servicio de emergencia.",
                category = "HOURS"
            ),
            FAQItem(
                id = "faq_12",
                question = "¿Puedo elegir siempre el mismo conductor?",
                answer = "Sí, puedes marcar conductores como favoritos y solicitar viajes específicamente con ellos cuando estén disponibles. Esta es una ventaja única de Mubitt.",
                category = "FAVORITES"
            )
        )
    }
}

data class SupportUiState(
    val faqItems: List<FAQItem> = emptyList(),
    val expandedFAQs: Set<String> = emptySet(),
    val showReportDialog: Boolean = false,
    val reportType: String? = null,
    val isSubmittingReport: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)