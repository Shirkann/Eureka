package com.example.eureka.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eureka.models.User

@Dao
interface UserDao
{
    @Query("SELECT * FROM User LIMIT 1")
    fun getCurrentUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(users: User)

    @Delete
    fun deleteUser(user: User)
}