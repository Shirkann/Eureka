package com.example.eureka.features.post_list

import androidx.recyclerview.widget.RecyclerView
import com.example.eureka.databinding.PostRowLayoutBinding
import com.example.eureka.models.Post.Post
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class PostRowViewHolder(
    private val binding: PostRowLayoutBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {

        binding.titleText.text =
            post.type?.name ?: post.category.name

        binding.dateText.text = formatDate(post.createdAt)

        binding.subTitle.text =
            post.locationName ?: "מיקום לא צוין"

        binding.bodyText.text = post.text

        binding.descriptionText.text =
            "פורסם על ידי: ${post.ownerId}"

        binding.detailsTag.text = post.category.name

        if (!post.imageRemoteUrl.isNullOrEmpty()) {
            binding.itemImage.visibility = android.view.View.VISIBLE
            Picasso
                .get()
                .load(post.imageRemoteUrl)
                .fit()
                .centerCrop()
                .into(binding.itemImage)
        } else {
            binding.itemImage.visibility = android.view.View.GONE
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy | HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
