package com.example.eureka.models.Post

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.eureka.dao.AppLocalDB
import com.example.eureka.models.FireBaseModel
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




}
