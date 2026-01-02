package com.example.eureka.dao

import androidx.room.Room
import com.example.eureka.MyApplication


object AppLocalDB {

    val db: AppLocalDbRepository by lazy {

        val context = MyApplication.appContext
            ?: throw IllegalStateException("Context is null")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "eureka.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }
}