package com.example.eureka.features.post_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.databinding.FragmentPostDetailBinding
import com.example.eureka.models.Post.Post
import com.example.eureka.models.Post.PostFirebaseModel
import com.example.eureka.models.Post.PostsRepository
import com.example.eureka.models.User.UserFirebaseModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.eureka.utils.RetrofitClient

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
            PostFirebaseModel().getPostById(postId) { post ->
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

        // Check if current user is owner
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null && currentUser.uid == post.ownerId) {
            binding?.ownerActionsLayout?.visibility = View.VISIBLE
            setupOwnerActions(post)
        } else {
            binding?.ownerActionsLayout?.visibility = View.GONE
        }

        UserFirebaseModel().getUserById(post.ownerId) { user ->
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

        loadWeather(post.latitude, post.longitude)
    }

    private fun setupOwnerActions(post: Post) {
        binding?.editButton?.setOnClickListener {
            val bundle = Bundle().apply {
                putString("postId", post.id)
            }
            findNavController().navigate(R.id.action_postDetailFragment_to_createPostFragment, bundle)
        }

        binding?.deleteButton?.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("מחיקת פוסט")
                .setMessage("האם אתה בטוח שברצונך למחוק פוסט זה?")
                .setPositiveButton("מחק") { _, _ ->
                    deletePost(post)
                }
                .setNegativeButton("ביטול", null)
                .show()
        }
    }

    private fun deletePost(post: Post) {
        PostsRepository.shared.deletePost(post) { success ->
            if (success) {
                Toast.makeText(requireContext(), "הפוסט נמחק בהצלחה", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "מחיקה נכשלה", Toast.LENGTH_SHORT).show()
            }
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

    private fun loadWeather(lat: Double?, lon: Double?) {

        if (lat == null || lon == null) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.weatherApi.getWeather(lat, lon)
                val temp = response.current_weather.temperature
                binding?.weatherText?.text = "Weather: ${temp}°C"
            } catch (e: Exception) {
                binding?.weatherText?.text = "Weather unavailable"
            }
        }
    }
}
