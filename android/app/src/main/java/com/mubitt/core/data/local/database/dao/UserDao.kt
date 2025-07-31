package com.mubitt.core.data.local.database.dao

import androidx.room.*
import com.mubitt.core.data.local.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de usuario
 * Optimizado para cache local y funcionalidad offline
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: String): Flow<UserEntity?>
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getUserByPhone(phoneNumber: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET rating = :rating, tripCount = :tripCount WHERE id = :userId")
    suspend fun updateUserStats(userId: String, rating: Double, tripCount: Int)
    
    @Query("UPDATE users SET profilePictureUrl = :profilePictureUrl WHERE id = :userId")
    suspend fun updateProfilePicture(userId: String, profilePictureUrl: String)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    @Query("SELECT * FROM users ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentUsers(limit: Int = 10): List<UserEntity>
}