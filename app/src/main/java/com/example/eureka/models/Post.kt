package com.example.eureka.models

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class Post(
    @PrimaryKey val id: String,

    val ownerId: String,

    val createdAt: Long,
    val type: PostType,

    val latitude: Double?,
    val longitude: Double?,

    val text: String,

    val category: ItemCategory,

    val imageRemoteUrl: String?,
    val imageLocalPath: String?
)
