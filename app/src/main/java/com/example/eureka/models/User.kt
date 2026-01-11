package com.example.eureka.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey
    val id: String,
    val fullname: String,
    val email: String
) {
    fun toJson(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "fullname" to fullname,
            "email" to email
        )
    }
}
