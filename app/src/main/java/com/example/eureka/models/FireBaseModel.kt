package com.example.eureka.models

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.example.eureka.base.Completion
import com.example.eureka.base.UserCompletion
import com.example.eureka.base.BooleanCompletion
import com.example.eureka.base.PostsCompletion

class FireBaseModel {

    private val db = Firebase.firestore

    private companion object COLLECTIONS {
        const val POSTS = "Posts"
    }



    fun getPostsByUser(userId: String, completion: PostsCompletion) {
        db.collection(POSTS)
            .whereEqualTo("ownerId", userId)
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


<<<<<<< Updated upstream
    fun addPost(post: Post, completion: Completion) {
=======
    fun addPost(post: Post, completion: BooleanCompletion) {
>>>>>>> Stashed changes
        db.collection(POSTS)
            .document(post.id)
            .set(post.toJson)
            .addOnSuccessListener { documentReference ->
                completion()
            }
            .addOnFailureListener { e ->
                completion()
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
