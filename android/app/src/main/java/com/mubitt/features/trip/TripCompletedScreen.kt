package com.mubitt.features.trip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mubitt.core.domain.model.Driver
import com.mubitt.core.domain.model.Trip
import java.text.SimpleDateFormat
import java.util.*

/**
 * TripCompletedScreen - Pantalla de finalización de viaje
 * Permite calificar conductor, agregar propina, ver resumen y solicitar nuevo viaje
 * Optimizada para generar satisfacción y retención en San Juan
 */
@Composable
fun TripCompletedScreen(
    trip: Trip,
    onNavigateToHome: () -> Unit,
    onRequestNewTrip: () -> Unit,
    onNavigateToSupport: () -> Unit,
    viewModel: TripCompletedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar datos del viaje
    LaunchedEffect(trip.id) {
        viewModel.loadTripDetails(trip)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header de celebración
        TripCompletionHeader()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Resumen del viaje
        TripSummaryCard(
            trip = trip,
            duration = uiState.tripDuration,
            distance = uiState.tripDistance
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Información del conductor
        uiState.driver?.let { driver ->
            DriverCompletionCard(
                driver = driver,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        
        // Sistema de calificación
        RatingSection(
            currentRating = uiState.driverRating,
            onRatingChange = viewModel::updateDriverRating,
            comment = uiState.ratingComment,
            onCommentChange = viewModel::updateRatingComment
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sistema de propinas
        TipSection(
            selectedTip = uiState.selectedTip,
            customTip = uiState.customTip,
            onTipSelected = viewModel::updateTip,
            onCustomTipChange = viewModel::updateCustomTip,
            totalFare = trip.fare
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Método de pago confirmado
        PaymentConfirmationCard(
            trip = trip,
            tip = uiState.selectedTip,
            customTip = uiState.customTip
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Acciones principales
        TripCompletionActions(
            onSubmitRating = {
                viewModel.submitRating(
                    onSuccess = { 
                        // Rating enviado exitosamente
                    }
                )
            },
            onRequestNewTrip = onRequestNewTrip,
            onGoToHome = onNavigateToHome,
            onReportIssue = onNavigateToSupport,
            isSubmitting = uiState.isSubmittingRating,
            canSubmit = uiState.canSubmitRating
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mensaje de agradecimiento específico San Juan
        SanJuanAppreciationMessage()
    }
}

@Composable
private fun TripCompletionHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "¡Viaje completado!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Llegaste seguro a tu destino",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TripSummaryCard(
    trip: Trip,
    duration: String?,
    distance: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Resumen del viaje",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Ruta del viaje
            TripRouteInfo(
                from = trip.pickupLocation.address,
                to = trip.dropoffLocation.address,
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(trip.createdAt),
                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(trip.createdAt)
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Detalles del viaje
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TripDetailItem(
                    icon = Icons.Default.Schedule,
                    label = "Duración",
                    value = duration ?: "N/A"
                )
                
                TripDetailItem(
                    icon = Icons.Default.Straighten,
                    label = "Distancia",
                    value = distance ?: "N/A"
                )
                
                TripDetailItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Tarifa",
                    value = "$${String.format("%.2f", trip.fare)}"
                )
            }
        }
    }
}

@Composable
private fun TripRouteInfo(
    from: String,
    to: String,
    time: String,
    date: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.RadioButtonChecked,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Desde",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = from,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$time • $date",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        // Línea conectora
        Box(
            modifier = Modifier
                .padding(start = 6.dp, top = 4.dp, bottom = 4.dp)
                .width(1.dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hasta",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = to,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TripDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DriverCompletionCard(
    driver: Driver,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del conductor
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = driver.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${driver.rating} • ${driver.vehicle.make} ${driver.vehicle.model}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Text(
                    text = "Gracias por elegir a ${driver.name.split(" ").first()}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun RatingSection(
    currentRating: Int,
    onRatingChange: (Int) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "¿Cómo fue tu experiencia?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Estrellas de rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(5) { index ->
                    val starIndex = index + 1
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Estrella $starIndex",
                        tint = if (starIndex <= currentRating) Color(0xFFFFB000) 
                               else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onRatingChange(starIndex) }
                            .padding(4.dp)
                    )
                }
            }
            
            // Mensaje de rating
            val ratingMessage = when (currentRating) {
                1 -> "Muy malo"
                2 -> "Malo"
                3 -> "Regular"
                4 -> "Bueno"
                5 -> "Excelente"
                else -> "Califica tu viaje"
            }
            
            Text(
                text = ratingMessage,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            // Campo de comentario opcional
            AnimatedVisibility(visible = currentRating > 0) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = onCommentChange,
                        label = { Text("Comentario (opcional)") },
                        placeholder = { Text("Cuéntanos más sobre tu experiencia...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TipSection(
    selectedTip: Double,
    customTip: String,
    onTipSelected: (Double) -> Unit,
    onCustomTipChange: (String) -> Unit,
    totalFare: Double
) {
    val tipOptions = listOf(0.0, totalFare * 0.10, totalFare * 0.15, totalFare * 0.20)
    val tipLabels = listOf("Sin propina", "10%", "15%", "20%")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Agregar propina (opcional)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Opciones de propina predefinidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tipOptions.forEachIndexed { index, tipAmount ->
                    TipOption(
                        label = tipLabels[index],
                        amount = if (tipAmount > 0) "$${String.format("%.0f", tipAmount)}" else "",
                        isSelected = selectedTip == tipAmount && customTip.isEmpty(),
                        onClick = { 
                            onTipSelected(tipAmount)
                            onCustomTipChange("")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Propina personalizada
            OutlinedTextField(
                value = customTip,
                onValueChange = { 
                    onCustomTipChange(it)
                    if (it.isNotEmpty()) onTipSelected(0.0)
                },
                label = { Text("Propina personalizada") },
                leadingIcon = { Text("$") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun TipOption(
    label: String,
    amount: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                           else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            2.dp, MaterialTheme.colorScheme.primary
        ) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            if (amount.isNotEmpty()) {
                Text(
                    text = amount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PaymentConfirmationCard(
    trip: Trip,
    tip: Double,
    customTip: String
) {
    val tipAmount = if (customTip.isNotEmpty()) {
        customTip.toDoubleOrNull() ?: 0.0
    } else {
        tip
    }
    
    val totalAmount = trip.fare + tipAmount
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pago confirmado",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Desglose de pago
            PaymentBreakdownItem("Tarifa del viaje", trip.fare)
            if (tipAmount > 0) {
                PaymentBreakdownItem("Propina", tipAmount)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${String.format("%.2f", totalAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = "Cobrado a tu método de pago",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun PaymentBreakdownItem(
    label: String,
    amount: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "$${String.format("%.2f", amount)}",
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TripCompletionActions(
    onSubmitRating: () -> Unit,
    onRequestNewTrip: () -> Unit,
    onGoToHome: () -> Unit,
    onReportIssue: () -> Unit,
    isSubmitting: Boolean,
    canSubmit: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Enviar calificación
        Button(
            onClick = onSubmitRating,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = canSubmit && !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Enviar calificación",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Solicitar nuevo viaje
        OutlinedButton(
            onClick = onRequestNewTrip,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Solicitar otro viaje",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Acciones secundarias
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onGoToHome,
                modifier = Modifier.weight(1f)
            ) {
                Text("Ir a inicio")
            }
            
            OutlinedButton(
                onClick = onReportIssue,
                modifier = Modifier.weight(1f)
            ) {
                Text("Reportar problema")
            }
        }
    }
}

@Composable
private fun SanJuanAppreciationMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡Gracias por elegir Mubitt!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Juntos hacemos de San Juan una ciudad más conectada 🚗💚",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}