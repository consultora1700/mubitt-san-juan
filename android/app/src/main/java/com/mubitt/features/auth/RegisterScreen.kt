package com.mubitt.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * RegisterScreen - Registro de nuevos usuarios
 * Diseñada para ser simple y rápida (<1 minuto para completar)
 * Enfocada en la experiencia de usuario sanjuanina
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Navegar a home si el registro es exitoso
    LaunchedEffect(uiState.isRegistrationSuccessful) {
        if (uiState.isRegistrationSuccessful) {
            onNavigateToHome()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        RegisterHeader(onNavigateBack = onNavigateBack)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Formulario de registro
        RegisterForm(
            uiState = uiState,
            onNameChange = viewModel::updateName,
            onEmailChange = viewModel::updateEmail,
            onPhoneChange = viewModel::updatePhone,
            onPasswordChange = viewModel::updatePassword,
            onConfirmPasswordChange = viewModel::updateConfirmPassword,
            onTermsAcceptedChange = viewModel::updateTermsAccepted,
            onRegister = viewModel::register
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Link para login
        LoginLink(onNavigateToLogin = onNavigateToLogin)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Información de confianza para San Juan
        TrustIndicators()
    }
}

@Composable
private fun RegisterHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Crear cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Únete a la comunidad de Mubitt en San Juan",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterForm(
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onRegister: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo Nombre completo
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Nombre completo") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } }
            )
            
            // Campo Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it) } }
            )
            
            // Campo Teléfono
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = onPhoneChange,
                label = { Text("Teléfono (ej: 2644123456)") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.phoneError != null,
                supportingText = uiState.phoneError?.let { { Text(it) } }
            )
            
            // Campo Contraseña
            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.VisibilityOff 
                            else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None 
                                   else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { { Text(it) } }
            )
            
            // Campo Confirmar contraseña
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            if (isConfirmPasswordVisible) Icons.Default.VisibilityOff 
                            else Icons.Default.Visibility,
                            contentDescription = if (isConfirmPasswordVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None 
                                   else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.confirmPasswordError != null,
                supportingText = uiState.confirmPasswordError?.let { { Text(it) } }
            )
            
            // Checkbox términos y condiciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.termsAccepted,
                    onCheckedChange = onTermsAcceptedChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acepto los términos y condiciones de Mubitt",
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (uiState.termsError != null) {
                Text(
                    text = uiState.termsError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón Registrarse
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading && uiState.canRegister
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Crear cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Error general
            uiState.generalError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginLink(onNavigateToLogin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "¿Ya tienes cuenta? ",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        TextButton(onClick = onNavigateToLogin) {
            Text(
                text = "Inicia sesión",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TrustIndicators() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Mubitt en San Juan",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            TrustItem(
                icon = Icons.Default.Security,
                text = "Conductores verificados con antecedentes"
            )
            
            TrustItem(
                icon = Icons.Default.LocationOn,
                text = "Tracking GPS en tiempo real"
            )
            
            TrustItem(
                icon = Icons.Default.Phone,
                text = "Soporte local 24/7"
            )
        }
    }
}

@Composable
private fun TrustItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}