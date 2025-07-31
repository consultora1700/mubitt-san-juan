package com.mubitt.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt App Module
 * 
 * Dependency Injection configuration for Mubitt
 * Standards: Clean separation of concerns, testable architecture
 * Scope: Application-level dependencies
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // TODO: Add application-level dependencies (Phase 1)
    // - Database
    // - Network configuration
    // - Analytics
    // - Logging
    
}