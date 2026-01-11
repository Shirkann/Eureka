package com.example.eureka.models

import android.os.Handler
import android.os.Looper
import com.example.eureka.base.Completion
import com.example.eureka.base.UserCompletion
import com.example.eureka.base.PostsCompletion
import com.example.eureka.base.BooleanCompletion
import com.example.eureka.dao.AppLocalDB
import java.util.concurrent.Executors

class Model private constructor() {

    private val firebaseModel = FireBaseModel()
    private val firebaseAuth = FirebaseAuthModel()

    companion object {
        val shared = Model()
    }

    fun addPost(post: Post, completion: BooleanCompletion)
    {
        firebaseModel.addPost(post, completion)
    }

    fun getPostsByUser(userId: String, completion: PostsCompletion)
    {
        firebaseModel.getPostsByUser(userId, completion)
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
