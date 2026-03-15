package com.example.eureka.base

import com.example.eureka.models.Post.Post
import com.example.eureka.models.User.User

typealias UserCompletion = (User?) -> Unit
typealias Completion = () -> Unit
typealias PostsCompletion = (List<Post>) -> Unit
typealias BooleanCompletion = (Boolean) -> Unit
