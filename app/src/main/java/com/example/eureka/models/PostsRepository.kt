package com.example.eureka.models

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import com.example.eureka.base.PostsCompletion
import com.example.eureka.base.BooleanCompletion
import com.example.eureka.dao.AppLocalDB
import com.example.eureka.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class PostsRepository private constructor() {


    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler.createAsync(Looper.getMainLooper())
    private val database: AppLocalDbRepository = AppLocalDB.db
    private val firebaseModel = FireBaseModel()

    companion object {
        val shared = PostsRepository()
    }

    fun addPost(post: Post, completion: BooleanCompletion)
    {
        firebaseModel.addPost(post, completion)
    }

    fun getPostsByUser(userId: String, completion: PostsCompletion)
    {
        val lastUpdated = Post.LastUpdated

        firebaseModel.getPostsByUser(lastUpdated, userId) {
            executor.execute {
                var time = lastUpdated
                for (post in it) {
                    database.postDao.insert(post)
                    post.lastUpdated?.let { postLastUpdated ->
                        if (time < postLastUpdated) {
                            time = postLastUpdated
                        }
                    }
                    post.lastUpdated = time
                    val posts = database.postDao.getPostsByUser(userId)
                    mainHandler.post { completion(posts)}
                }

            }

        }

    }

    fun getPostsByType(type: PostType, limit: Int, completion: PostsCompletion)
    {
        firebaseModel.getPostsByType(type, limit, completion)
    }

    fun deletePost(post: Post)
    {
        firebaseModel.deletePost(post)
    }

    fun getPostById(id: String, completion: PostsCompletion)
    {
        firebaseModel.getPostById(id, completion)
    }
}
