package com.example.eureka.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.eureka.models.Post.Post
import com.example.eureka.models.User

@Database(entities = [User::class, Post::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val postDao: PostDao
}
