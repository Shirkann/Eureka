package com.example.eureka.features.post_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.eureka.base.PostsCompletion
import com.example.eureka.models.Post
import com.example.eureka.models.PostsRepository

class PostsListViewModel: ViewModel() {

    var data: LiveData<MutableList<Post>>? = null


    fun getPostsByUser(userId: String,completion: PostsCompletion){
        PostsRepository.shared.getPostsByUser(userId,completion)
    }
}