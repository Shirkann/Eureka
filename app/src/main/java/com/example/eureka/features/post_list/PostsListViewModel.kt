package com.example.eureka.features.post_list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.eureka.models.Post.Post
import com.example.eureka.models.Post.PostType
import com.example.eureka.models.Post.PostsRepository

class PostsListViewModel : ViewModel() {

    private val selectedType = MutableLiveData<PostType>()

    val posts: LiveData<MutableList<Post>> =
        selectedType.switchMap { type ->
            PostsRepository.shared.getPostsByType(type)
        }

    private val _refreshDone = MutableLiveData<Unit>()
    val refreshDone: LiveData<Unit> = _refreshDone

    fun setType(type: PostType) {
        if (selectedType.value != type) {
            selectedType.value = type
        }
    }

    fun refresh() {
        val type = selectedType.value ?: return

        PostsRepository.shared.refreshPostsByType(type) {
            _refreshDone.postValue(Unit)
        }
    }
}

