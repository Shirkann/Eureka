package com.example.eureka.models

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.example.eureka.base.Completion
import com.example.eureka.base.UserCompletion
import com.example.eureka.base.PostsCompletion

class FireBaseModel {

    private val db = Firebase.firestore

    private companion object COLLECTIONS {
        const val POSTS = "Posts"
    }


    fun getPostsByUser(userId: String, completion: PostsCompletion) {
        db.collection(POSTS)
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener { result ->
                when (result.isSuccessful) {
                    true -> completion(result.result.map { Post.fromJson(it.data) })
                    false -> completion(emptyList())
                }
            }
    }

    fun getPostsByType(type: PostType, limit: Int, completion: PostsCompletion) {
        db.collection(POSTS)
            .whereEqualTo("type", type.name)
            .limit(limit.toLong())
            .get()
            .addOnCompleteListener { result ->
                when (result.isSuccessful) {
                    true -> completion(result.result.map { Post.fromJson(it.data) })
                    false -> completion(emptyList())
                }
            }
    }


    fun addPost(post: Post, completion: (Boolean) -> Unit) {
        db.collection(POSTS)
            .document(post.id)
            .set(post.toJson)
            .addOnSuccessListener {
                completion(true)
            }
            .addOnFailureListener {
                completion(false)
            }

    }

    fun deletePost(post: Post) {
        db.collection(COLLECTIONS.POSTS)
            .document(post.id)
            .delete()

    }

    fun getPostById(id: String, completion: PostsCompletion) {
        db.collection(POSTS)
            .whereEqualTo("id", id)
            .get()
            .addOnCompleteListener { result ->
                when (result.isSuccessful) {
                    true -> completion(result.result.map { Post.fromJson(it.data) })
                    false -> completion(emptyList())
                }
            }

    }
}