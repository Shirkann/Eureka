package com.example.eureka.models

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.util.UUID

class StorageModel {

    companion object {
        private const val TAG = "StorageModel"
    }

    fun uploadImage(imageUri: Uri, completion: (String?) -> Unit) {
        val storageRef = Firebase.storage.reference.child("images/${UUID.randomUUID()}")
        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                completion(uri.toString())
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Image upload FAILED: ${e.message}", e)
            completion(null)
        }
    }
}
