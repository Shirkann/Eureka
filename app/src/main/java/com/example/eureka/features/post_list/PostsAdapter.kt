package com.example.eureka.features.post_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eureka.databinding.PostRowLayoutBinding
import com.example.eureka.models.Post.Post


class PostsAdapter(
    var posts: MutableList<Post>) : RecyclerView.Adapter<PostRowViewHolder>() {

    override fun getItemCount(): Int = posts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostRowViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PostRowLayoutBinding.inflate(inflater, parent, false)
        return PostRowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostRowViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    fun update(newPosts: List<Post>) {
        posts.clear()
        posts.addAll(newPosts)
        notifyDataSetChanged()
    }

}
