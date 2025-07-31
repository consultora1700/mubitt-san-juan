package com.mubitt.features.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * OnboardingScreen - Primera experiencia para nuevos usuarios
 * 3 slides optimizados para mostrar las ventajas de Mubitt en San Juan
 * Diseño premium que rivaliza con apps globales
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    
    val onboardingPages = remember {
        listOf(
            OnboardingPage(
                icon = Icons.Default.Waving_Hand,
                title = "¡Bienvenido a Mubitt!",
                subtitle = "Tu nueva forma de viajar por San Juan",
                description = "Conectamos a los sanjuaninos con conductores locales de confianza. Tarifas justas, viajes seguros, experiencia premium.",
                primaryColor = Color(0xFF006C4C),
                backgroundColor = Color(0xFFF0FDF4)
            ),
            OnboardingPage(
                icon = Icons.Default.LocationCity,
                title = "Conocemos San Juan",
                subtitle = "Referencias locales que entiendes",
                description = "Busca por \"Hospital Rawson\", \"UNSJ\", \"cerca del semáforo\". Nuestros conductores conocen cada rincón de la ciudad.",
                primaryColor = Color(0xFF0066CC),
                backgroundColor = Color(0xFFF0F8FF)
            ),
            OnboardingPage(
                icon = Icons.Default.Security,
                title = "Viaja con confianza",
                subtitle = "Conductores verificados y soporte local",
                description = "Todos nuestros conductores están verificados. Soporte 24/7 en San Juan, tracking en tiempo real y botón de emergencia.",
                primaryColor = Color(0xFF7C3AED),
                backgroundColor = Color(0xFFFAF5FF)
            )
        )
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Pager con las páginas de onboarding
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(
                page = onboardingPages[page],
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Controles en la parte inferior
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth()
        ) {
            // Indicadores de página
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { index ->
                    PageIndicator(
                        isSelected = index == pagerState.currentPage,
                        color = onboardingPages[pagerState.currentPage].primaryColor
                    )
                    if (index < onboardingPages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Skip/Atrás
                if (pagerState.currentPage == 0) {
                    TextButton(
                        onClick = onSkip,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            text = "Saltar",
                            fontSize = 16.sp
                        )
                    }
                } else {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = onboardingPages[pagerState.currentPage].primaryColor
                        )
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Atrás",
                            fontSize = 16.sp
                        )
                    }
                }
                
                // Botón Siguiente/Comenzar
                Button(
                    onClick = {
                        if (pagerState.currentPage == onboardingPages.size - 1) {
                            onComplete()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = onboardingPages[pagerState.currentPage].primaryColor
                    ),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == onboardingPages.size - 1) "¡Comenzar!" else "Siguiente",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(page.backgroundColor)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        // Icono principal
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    page.primaryColor.copy(alpha = 0.1f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                page.icon,
                contentDescription = null,
                tint = page.primaryColor,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Título principal
        Text(
            text = page.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = page.primaryColor,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Subtítulo
        Text(
            text = page.subtitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Descripción
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Características específicas por página
        when (page.title) {
            "¡Bienvenido a Mubitt!" -> {
                WelcomeFeatures()
            }
            "Conocemos San Juan" -> {
                SanJuanFeatures()
            }
            "Viaja con confianza" -> {
                SafetyFeatures()
            }
        }
    }
}

@Composable
private fun WelcomeFeatures() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureItem(
                icon = Icons.Default.LocalTaxi,
                title = "Conductores locales",
                description = "Conocen cada calle de San Juan"
            )
            
            FeatureItem(
                icon = Icons.Default.AttachMoney,
                title = "Tarifas justas",
                description = "Sin surge pricing abusivo"
            )
            
            FeatureItem(
                icon = Icons.Default.Support,
                title = "Soporte 24/7",
                description = "Atención local en español"
            )
        }
    }
}

@Composable
private fun SanJuanFeatures() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Busca como hablas:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0066CC)
            )
            
            SanJuanReferenceItem("🏥 Hospital Rawson")
            SanJuanReferenceItem("🎓 UNSJ")
            SanJuanReferenceItem("🛍️ Shopping del Sol")
            SanJuanReferenceItem("🚦 Cerca del semáforo")
            SanJuanReferenceItem("🏛️ Plaza 25 de Mayo")
        }
    }
}

@Composable
private fun SafetyFeatures() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureItem(
                icon = Icons.Default.VerifiedUser,
                title = "Conductores verificados",
                description = "Antecedentes chequeados"
            )
            
            FeatureItem(
                icon = Icons.Default.GpsFixed,
                title = "Tracking en tiempo real",
                description = "Comparte tu viaje con familiares"
            )
            
            FeatureItem(
                icon = Icons.Default.Emergency,
                title = "Botón de emergencia",
                description = "Conectado a servicios locales"
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SanJuanReferenceItem(reference: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = reference,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PageIndicator(
    isSelected: Boolean,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(if (isSelected) 12.dp else 8.dp)
            .background(
                if (isSelected) color else color.copy(alpha = 0.3f),
                CircleShape
            )
    )
}

/**
 * Data class para las páginas de onboarding
 */
private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val description: String,
    val primaryColor: Color,
    val backgroundColor: Color
)

// Extension para el icono de saludo
private val Icons.Default.Waving_Hand: ImageVector
    get() = Icons.Default.EmojiEmotions