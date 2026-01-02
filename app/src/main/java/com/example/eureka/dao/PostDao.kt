package com.example.eureka.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eureka.models.PostType
import com.example.eureka.models.Post

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(post: Post)

    @Query("""
        SELECT * FROM Post
        WHERE ownerId = :userId
        ORDER BY createdAt DESC """)
    fun getPostsByUser(userId: String): List<Post>

    @Query("""
        SELECT * FROM Post
        WHERE type = :type
        ORDER BY createdAt DESC
        LIMIT :limit""")
    fun getPostsByType(limit: Int,type: PostType): List<Post>




    @Query("DELETE FROM Post")
    fun clearAll()
}
