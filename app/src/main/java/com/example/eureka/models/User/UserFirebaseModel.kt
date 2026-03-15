package com.example.eureka.models.User

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserFirebaseModel {

    private val db = Firebase.firestore

    companion object {
        const val USERS = "users"
    }

    fun getUserById(userId: String, completion: (User?) -> Unit) {
        db.collection(USERS).document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val fullname = document.getString("fullname") ?: "Unknown"
                val email = document.getString("email") ?: ""
                val user = User(userId, fullname, email)
                completion(user)
            } else {
                completion(null)
            }
        }.addOnFailureListener {
            completion(null)
        }
    }
}
