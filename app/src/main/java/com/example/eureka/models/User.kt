package com.example.eureka.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(

    @PrimaryKey  val id: String,

    val email: String,
    val fullName: String,
    val avatar: String?,
    )
