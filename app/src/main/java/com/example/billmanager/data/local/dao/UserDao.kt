package com.example.billmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.billmanager.data.local.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): MutableList<User>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun findByEmail(email: String?): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Delete
    fun delete(user: User)

    @Update
    fun update(user: User)

    @Query("SELECT * FROM users WHERE email = :e AND password = :p")
    fun login(e: String, p: String): User?

//    @Insert
//    suspend fun insertUser(user: User)
//
//    @Update
//    suspend fun updateUser(user: User)
//
//    @Delete
//    suspend fun deleteUser(user: User)
//
//    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
//    suspend fun findByEmail(email: String): User?
//
//    @Query("SELECT * FROM users WHERE email = :e AND password = :p")
//    suspend fun login(e: String, p: String): User?
//
//    @Query("SELECT * FROM users")
//    suspend fun getAll(): List<User>
}