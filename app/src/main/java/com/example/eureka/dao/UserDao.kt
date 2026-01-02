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
    @Query("SELECT * FROM User WHERE id = :uid LIMIT 1")
    fun getUser(uid: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg users: User)

    @Delete
    fun deleteUser(user: User)
}