package com.example.eureka.dao

import androidx.room.TypeConverter
import com.example.eureka.models.ItemCategory
import com.example.eureka.models.Post.PostType

class Converters {

    @TypeConverter
    fun fromPostType(value: PostType): String = value.name

    @TypeConverter
    fun toPostType(value: String): PostType = PostType.valueOf(value)

    @TypeConverter
    fun fromItemCategory(value: ItemCategory): String = value.name

    @TypeConverter
    fun toItemCategory(value: String): ItemCategory = ItemCategory.valueOf(value)
}
