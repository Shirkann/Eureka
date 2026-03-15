package com.example.eureka.models.Post

import android.util.Log
import com.example.eureka.base.PostsCompletion
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.firestore
import java.util.Date

class PostFirebaseModel {

    private val db = Firebase.firestore

    companion object {
        const val POSTS = "Posts"
        private const val TAG = "PostFirebaseModel"
    }

    fun listenToPostsByType(
        type: PostType,
        onEvent: (posts: List<Post>, deletedIds: List<String>) -> Unit
    ) {
        db.collection(POSTS)
            .whereEqualTo(Post.TYPE_KEY, type.name)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                val updatedPosts = mutableListOf<Post>()
                val deletedIds = mutableListOf<String>()

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                            updatedPosts.add(Post.fromJson(dc.document.data))
                        }
                        DocumentChange.Type.REMOVED -> {
                            deletedIds.add(dc.document.id)
                        }
                    }
                }
                onEvent(updatedPosts, deletedIds)
            }
    }

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

    fun getPostsByType(
        since: Long,
        type: PostType,
        limit: Int,
        completion: PostsCompletion
    ) {
        Log.d(TAG, "FB:getPostsByType START type=$type since=$since limit=$limit")

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
                    Log.d(TAG, "FB:getPostsByType SUCCESS type=$type docs=${docs.size()}")

                    val posts = docs.map { doc ->
                        Post.fromJson(doc.data)
                    }
                    completion(posts)
                } else {
                    Log.e(TAG, "FB:getPostsByType FAILED type=$type", task.exception)
                    completion(emptyList())
                }
            }
    }

    fun addPost(post: Post, completion: (Boolean) -> Unit) {
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

    fun deletePost(post: Post, completion: (Boolean) -> Unit) {
        Log.d(TAG, "FB:deletePost id=${post.id}")
        db.collection(POSTS)
            .document(post.id)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "FB:deletePost SUCCESS id=${post.id}")
                completion(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "FB:deletePost FAILED id=${post.id}", e)
                completion(false)
            }
    }

    fun getPostById(postId: String, completion: (Post?) -> Unit) {
        db.collection(POSTS).document(postId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val post = Post.fromJson(document.data ?: emptyMap())
                completion(post)
            } else {
                completion(null)
            }
        }.addOnFailureListener {
            completion(null)
        }
    }
}
