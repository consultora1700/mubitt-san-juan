package com.mubitt.core.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.mubitt.core.data.local.database.dao.TripDao
import com.mubitt.core.data.local.database.dao.UserDao
import com.mubitt.core.data.local.database.entity.TripEntity
import com.mubitt.core.data.local.database.entity.TripConverters
import com.mubitt.core.data.local.database.entity.UserEntity

/**
 * Base de datos Room principal para Mubitt
 * Versión: 1 - MVP initial schema
 */
@Database(
    entities = [
        UserEntity::class,
        TripEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(TripConverters::class)
abstract class MubittDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    
    companion object {
        private const val DATABASE_NAME = "mubitt_database"
        
        @Volatile
        private var INSTANCE: MubittDatabase? = null
        
        fun getDatabase(context: Context): MubittDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MubittDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // Para MVP, no migration strategy
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}