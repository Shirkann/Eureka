package com.example.eureka.features.post_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.databinding.FragmentPostDetailBinding
import com.example.eureka.models.FireBaseModel
import com.example.eureka.models.Post.Post
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class PostDetailFragment : Fragment(R.layout.fragment_post_detail) {

    private var binding: FragmentPostDetailBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.backButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        loadPost()
    }

    private fun loadPost() {
        val postId = arguments?.getString("postId")
        if (postId != null) {
            FireBaseModel().getPostById(postId) { post ->
                if (post != null) {
                    displayPost(post)
                }
            }
        }
    }

    private fun displayPost(post: Post) {
        binding?.titleText?.text = post.type?.name ?: post.category.name
        binding?.dateText?.text = formatDate(post.createdAt)
        binding?.locationText?.text = post.locationName ?: "מיקום לא צוין"
        binding?.bodyText?.text = post.text
        binding?.categoryText?.text = post.category.name

        FireBaseModel().getUserById(post.ownerId) { user ->
            if (user != null) {
                binding?.ownerText?.text = "פורסם על ידי: ${user.fullname}"
            } else {
                binding?.ownerText?.text = "פורסם על ידי: Unknown"
            }
        }

        if (!post.imageRemoteUrl.isNullOrEmpty()) {
            binding?.itemImage?.visibility = View.VISIBLE
            Picasso.get().load(post.imageRemoteUrl).fit().centerCrop().into(binding!!.itemImage)
        } else {
            binding?.itemImage?.visibility = View.GONE
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy | HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}


