package com.example.eureka.models.Post

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.eureka.MyApplication
import com.example.eureka.models.ItemCategory
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity
data class Post(
    @PrimaryKey val id: String,
    val ownerId: String,
    val createdAt: Long,
    val type: PostType?,
    val latitude: Double?,
    val longitude: Double?,
    val locationName: String?,
    val text: String?,
    val category: ItemCategory,
    val imageRemoteUrl: String?,
    val imageLocalPath: String?,
    var lastUpdated: Long?
) {
    companion object {

        const val ID_KEY = "id"
        const val OWNER_ID_KEY = "ownerId"
        const val CREATED_AT_KEY = "createdAt"
        const val TYPE_KEY = "type"
        const val LATITUDE_KEY = "latitude"
        const val LONGITUDE_KEY = "longitude"
        const val TEXT_KEY = "text"
        const val CATEGORY_KEY = "category"
        const val IMAGE_REMOTE_URL_KEY = "imageRemoteUrl"
        const val IMAGE_LOCAL_PATH_KEY = "imageLocalPath"
        const val LAST_UPDATED_KEY = "lastUpdated"
        const val LOCATION_NAME_KEY = "locationName"


        fun fromJson(json: Map<String, Any?>): Post {
            val id = json[ID_KEY] as String
            val ownerId = json[OWNER_ID_KEY] as String
            val createdAt = json[CREATED_AT_KEY] as Long
            val type = (json[TYPE_KEY] as String)
                .let { PostType.valueOf(it) }

            val latitude = json[LATITUDE_KEY] as? Double
            val longitude = json[LONGITUDE_KEY] as? Double
            val locationName = json[LOCATION_NAME_KEY] as? String

            val text = json[TEXT_KEY] as? String
            val category = (json[CATEGORY_KEY] as? String)
                ?.let { ItemCategory.valueOf(it) }
                ?: ItemCategory.OTHER

            val imageRemoteUrl = json[IMAGE_REMOTE_URL_KEY] as? String
            val imageLocalPath = json[IMAGE_LOCAL_PATH_KEY] as? String
            val timestamp = json[LAST_UPDATED_KEY] as? Timestamp
            val lastUpdatedLong = timestamp?.toDate()?.time

            return Post(
                id = id,
                ownerId = ownerId,
                createdAt = createdAt,
                type = type,
                latitude = latitude,
                longitude = longitude,
                locationName = locationName,
                text = text,
                category = category,
                imageRemoteUrl = imageRemoteUrl,
                imageLocalPath = imageLocalPath,
                lastUpdated = lastUpdatedLong
            )
        }
    }

    fun toJson(): Map<String, Any?> {
        return hashMapOf(
            ID_KEY to id,
            OWNER_ID_KEY to ownerId,
            CREATED_AT_KEY to createdAt,
            TYPE_KEY to type?.name,
            LATITUDE_KEY to latitude,
            LONGITUDE_KEY to longitude,
            LOCATION_NAME_KEY to locationName,
            TEXT_KEY to text,
            CATEGORY_KEY to category.name,
            IMAGE_REMOTE_URL_KEY to imageRemoteUrl,
            IMAGE_LOCAL_PATH_KEY to imageLocalPath,
            LAST_UPDATED_KEY to FieldValue.serverTimestamp()
        )
    }






}
