package com.mubitt.features.maps.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mubitt.core.domain.model.SanJuanReference
import com.mubitt.core.domain.model.ReferenceCategory
import com.mubitt.core.domain.usecase.SearchResult
import com.mubitt.shared.theme.Spacing

/**
 * Barra de búsqueda inteligente para San Juan
 * Incluye sugerencias y autocompletado con referencias locales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanJuanSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<SearchResult>,
    onLocationSelected: (SanJuanReference) -> Unit,
    placeholder: String = "¿A dónde vamos?",
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(modifier = modifier) {
        // Barra de búsqueda principal
        OutlinedTextField(
            value = query,
            onValueChange = { 
                onQueryChange(it)
                isExpanded = it.isNotEmpty()
            },
            placeholder = { 
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(
                        onClick = { 
                            onQueryChange("")
                            isExpanded = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    if (searchResults.isNotEmpty()) {
                        onLocationSelected(searchResults.first().reference)
                        isExpanded = false
                    }
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        
        // Lista de sugerencias
        if (isExpanded && searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(searchResults.take(5)) { result ->
                        SearchResultItem(
                            result = result,
                            onClick = {
                                onLocationSelected(result.reference)
                                onQueryChange(result.reference.name)
                                isExpanded = false
                                keyboardController?.hide()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono según categoría
        Icon(
            imageVector = getCategoryIcon(result.reference.category),
            contentDescription = null,
            tint = getCategoryColor(result.reference.category),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(Spacing.medium))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Nombre del lugar
            Text(
                text = result.reference.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Descripción y tipo de match
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.reference.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Badge de popularidad
                if (result.reference.isPopular) {
                    Box(
                        modifier = Modifier
                            .padding(start = Spacing.small)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Popular",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        
        // Indicador de relevancia
        if (result.relevanceScore > 0.8f) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Alta relevancia",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun getCategoryIcon(category: ReferenceCategory) = when (category) {
    ReferenceCategory.HOSPITAL -> Icons.Default.LocalHospital
    ReferenceCategory.UNIVERSITY -> Icons.Default.School
    ReferenceCategory.SHOPPING -> Icons.Default.ShoppingCart
    ReferenceCategory.GOVERNMENT -> Icons.Default.AccountBalance
    ReferenceCategory.TRANSPORT -> Icons.Default.DirectionsBus
    ReferenceCategory.LANDMARK -> Icons.Default.LocationOn
    ReferenceCategory.SCHOOL -> Icons.Default.School
    ReferenceCategory.BANK -> Icons.Default.AccountBalance
    ReferenceCategory.RESTAURANT -> Icons.Default.Restaurant
    ReferenceCategory.GAS_STATION -> Icons.Default.LocalGasStation
}

private fun getCategoryColor(category: ReferenceCategory) = when (category) {
    ReferenceCategory.HOSPITAL -> Color(0xFFE57373)        // Rojo suave
    ReferenceCategory.UNIVERSITY -> Color(0xFF4FC3F7)      // Azul
    ReferenceCategory.SHOPPING -> Color(0xFF81C784)        // Verde
    ReferenceCategory.GOVERNMENT -> Color(0xFF9575CD)      // Violeta
    ReferenceCategory.TRANSPORT -> Color(0xFFFFB74D)       // Naranja
    ReferenceCategory.LANDMARK -> Color(0xFFF06292)        // Rosa
    ReferenceCategory.SCHOOL -> Color(0xFF4FC3F7)          // Azul
    ReferenceCategory.BANK -> Color(0xFF9575CD)            // Violeta
    ReferenceCategory.RESTAURANT -> Color(0xFFFF8A65)      // Naranja suave
    ReferenceCategory.GAS_STATION -> Color(0xFF757575)     // Gris
}