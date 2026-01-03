package com.example.eureka.base

import com.example.eureka.models.User
import com.example.eureka.models.Post

typealias UserCompletion = (User?) -> Unit
typealias Completion = () -> Unit
typealias PostsCompletion = (List<Post>) -> Unit
