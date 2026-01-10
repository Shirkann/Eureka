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
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): Post {
            val id = json["id"] as? String ?: ""
            val ownerId = json["ownerId"] as? String ?: ""
            val createdAt = json["createdAt"] as? Long ?: 0L
            val typeString = json["type"] as? String
            val type = typeString?.let { PostType.valueOf(it) }
            val latitude = json["latitude"] as? Double
            val longitude = json["longitude"] as? Double
            val text = json["text"] as? String ?: ""
            val categoryString = json["category"] as? String
            val category = categoryString?.let { ItemCategory.valueOf(it) } ?: ItemCategory.OTHER
            val imageRemoteUrl = json["imageRemoteUrl"] as? String
            val imageLocalPath = json["imageLocalPath"] as? String

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
            "type" to type?.name,
            "latitude" to latitude,
            "longitude" to longitude,
            "text" to text,
            "category" to category.name,
            "imageRemoteUrl" to imageRemoteUrl,
            "imageLocalPath" to imageLocalPath
        )
}
