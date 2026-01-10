package com.example.eureka.models

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class Post(
    @PrimaryKey val id: String,
    val ownerId: String,
    val createdAt: Long,
    val type: PostType?,
    val latitude: Double?,
    val longitude: Double?,
    val text: String,
    val category: ItemCategory,
    val imageRemoteUrl: String?,
    val imageLocalPath: String?
){
    companion object {

        fun fromJson(json: Map<String, Any?>): Post {
            val id = json["id"] as String
            val ownerId = json["ownerId"] as String
            val createdAt = json["createdAt"] as Long
            val type = json["type"] as PostType?
            val latitude = json["latitude"] as Double?
            val longitude = json["longitude"] as Double?
            val text =  json["text"] as String
            val category = json["category"] as ItemCategory
            val imageRemoteUrl = json["imageRemoteUrl"] as String?
            val imageLocalPath = json["imageLocalPath"] as String

            return Post(
                id = id,
                ownerId = ownerId,
                createdAt = createdAt,
                type = type,
                latitude = latitude,
                longitude = longitude,
                text = text,
                category = category,
                imageRemoteUrl = imageRemoteUrl,
                imageLocalPath = imageLocalPath
            )
        }
    }

    val toJson: Map<String, Any?>
        get() = hashMapOf(
            "id" to id,
            "ownerId" to ownerId,
            "createdAt" to createdAt,
            "type" to type,
            "latitude" to latitude,
            "longitude" to longitude,
            "text" to text,
            "category" to category,
            "imageRemoteUrl" to imageRemoteUrl,
            "imageLocalPath" to imageLocalPath
        )





}
