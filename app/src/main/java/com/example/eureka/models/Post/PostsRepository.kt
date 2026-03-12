package com.example.eureka.models.Post

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import com.example.eureka.dao.AppLocalDB
import com.example.eureka.models.FireBaseModel
import java.util.concurrent.Executors

class PostsRepository private constructor() {

    private val firebaseModel = FireBaseModel()
    private val database = AppLocalDB.db
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        val shared = PostsRepository()
        private const val POSTS_LIMIT = 50
    }

    fun getPostsByType(type: PostType): LiveData<MutableList<Post>> {
        return database.postDao.getPostsByType(type.name, POSTS_LIMIT)
    }

    fun refreshPostsByType(
        type: PostType,
        onDone: () -> Unit
    ) {
        val lastUpdated = PostLastUpdatedManager.getLastUpdated(type)

        firebaseModel.getPostsByType(lastUpdated, type, POSTS_LIMIT) { posts ->
            executor.execute {
                var time = lastUpdated

                for (post in posts) {
                    database.postDao.insert(post)
                    post.lastUpdated?.let {
                        if (time < it) time = it
                    }
                }

                PostLastUpdatedManager.setLastUpdated(type, time)

                onDone()
            }
        }
    }

    fun getPostsByUser(userId: String): LiveData<MutableList<Post>> {
        return database.postDao.getPostsByUser(userId)
    }

    fun refreshPostsByUser(
        userId: String,
        onDone: () -> Unit
    ) {
        firebaseModel.getPostsByUser(0L, userId) { posts ->
            executor.execute {
                for (post in posts) {
                    database.postDao.insert(post)
                }
                onDone()
            }
        }
    }

    fun addPost(post: Post, completion: (Boolean) -> Unit) {
        firebaseModel.addPost(post) { success ->
            if (success) {
                executor.execute {
                    database.postDao.insert(post)
                    mainHandler.post {
                        completion(true)
                    }
                }
            } else {
                mainHandler.post {
                    completion(false)
                }
            }
        }
    }

    fun updatePost(post: Post, completion: (Boolean) -> Unit) {
        addPost(post, completion)
    }

    fun deletePost(post: Post, completion: (Boolean) -> Unit) {
        firebaseModel.deletePost(post) { success ->
            if (success) {
                executor.execute {
                    database.postDao.delete(post)
                    mainHandler.post {
                        completion(true)
                    }
                }
            } else {
                mainHandler.post {
                    completion(false)
                }
            }
        }
    }
}
