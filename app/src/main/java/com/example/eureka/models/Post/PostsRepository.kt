package com.example.eureka.models.Post

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import com.example.eureka.dao.AppLocalDB
import java.util.concurrent.Executors

class PostsRepository private constructor() {

    private val firebaseModel = PostFirebaseModel()
    private val database = AppLocalDB.db
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        val shared = PostsRepository()
        private const val POSTS_LIMIT = 50
    }

    init {
        // Start listening to both types of posts in real-time
        listenToPosts(PostType.LOST)
        listenToPosts(PostType.FOUND)
    }

    private fun listenToPosts(type: PostType) {
        firebaseModel.listenToPostsByType(type) { updatedPosts, deletedIds ->
            executor.execute {
                // Update or Insert new/modified posts
                for (post in updatedPosts) {
                    database.postDao.insert(post)
                }
                
                // Delete removed posts from local DB
                for (id in deletedIds) {
                    // We need a way to delete by ID if we only have the ID
                    // For now, if your DAO supports it, or use a dummy post with that ID
                    database.postDao.deleteById(id)
                }
            }
        }
    }

    fun getPostsByType(type: PostType): LiveData<MutableList<Post>> {
        return database.postDao.getPostsByType(type.name, POSTS_LIMIT)
    }

    fun refreshPostsByType(
        type: PostType,
        onDone: () -> Unit
    ) {
        // With real-time listeners, refresh is technically not needed for new data,
        // but we keep it for manual triggers if necessary.
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
