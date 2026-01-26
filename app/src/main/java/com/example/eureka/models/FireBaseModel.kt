package com.example.eureka.models


import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.example.eureka.base.BooleanCompletion
import com.example.eureka.base.PostsCompletion
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth

class FireBaseModel {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    companion object COLLECTIONS {
        const val POSTS = "Posts"
        const val USERS = "users"
    }



    fun getPostsByUser(since:Long, userId: String, completion: PostsCompletion) {

        db.collection(POSTS)
            .whereGreaterThanOrEqualTo(Post.LAST_UPDATED_KEY, Timestamp(since/1000,0))
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