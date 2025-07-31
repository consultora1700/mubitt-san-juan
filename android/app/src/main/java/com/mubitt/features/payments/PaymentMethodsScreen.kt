package com.mubitt.features.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mubitt.shared.theme.Spacing

/**
 * PaymentMethodsScreen - Gestión de métodos de pago
 * Características específicas San Juan:
 * - Efectivo como opción principal (diferenciador vs Uber)
 * - MercadoPago integración (método preferido argentino)
 * - Tarjetas nacionales e internacionales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    navController: NavController,
    viewModel: PaymentMethodsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Métodos de pago",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            item {
                Spacer(modifier = Modifier.height(Spacing.small))
            }
            
            // Método de pago principal
            item {
                Text(
                    text = "Método principal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = Spacing.small)
                )
            }
            
            item {
                PaymentMethodCard(
                    paymentMethod = uiState.defaultPaymentMethod,
                    isSelected = true,
                    onClick = { /* Ya seleccionado */ }
                )
            }
            
            // Otros métodos de pago
            item {
                Text(
                    text = "Otros métodos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        top = Spacing.large,
                        bottom = Spacing.small
                    )
                )
            }
            
            items(uiState.otherPaymentMethods) { paymentMethod ->
                PaymentMethodCard(
                    paymentMethod = paymentMethod,
                    isSelected = false,
                    onClick = { viewModel.selectPaymentMethod(paymentMethod) }
                )
            }
            
            // Agregar nuevo método
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showAddPaymentDialog() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp, 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(Spacing.medium))
                        
                        Text(
                            text = "Agregar método de pago",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Información sobre métodos de pago en San Juan
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(Spacing.small))
                            
                            Text(
                                text = "Métodos aceptados en San Juan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(Spacing.small))
                        
                        Text(
                            text = "• Efectivo (sin comisiones adicionales)\n" +
                                  "• MercadoPago (método preferido)\n" +
                                  "• Tarjetas Visa/Mastercard nacionales\n" +
                                  "• Tarjetas internacionales aceptadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(Spacing.large))
            }
        }
    }
    
    // Dialog para agregar método de pago
    if (uiState.showAddPaymentDialog) {
        AddPaymentMethodDialog(
            onDismiss = { viewModel.hideAddPaymentDialog() },
            onAddPaymentMethod = { type, details ->
                viewModel.addPaymentMethod(type, details)
            }
        )
    }
}

@Composable
private fun PaymentMethodCard(
    paymentMethod: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono del método de pago
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (paymentMethod.type) {
                            PaymentMethodType.CASH -> Color(0xFF4CAF50)
                            PaymentMethodType.MERCADOPAGO -> Color(0xFF009EE3)
                            PaymentMethodType.CREDIT_CARD -> Color(0xFF1976D2)
                            PaymentMethodType.DEBIT_CARD -> Color(0xFF7B1FA2)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (paymentMethod.type) {
                        PaymentMethodType.CASH -> Icons.Default.Money
                        PaymentMethodType.MERCADOPAGO -> Icons.Default.AccountBalance
                        PaymentMethodType.CREDIT_CARD -> Icons.Default.CreditCard
                        PaymentMethodType.DEBIT_CARD -> Icons.Default.Payment
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.medium))
            
            // Información del método
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = paymentMethod.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (paymentMethod.lastFourDigits.isNotEmpty()) {
                    Text(
                        text = "•••• ${paymentMethod.lastFourDigits}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (paymentMethod.description.isNotEmpty()) {
                    Text(
                        text = paymentMethod.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Indicador de selección
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPaymentMethodDialog(
    onDismiss: () -> Unit,
    onAddPaymentMethod: (PaymentMethodType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf<PaymentMethodType?>(null) }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar método de pago",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                // Selección de tipo
                Text(
                    text = "Selecciona el tipo:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                LazyColumn(
                    modifier = Modifier.height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    items(PaymentMethodType.values()) { type ->
                        FilterChip(
                            onClick = { selectedType = type },
                            label = {
                                Text(
                                    text = when (type) {
                                        PaymentMethodType.CASH -> "Efectivo"
                                        PaymentMethodType.MERCADOPAGO -> "MercadoPago"
                                        PaymentMethodType.CREDIT_CARD -> "Tarjeta de Crédito"
                                        PaymentMethodType.DEBIT_CARD -> "Tarjeta de Débito"
                                    }
                                )
                            },
                            selected = selectedType == type,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Campos para tarjetas
                if (selectedType == PaymentMethodType.CREDIT_CARD || 
                    selectedType == PaymentMethodType.DEBIT_CARD) {
                    
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Número de tarjeta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("MM/AA") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it },
                            label = { Text("CVV") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    OutlinedTextField(
                        value = cardHolderName,
                        onValueChange = { cardHolderName = it },
                        label = { Text("Nombre del titular") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedType?.let { type ->
                        val details = when (type) {
                            PaymentMethodType.CASH -> "Efectivo"
                            PaymentMethodType.MERCADOPAGO -> "MercadoPago"
                            else -> "$cardHolderName - ${cardNumber.takeLast(4)}"
                        }
                        onAddPaymentMethod(type, details)
                    }
                },
                enabled = selectedType != null
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Data classes
data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val displayName: String,
    val lastFourDigits: String = "",
    val description: String = "",
    val isDefault: Boolean = false
)

enum class PaymentMethodType {
    CASH,
    MERCADOPAGO,
    CREDIT_CARD,
    DEBIT_CARD
}