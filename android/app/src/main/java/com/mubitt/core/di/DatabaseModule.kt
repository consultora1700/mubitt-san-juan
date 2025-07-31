package com.mubitt.core.di

import android.content.Context
import com.mubitt.core.data.local.database.MubittDatabase
import com.mubitt.core.data.local.database.dao.TripDao
import com.mubitt.core.data.local.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Database Module
 * 
 * Room database configuration for Mubitt
 * Standards: Offline-first architecture, data consistency
 * Features: Caching, synchronization, performance optimization
 */

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val DATABASE_NAME = "mubitt_database"
    
    @Provides
    @Singleton
    fun provideMubittDatabase(@ApplicationContext context: Context): MubittDatabase {
        return MubittDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideUserDao(database: MubittDatabase): UserDao = database.userDao()
    
    @Provides
    fun provideTripDao(database: MubittDatabase): TripDao = database.tripDao()
}