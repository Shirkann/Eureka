package com.example.eureka.features.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eureka.R
import com.example.eureka.databinding.FragmentProfileBinding
import com.example.eureka.features.post_list.PostsAdapter
import com.example.eureka.models.User.UserFirebaseModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var binding: FragmentProfileBinding? = null
    private var adapter: PostsAdapter? = null
    private val userFirebaseModel = UserFirebaseModel()
    private val viewModel: ProfileViewModel by viewModels()

    private var isEditMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentProfileBinding.bind(view)

        setupRecyclerView()
        setupClicks()
        observeViewModel()
        loadHeader()
        initPosts()
    }

    private fun setupRecyclerView() {
        adapter = PostsAdapter(mutableListOf()) { post ->
            findNavController().navigate(
                R.id.action_profile_to_detail,
                bundleOf("postId" to post.id)
            )
        }

        binding?.postsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = this@ProfileFragment.adapter
        }

        binding?.swipeRefresh?.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupClicks() {
        binding?.logoutButton?.setOnClickListener {
            Firebase.auth.signOut()
            findNavController().navigate(R.id.action_profile_to_login)
        }

        binding?.updateDetails?.setOnClickListener {
            if (isEditMode) {
                saveProfileChanges()
            } else {
                enterEditMode()
            }
        }

        binding?.userImage?.setOnClickListener {
            if (isEditMode) {
                Toast.makeText(
                    requireContext(),
                    "כאן תתווסף בהמשך לוגיקת החלפת תמונה",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter?.update(posts)
        }

        viewModel.refreshDone.observe(viewLifecycleOwner) {
            binding?.swipeRefresh?.isRefreshing = false
        }
    }

    private fun initPosts() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "לא נמצא משתמש מחובר", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.setUser(currentUser.uid)
        binding?.swipeRefresh?.isRefreshing = true
        viewModel.refresh()
    }

    private fun loadHeader() {
        val currentUser = Firebase.auth.currentUser ?: return

        userFirebaseModel.getUserById(currentUser.uid) { user ->
            activity?.runOnUiThread {
                val fullName = user?.fullname ?: "משתמש"
                binding?.userName?.text = fullName
                binding?.userNameInput?.setText(fullName)
            }
        }
    }

    private fun enterEditMode() {
        isEditMode = true
        binding?.userName?.visibility = View.GONE
        binding?.userNameInput?.visibility = View.VISIBLE
        binding?.userNameInput?.requestFocus()
        binding?.updateDetails?.text = "שמירה"
        binding?.userImage?.alpha = 0.7f
    }

    private fun exitEditMode(updatedName: String) {
        isEditMode = false
        binding?.userName?.text = updatedName
        binding?.userName?.visibility = View.VISIBLE
        binding?.userNameInput?.visibility = View.GONE
        binding?.updateDetails?.text = "עדכון פרטים"
        binding?.userImage?.alpha = 1f
    }

    private fun saveProfileChanges() {
        val currentUser = Firebase.auth.currentUser ?: return
        val newName = binding?.userNameInput?.text?.toString()?.trim().orEmpty()

        if (newName.isBlank()) {
            Toast.makeText(requireContext(), "נא להזין שם", Toast.LENGTH_SHORT).show()
            return
        }

        Firebase.firestore.collection(UserFirebaseModel.USERS)
            .document(currentUser.uid)
            .update("fullname", newName)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "הפרטים עודכנו", Toast.LENGTH_SHORT).show()
                exitEditMode(newName)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "שגיאה בעדכון הפרטים", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        adapter = null
    }
}
