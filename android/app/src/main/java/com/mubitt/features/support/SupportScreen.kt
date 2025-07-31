package com.mubitt.features.support

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mubitt.shared.theme.Spacing

/**
 * SupportScreen - Soporte y ayuda
 * Características específicas San Juan:
 * - Soporte local 24/7 en San Juan
 * - WhatsApp nativo (preferido por sanjuaninos)
 * - FAQs específicos para San Juan
 * - Contacto directo con oficinas locales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    navController: NavController,
    viewModel: SupportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Ayuda y soporte",
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // Header de bienvenida
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(Spacing.small))
                        
                        Text(
                            text = "¿Cómo podemos ayudarte?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Soporte local 24/7 en San Juan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Contacto rápido
            item {
                Text(
                    text = "Contacto rápido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Opciones de contacto
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    // WhatsApp (preferido en San Juan)
                    SupportOptionCard(
                        icon = Icons.Default.Chat,
                        title = "WhatsApp",
                        description = "Respuesta inmediata - Preferido por sanjuaninos",
                        action = "+54 264 123-4567",
                        backgroundColor = Color(0xFF25D366).copy(alpha = 0.1f),
                        iconTint = Color(0xFF25D366),
                        onClick = {
                            uriHandler.openUri("https://wa.me/5426412344567?text=Hola, necesito ayuda con Mubitt")
                        }
                    )
                    
                    // Teléfono local
                    SupportOptionCard(
                        icon = Icons.Default.Phone,
                        title = "Teléfono San Juan",
                        description = "Atención local 24/7",
                        action = "264 423-5678",
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            uriHandler.openUri("tel:+542644235678")
                        }
                    )
                    
                    // Email
                    SupportOptionCard(
                        icon = Icons.Default.Email,
                        title = "Email",
                        description = "Respuesta en 2-4 horas",
                        action = "ayuda@mubitt.com.ar",
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            uriHandler.openUri("mailto:ayuda@mubitt.com.ar?subject=Consulta desde la app")
                        }
                    )
                }
            }
            
            // Oficina física en San Juan
            item {
                Text(
                    text = "Oficina en San Juan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Spacing.medium)
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.width(Spacing.small))
                            
                            Text(
                                text = "Centro de atención Mubitt",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(Spacing.small))
                        
                        Text(
                            text = "Av. San Martín 123, Centro\nSan Juan Capital, Argentina",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "Lunes a Viernes: 8:00 - 20:00\nSábados: 9:00 - 14:00",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = Spacing.small)
                        )
                    }
                }
            }
            
            // FAQs
            item {
                Text(
                    text = "Preguntas frecuentes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Spacing.medium)
                )
            }
            
            items(uiState.faqItems) { faq ->
                FAQCard(
                    faq = faq,
                    isExpanded = uiState.expandedFAQs.contains(faq.id),
                    onToggle = { viewModel.toggleFAQ(faq.id) }
                )
            }
            
            // Reportar problema
            item {
                Text(
                    text = "Reportar problema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Spacing.medium)
                )
            }
            
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    ReportOptionCard(
                        icon = Icons.Default.ReportProblem,
                        title = "Problema con viaje",
                        description = "Conductor, ruta, pago, etc.",
                        onClick = { viewModel.openReportDialog("TRIP_ISSUE") }
                    )
                    
                    ReportOptionCard(
                        icon = Icons.Default.BugReport,
                        title = "Error en la app",
                        description = "Pantalla en blanco, crashes, etc.",
                        onClick = { viewModel.openReportDialog("APP_BUG") }
                    )
                    
                    ReportOptionCard(
                        icon = Icons.Default.Security,
                        title = "Problema de seguridad",
                        description = "Situación riesgosa o sospechosa",
                        onClick = { viewModel.openReportDialog("SECURITY_ISSUE") }
                    )
                }
            }
            
            // Información adicional
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium)
                    ) {
                        Text(
                            text = "💡 Tips de Mubitt San Juan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(Spacing.small))
                        
                        Text(
                            text = "• Usa referencias locales: \"Hospital Rawson\", \"UNSJ\"\n" +
                                  "• El efectivo no tiene comisión adicional\n" +
                                  "• Comparte tu viaje para mayor seguridad\n" +
                                  "• Califica a tu conductor para mejorar el servicio",
                            style = MaterialTheme.typography.bodySmall,
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
    
    // Dialog para reportar problemas
    if (uiState.showReportDialog) {
        ReportProblemDialog(
            reportType = uiState.reportType,
            onDismiss = { viewModel.closeReportDialog() },
            onSubmit = { description -> viewModel.submitReport(description) }
        )
    }
}

@Composable
private fun SupportOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    action: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.medium))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = action,
                    style = MaterialTheme.typography.bodyMedium,
                    color = iconTint,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun FAQCard(
    faq: FAQItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir"
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(Spacing.small))
                
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ReportOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(Spacing.medium))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun ReportProblemDialog(
    reportType: String?,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (reportType) {
                    "TRIP_ISSUE" -> "Reportar problema con viaje"
                    "APP_BUG" -> "Reportar error en la app"
                    "SECURITY_ISSUE" -> "Reportar problema de seguridad"
                    else -> "Reportar problema"
                }
            )
        },
        text = {
            Column {
                Text(
                    text = "Describe el problema en detalle:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(Spacing.small))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("¿Qué ocurrió?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(description) },
                enabled = description.isNotBlank()
            ) {
                Text("Enviar reporte")
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
data class FAQItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: String = ""
)