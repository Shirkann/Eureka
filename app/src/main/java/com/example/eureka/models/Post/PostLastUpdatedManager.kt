package com.example.eureka.models.Post

import android.content.Context
import com.example.eureka.MyApplication

object PostLastUpdatedManager {

    private const val PREFS_NAME = "POSTS_PREFS"

    private fun prefs() =
        MyApplication.appContext
            ?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLastUpdated(type: PostType): Long {
        return prefs()
            ?.getLong("LAST_UPDATED_${type.name}", 0)
            ?: 0
    }

    fun setLastUpdated(type: PostType, value: Long) {
        prefs()
            ?.edit()
            ?.putLong("LAST_UPDATED_${type.name}", value)
            ?.apply()
    }

}
