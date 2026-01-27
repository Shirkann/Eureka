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

    private val firebaseModel = FireBaseModel()
    private val database = AppLocalDB.db
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        val shared = PostsRepository()
        private const val POSTS_LIMIT = 50
    }

    fun getPostsByType(type: PostType): LiveData<MutableList<Post>> {
        return database.postDao.getPostsByType(type.name, POSTS_LIMIT)
    }


    fun refreshPostsByType(type: PostType) {
        val lastUpdated = Post.LastUpdated

        firebaseModel.getPostsByType(lastUpdated, type, POSTS_LIMIT) { posts ->
            executor.execute {
                var time = lastUpdated
                for (post in posts) {
                    database.postDao.insert(post)
                    post.lastUpdated?.let {
                        if (time < it) time = it
                    }
                }
                Post.LastUpdated = time
            }
        }
    }
}

