package com.example.eureka.models

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.example.eureka.base.Completion

class FirebaseAuthModel {

    private val auth: FirebaseAuth = Firebase.auth

    companion object {
        val shared = FirebaseAuthModel()
    }

    fun signIn(
        email: String,
        password: String,
        completion: (Boolean) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                completion(task.isSuccessful)
            }
    }

    fun createUser(
        email: String,
        password: String,
        completion: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                completion(task.isSuccessful)
            }
            .addOnFailureListener {
                Log.i("TAG", "createUser failed: ${it.message}")
            }
    }

    fun signOut() {
        auth.signOut()
    }
}

