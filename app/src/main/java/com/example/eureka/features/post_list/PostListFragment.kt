package com.example.eureka.features.post_list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eureka.R
import com.example.eureka.databinding.FragmentPostListBinding
import com.example.eureka.models.Post.PostType
import com.google.android.material.button.MaterialButtonToggleGroup

class PostListFragment : Fragment() {

    private var binding: FragmentPostListBinding? = null
    private val viewModel: PostsListViewModel by viewModels()
    private var adapter: PostsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostListBinding.inflate(inflater, container, false)

        setupBackground()
        setupRecyclerView()
        setupToggleButtons()
        setupSwipeRefresh()
        observePosts()

        binding?.swipeRefresh?.setOnRefreshListener {
            Log.d("REFRESH_FLOW", "Fragment: SwipeRefresh triggered")
            refreshData()
        }

        // טעינה ראשונית
        binding?.swipeRefresh?.isRefreshing = true
        refreshData()

        return binding!!.root
    }

    private fun setupBackground() {
        requireActivity()
            .findViewById<View?>(R.id.fragment_bg)
            ?.setBackgroundColor(requireContext().getColor(R.color.home_bg))
    }

    private fun setupRecyclerView() {
        binding?.postsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        adapter = PostsAdapter(mutableListOf())
        binding?.postsRecyclerView?.adapter = adapter
    }

    private fun setupToggleButtons() {
        val toggleGroup = binding?.segmented ?: return

        // 🔴 איפוס אייקונים / loaders
        binding?.btnLost?.icon = null
        binding?.btnFound?.icon = null

        val initialChecked =
            toggleGroup.checkedButtonId.takeIf { it != View.NO_ID }
                ?: R.id.btn_lost

        toggleGroup.check(initialChecked)
        updateButtonStyles(initialChecked)
        updatePostType(initialChecked)

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            // 🔴 איפוס גם בזמן החלפה
            binding?.btnLost?.icon = null
            binding?.btnFound?.icon = null

            updateButtonStyles(checkedId)
            updatePostType(checkedId)

            binding?.swipeRefresh?.isRefreshing = true
            refreshData()
        }
    }


    private fun setupSwipeRefresh() {
        binding?.swipeRefresh?.setOnRefreshListener {
            refreshData()
        }
    }

    private fun updateButtonStyles(checkedId: Int) {
        val selected = R.style.SegmentedText_Selected
        val unselected = R.style.SegmentedText_Unselected

        binding?.btnLost?.setTextAppearance(
            if (checkedId == R.id.btn_lost) selected else unselected
        )
        binding?.btnFound?.setTextAppearance(
            if (checkedId == R.id.btn_found) selected else unselected
        )
    }

    private fun updatePostType(checkedId: Int) {
        val type =
            if (checkedId == R.id.btn_lost) PostType.LOST else PostType.FOUND
        viewModel.setType(type)
    }

    private fun observePosts() {
        viewModel.posts.observe(viewLifecycleOwner) {
            adapter?.update(it)
        }

        viewModel.refreshDone.observe(viewLifecycleOwner) {
            binding?.swipeRefresh?.isRefreshing = false
        }
    }


    private fun refreshData() {
        Log.d("REFRESH_FLOW", "Fragment: refreshData() called")
        binding?.swipeRefresh?.isRefreshing = true
        viewModel.refresh()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
