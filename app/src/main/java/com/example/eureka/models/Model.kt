package com.example.eureka.models

import android.os.Handler
import android.os.Looper
import com.example.eureka.base.Completion
import com.example.eureka.base.UserCompletion
import com.example.eureka.base.PostsCompletion
import com.example.eureka.dao.AppLocalDB
import java.util.concurrent.Executors

class Model private constructor() {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler.createAsync(Looper.getMainLooper())

    private val database = AppLocalDB.db

    // -------- Users --------
    fun addUser(user: User, completion: Completion) {
        executor.execute {
            database.userDao.insertUser(user)
            mainHandler.post { completion() }
        }
    }

    fun deleteUser(user: User, completion: Completion = {}) {
        executor.execute {
            database.userDao.deleteUser(user)
            mainHandler.post { completion() }
        }
    }

    fun getCurrentUser(completion: UserCompletion) {
        executor.execute {
            val user = database.userDao.getCurrentUser()
            mainHandler.post {
                if (user != null) {
                    completion(user)
                }
            }
        }
    }



    // -------- Posts --------
    fun addPost(post: Post, completion: Completion) {
        executor.execute {
            database.postDao.insert(post)
            mainHandler.post { completion() }
        }
    }

    fun getPostsByUser(userId: String, completion: PostsCompletion) {
        executor.execute {
            val posts = database.postDao.getPostsByUser(userId)
            mainHandler.post { completion(posts) }
        }
    }

    fun getPostsByType(type: PostType, limit: Int, completion: PostsCompletion) {
        executor.execute {
            val posts = database.postDao.getPostsByType(limit, type)
            mainHandler.post { completion(posts) }
        }
    }

    companion object {
        val shared: Model by lazy { Model() }
    }
}
