package com.example.eureka.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eureka.models.Post.Post

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ost: Post)

    @Query("""
        SELECT * FROM Post
        WHERE ownerId = :userId
        ORDER BY createdAt DESC""")
    fun getPostsByUser(userId: String): LiveData<MutableList<Post>>

    @Query("""
        SELECT * FROM Post
        WHERE type = :type
        ORDER BY createdAt DESC
        LIMIT :limit""")
    fun getPostsByType(type: String, limit: Int): LiveData<MutableList<Post>>

    @Query("DELETE FROM Post")
    fun clearAll()
}

