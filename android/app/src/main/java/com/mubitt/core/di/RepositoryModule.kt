package com.mubitt.core.di

import com.mubitt.core.data.repository.TripRepositoryImpl
import com.mubitt.core.data.repository.UserRepositoryImpl
import com.mubitt.core.domain.repository.TripRepository
import com.mubitt.core.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Repository Module
 * 
 * Repository implementations binding for Mubitt
 * Standards: Clean Architecture, dependency inversion
 * Pattern: Repository pattern for data layer abstraction
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
    
    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository
    
    // @Binds
    // @Singleton
    // abstract fun bindDriverRepository(
    //     driverRepositoryImpl: DriverRepositoryImpl
    // ): DriverRepository
    
    // @Binds
    // @Singleton
    // abstract fun bindLocationRepository(
    //     locationRepositoryImpl: LocationRepositoryImpl
    // ): LocationRepository
    
    // @Binds
    // @Singleton
    // abstract fun bindPaymentRepository(
    //     paymentRepositoryImpl: PaymentRepositoryImpl
    // ): PaymentRepository
}