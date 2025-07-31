package com.mubitt.features.auth

import com.mubitt.core.data.remote.dto.ApiResult
import com.mubitt.core.data.remote.interceptor.AuthInterceptor
import com.mubitt.core.domain.model.User
import com.mubitt.core.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests para LoginViewModel
 * Tests críticos para autenticación en Mubitt
 */
@ExperimentalCoroutinesApi
class LoginViewModelTest {
    
    private lateinit var userRepository: UserRepository
    private lateinit var authInterceptor: AuthInterceptor
    private lateinit var loginViewModel: LoginViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk()
        authInterceptor = mockk(relaxed = true)
        loginViewModel = LoginViewModel(userRepository, authInterceptor)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `when login with valid email and password, should update UI state to success`() = runTest {
        // Given
        val email = "usuario@ejemplo.com"
        val password = "password123"
        val expectedUser = User(
            id = "user_123",
            name = "Juan Pérez",
            email = email,
            phoneNumber = "+54 264 123-4567",
            profilePictureUrl = null,
            rating = 4.8,
            tripCount = 25,
            isVerified = true
        )
        
        coEvery { 
            userRepository.login(email, password) 
        } returns ApiResult.Success(expectedUser)
        
        // When
        loginViewModel.loginWithEmail(email, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = loginViewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.isLoginSuccessful)
        assertEquals(expectedUser, uiState.currentUser)
        assertNull(uiState.errorMessage)
    }
    
    @Test
    fun `when login with invalid email format, should show validation error`() = runTest {
        // Given
        val invalidEmail = "email-invalido"
        val password = "password123"
        
        // When
        loginViewModel.loginWithEmail(invalidEmail, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = loginViewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isLoginSuccessful)
        assertEquals("Email inválido", uiState.emailError)
        assertNull(uiState.currentUser)
    }
    
    @Test
    fun `when login with short password, should show validation error`() = runTest {
        // Given
        val email = "usuario@ejemplo.com"
        val shortPassword = "123" // Menos de 6 caracteres
        
        // When
        loginViewModel.loginWithEmail(email, shortPassword)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = loginViewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isLoginSuccessful)
        assertEquals("La contraseña debe tener al menos 6 caracteres", uiState.passwordError)
        assertNull(uiState.currentUser)
    }
    
    @Test
    fun `when login with valid phone number, should login successfully`() = runTest {
        // Given
        val phoneNumber = "+54 264 123-4567"
        val password = "password123"
        val expectedUser = User(
            id = "user_123",
            name = "María González",
            email = "maria@ejemplo.com",
            phoneNumber = phoneNumber,
            profilePictureUrl = null,
            rating = 4.9,
            tripCount = 15,
            isVerified = true
        )
        
        coEvery { 
            userRepository.loginWithPhone(phoneNumber, password) 
        } returns ApiResult.Success(expectedUser)
        
        // When
        loginViewModel.loginWithPhone(phoneNumber, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = loginViewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertTrue(uiState.isLoginSuccessful)
        assertEquals(expectedUser, uiState.currentUser)
        assertNull(uiState.errorMessage)
    }
    
    @Test
    fun `when login with invalid phone format, should show validation error`() = runTest {
        // Given
        val invalidPhone = "123" // Número muy corto
        val password = "password123"
        
        // When
        loginViewModel.loginWithPhone(invalidPhone, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = loginViewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isLoginSuccessful)
        assertEquals("Número de teléfono inválido", uiState.phoneError)
        assertNull(uiState.currentUser)
    }
    
    @Test
    fun `when repository returns network error, should show error message`() = runTest {
        // Given
        val email = "usuario@ejemplo.com"
        val password = "password123"
        val errorMessage = "Error de conexión"
        
        coEvery { 
            userRepository.login(email, password) 
        } returns ApiResult.Error(errorMessage, 500)
        
        // When
        loginViewModel.loginWithEmail(email, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = loginViewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isLoginSuccessful)
        assertEquals(errorMessage, uiState.errorMessage)
        assertNull(uiState.currentUser)
    }
    
    @Test
    fun `when repository returns authentication error, should show error message`() = runTest {
        // Given
        val email = "usuario@ejemplo.com"
        val password = "wrong-password"
        val errorMessage = "Credenciales incorrectas"
        
        coEvery { 
            userRepository.login(email, password) 
        } returns ApiResult.Error(errorMessage, 401)
        
        // When
        loginViewModel.loginWithEmail(email, password)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = loginViewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertFalse(uiState.isLoginSuccessful)
        assertEquals(errorMessage, uiState.errorMessage)
        assertNull(uiState.currentUser)
    }
    
    @Test
    fun `when login is in progress, should show loading state`() = runTest {
        // Given
        val email = "usuario@ejemplo.com"
        val password = "password123"
        
        coEvery { 
            userRepository.login(email, password) 
        } returns ApiResult.Loading()
        
        // When
        loginViewModel.loginWithEmail(email, password)
        
        // Then (before advancing the dispatcher)
        val uiState = loginViewModel.uiState.value
        assertTrue(uiState.isLoading)
        assertFalse(uiState.isLoginSuccessful)
        assertNull(uiState.errorMessage)
    }
    
    @Test
    fun `when clearErrors is called, should clear all error states`() = runTest {
        // Given - Set some errors first
        loginViewModel.loginWithEmail("invalid-email", "123")
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify errors are set
        var uiState = loginViewModel.uiState.value
        assertNotNull(uiState.emailError)
        assertNotNull(uiState.passwordError)
        
        // When
        loginViewModel.clearErrors()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        uiState = loginViewModel.uiState.value
        assertNull(uiState.emailError)
        assertNull(uiState.passwordError)
        assertNull(uiState.errorMessage)
    }
}