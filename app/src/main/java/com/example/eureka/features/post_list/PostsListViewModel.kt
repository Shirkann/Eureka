package com.example.eureka.features.post_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eureka.models.FireBaseModel
import com.example.eureka.models.Post
import com.example.eureka.models.PostType

class PostsListViewModel : ViewModel() {

    private val firebaseModel = FireBaseModel()

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val LIMIT = 50

    fun loadLostPosts() {
        firebaseModel.getPostsByType(0, PostType.LOST, LIMIT) { posts ->
            _posts.postValue(posts)
        }
    }


    fun loadFoundPosts() {
        firebaseModel.getPostsByType(0, PostType.FOUND, LIMIT) { posts ->
            _posts.postValue(posts)
        }
    }


    fun loadPostsByUser(userId: String, since: Long = 0) {
        firebaseModel.getPostsByUser(since, userId) { posts ->
            _posts.postValue(posts)
        }
    }
}
