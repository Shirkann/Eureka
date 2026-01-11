package com.example.eureka.models

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class FirebaseAuthModel private constructor() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    companion object {
        val shared = FirebaseAuthModel()
        const val USERS = "users"
    }


    fun createUser(
        email: String,
        password: String,
        fullname: String,
        completion: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser
                if (user == null) {
                    completion(false)
                    return@addOnSuccessListener
                }

                val userMap = hashMapOf(
                    "id" to user.uid,
                    "fullname" to fullname,
                    "email" to email
                )

                db.collection(USERS)
                    .document(user.uid)
                    .set(userMap)
                    .addOnSuccessListener { completion(true) }
                    .addOnFailureListener { completion(false) }
            }
            .addOnFailureListener {
                completion(false)
            }
    }


    fun signIn(
        email: String,
        password: String,
        completion: (Boolean) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { completion(true) }
            .addOnFailureListener { completion(false) }
    }


    fun signOut() {
        auth.signOut()
    }


    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
