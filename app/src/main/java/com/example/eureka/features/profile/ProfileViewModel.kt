package com.example.eureka.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.eureka.models.Post.Post
import com.example.eureka.models.Post.PostsRepository

class ProfileViewModel : ViewModel() {

    private val selectedUserId = MutableLiveData<String>()

    val posts: LiveData<MutableList<Post>> =
        selectedUserId.switchMap { userId ->
            PostsRepository.shared.getPostsByUser(userId)
        }

    private val _refreshDone = MutableLiveData<Unit>()
    val refreshDone: LiveData<Unit> = _refreshDone

    fun setUser(userId: String) {
        if (selectedUserId.value != userId) {
            selectedUserId.value = userId
        }
    }

    fun refresh() {
        val userId = selectedUserId.value ?: return

        PostsRepository.shared.refreshPostsByUser(userId) {
            _refreshDone.postValue(Unit)
        }
    }
}