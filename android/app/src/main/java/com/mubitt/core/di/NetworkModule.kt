package com.mubitt.core.di

import android.content.Context
import android.content.SharedPreferences
import com.mubitt.BuildConfig
import com.mubitt.core.data.remote.api.DriverApiService
import com.mubitt.core.data.remote.api.TripApiService
import com.mubitt.core.data.remote.api.UserApiService
import com.mubitt.core.data.remote.interceptor.AuthInterceptor
import com.mubitt.core.data.remote.interceptor.ErrorInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt Network Module
 * 
 * Network dependencies for Mubitt API communication
 * Standards: Uber-level reliability, <3s response times
 * Features: Logging, timeouts, authentication
 */

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("mubitt_prefs", Context.MODE_PRIVATE)
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(errorInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.mubitt.com/v1/") // TODO: Move to BuildConfig
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService = 
        retrofit.create(UserApiService::class.java)
    
    @Provides
    @Singleton
    fun provideTripApiService(retrofit: Retrofit): TripApiService = 
        retrofit.create(TripApiService::class.java)
    
    @Provides
    @Singleton
    fun provideDriverApiService(retrofit: Retrofit): DriverApiService = 
        retrofit.create(DriverApiService::class.java)
}