package com.example.eureka.models

import android.util.Log
import com.example.eureka.base.PostsCompletion
import com.example.eureka.models.Post.Post
import com.example.eureka.models.Post.PostType
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Date

class FireBaseModel {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    companion object {
        const val POSTS = "Posts"
        const val USERS = "users"
        private const val TAG = "REFRESH_FLOW"
    }

    /**
     * ===============================
     *  POSTS BY USER
     * ===============================
     */
    fun getPostsByUser(
        since: Long,
        userId: String,
        completion: PostsCompletion
    ) {
        Log.d(TAG, "FB:getPostsByUser userId=$userId since=$since")

        db.collection(POSTS)
            .whereGreaterThanOrEqualTo(
                Post.LAST_UPDATED_KEY,
                Timestamp(Date(since))
            )
            .whereEqualTo(Post.OWNER_ID_KEY, userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val posts = task.result.map { Post.fromJson(it.data) }
                    Log.d(TAG, "FB:getPostsByUser returned ${posts.size} posts")
                    completion(posts)
                } else {
                    Log.e(TAG, "FB:getPostsByUser FAILED", task.exception)
                    completion(emptyList())
                }
            }
    }

    /**
     * ===============================
     *  POSTS BY TYPE (LOST / FOUND)
     * ===============================
     */
    fun getPostsByType(
        since: Long,
        type: PostType,
        limit: Int,
        completion: PostsCompletion
    ) {
        Log.d(
            TAG,
            "FB:getPostsByType START type=$type since=$since limit=$limit"
        )

        val sinceTimestamp = Timestamp(Date(since))

        db.collection(POSTS)
            .whereGreaterThan(
                Post.LAST_UPDATED_KEY,
                sinceTimestamp
            )
            .whereEqualTo(Post.TYPE_KEY, type.name)
            .limit(limit.toLong())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val docs = task.result
                    Log.d(
                        TAG,
                        "FB:getPostsByType SUCCESS type=$type docs=${docs.size()}"
                    )

                    val posts = docs.map { doc ->
                        Log.d(
                            TAG,
                            "FB:doc id=${doc.id} lastUpdated=${doc.get(Post.LAST_UPDATED_KEY)}"
                        )
                        Post.fromJson(doc.data)
                    }

                    completion(posts)

                } else {
                    Log.e(
                        TAG,
                        "FB:getPostsByType FAILED type=$type",
                        task.exception
                    )
                    completion(emptyList())
                }
            }
    }

    /**
     * ===============================
     *  ADD POST
     * ===============================
     */
    fun addPost(
        post: Post,
        completion: (Boolean) -> Unit
    ) {
        Log.d(TAG, "FB:addPost id=${post.id}")

        db.collection(POSTS)
            .document(post.id)
            .set(post.toJson())
            .addOnSuccessListener {
                Log.d(TAG, "FB:addPost SUCCESS id=${post.id}")
                completion(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "FB:addPost FAILED id=${post.id}", e)
                completion(false)
            }
    }

    /**
     * ===============================
     *  DELETE POST
     * ===============================
     */
    fun deletePost(post: Post) {
        Log.d(TAG, "FB:deletePost id=${post.id}")

        db.collection(POSTS)
            .document(post.id)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "FB:deletePost SUCCESS id=${post.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "FB:deletePost FAILED id=${post.id}", e)
            }
    }
}
