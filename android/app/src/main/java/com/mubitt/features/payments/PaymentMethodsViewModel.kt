package com.mubitt.features.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PaymentMethodsViewModel - Gestión de métodos de pago
 * Características específicas San Juan:
 * - Efectivo como opción principal (diferenciador vs Uber)
 * - MercadoPago como método preferido argentino
 * - Validaciones específicas para tarjetas argentinas
 */
@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    // private val paymentRepository: PaymentRepository // Inyectar cuando esté disponible
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()
    
    init {
        loadPaymentMethods()
    }
    
    private fun loadPaymentMethods() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Simulación de datos - reemplazar con repository real
                val defaultMethod = PaymentMethod(
                    id = "cash_default",
                    type = PaymentMethodType.CASH,
                    displayName = "Efectivo",
                    description = "Paga al conductor directamente",
                    isDefault = true
                )
                
                val otherMethods = listOf(
                    PaymentMethod(
                        id = "mp_001",
                        type = PaymentMethodType.MERCADOPAGO,
                        displayName = "MercadoPago",
                        description = "Billetera digital argentina"
                    ),
                    PaymentMethod(
                        id = "visa_001",
                        type = PaymentMethodType.CREDIT_CARD,
                        displayName = "Visa",
                        lastFourDigits = "4532",
                        description = "Tarjeta de crédito"
                    ),
                    PaymentMethod(
                        id = "mastercard_001",
                        type = PaymentMethodType.DEBIT_CARD,
                        displayName = "Mastercard Débito",
                        lastFourDigits = "8901",
                        description = "Tarjeta de débito"
                    )
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    defaultPaymentMethod = defaultMethod,
                    otherPaymentMethods = otherMethods
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar métodos de pago: ${e.message}"
                )
            }
        }
    }
    
    fun selectPaymentMethod(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            try {
                // Actualizar método por defecto
                val currentState = _uiState.value
                val updatedOtherMethods = currentState.otherPaymentMethods.toMutableList()
                
                // Remover el método seleccionado de la lista de otros
                updatedOtherMethods.removeAll { it.id == paymentMethod.id }
                
                // Agregar el método anterior por defecto a la lista de otros
                if (currentState.defaultPaymentMethod.id != "cash_default") {
                    updatedOtherMethods.add(0, currentState.defaultPaymentMethod)
                }
                
                _uiState.value = currentState.copy(
                    defaultPaymentMethod = paymentMethod.copy(isDefault = true),
                    otherPaymentMethods = updatedOtherMethods
                )
                
                // TODO: Sincronizar con backend
                // paymentRepository.setDefaultPaymentMethod(paymentMethod.id)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al seleccionar método de pago: ${e.message}"
                )
            }
        }
    }
    
    fun addPaymentMethod(type: PaymentMethodType, details: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isAddingPaymentMethod = true)
                
                // Validaciones específicas
                when (type) {
                    PaymentMethodType.CREDIT_CARD, PaymentMethodType.DEBIT_CARD -> {
                        if (!isValidCardDetails(details)) {
                            _uiState.value = _uiState.value.copy(
                                isAddingPaymentMethod = false,
                                errorMessage = "Datos de tarjeta inválidos"
                            )
                            return@launch
                        }
                    }
                    else -> { /* No validation needed for cash/mercadopago */ }
                }
                
                // Crear nuevo método de pago
                val newMethod = PaymentMethod(
                    id = "new_${System.currentTimeMillis()}",
                    type = type,
                    displayName = getDisplayNameForType(type),
                    lastFourDigits = extractLastFourDigits(details),
                    description = getDescriptionForType(type)
                )
                
                // Agregar a la lista
                val currentState = _uiState.value
                val updatedOtherMethods = currentState.otherPaymentMethods.toMutableList()
                updatedOtherMethods.add(0, newMethod)
                
                _uiState.value = currentState.copy(
                    isAddingPaymentMethod = false,
                    showAddPaymentDialog = false,
                    otherPaymentMethods = updatedOtherMethods
                )
                
                // TODO: Sincronizar con backend
                // paymentRepository.addPaymentMethod(newMethod)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAddingPaymentMethod = false,
                    errorMessage = "Error al agregar método de pago: ${e.message}"
                )
            }
        }
    }
    
    fun removePaymentMethod(paymentMethodId: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val updatedOtherMethods = currentState.otherPaymentMethods
                    .filter { it.id != paymentMethodId }
                
                _uiState.value = currentState.copy(
                    otherPaymentMethods = updatedOtherMethods
                )
                
                // TODO: Sincronizar con backend
                // paymentRepository.removePaymentMethod(paymentMethodId)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al eliminar método de pago: ${e.message}"
                )
            }
        }
    }
    
    fun showAddPaymentDialog() {
        _uiState.value = _uiState.value.copy(showAddPaymentDialog = true)
    }
    
    fun hideAddPaymentDialog() {
        _uiState.value = _uiState.value.copy(showAddPaymentDialog = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    // Validaciones específicas para San Juan/Argentina
    private fun isValidCardDetails(details: String): Boolean {
        // Validación básica - en producción usar validaciones más robustas
        return details.isNotEmpty() && details.length >= 4
    }
    
    private fun getDisplayNameForType(type: PaymentMethodType): String {
        return when (type) {
            PaymentMethodType.CASH -> "Efectivo"
            PaymentMethodType.MERCADOPAGO -> "MercadoPago"
            PaymentMethodType.CREDIT_CARD -> "Tarjeta de Crédito"
            PaymentMethodType.DEBIT_CARD -> "Tarjeta de Débito"
        }
    }
    
    private fun getDescriptionForType(type: PaymentMethodType): String {
        return when (type) {
            PaymentMethodType.CASH -> "Paga al conductor directamente"
            PaymentMethodType.MERCADOPAGO -> "Billetera digital argentina"
            PaymentMethodType.CREDIT_CARD -> "Tarjeta de crédito"
            PaymentMethodType.DEBIT_CARD -> "Tarjeta de débito"
        }
    }
    
    private fun extractLastFourDigits(details: String): String {
        // Extraer últimos 4 dígitos si es una tarjeta
        val digits = details.filter { it.isDigit() }
        return if (digits.length >= 4) digits.takeLast(4) else ""
    }
}

data class PaymentMethodsUiState(
    val isLoading: Boolean = false,
    val isAddingPaymentMethod: Boolean = false,
    val defaultPaymentMethod: PaymentMethod = PaymentMethod(
        id = "cash_default",
        type = PaymentMethodType.CASH,
        displayName = "Efectivo",
        description = "Paga al conductor directamente",
        isDefault = true
    ),
    val otherPaymentMethods: List<PaymentMethod> = emptyList(),
    val showAddPaymentDialog: Boolean = false,
    val errorMessage: String? = null
)