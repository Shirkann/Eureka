package com.example.eureka.features.post_list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eureka.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class PostListFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: PostsListViewModel by viewModels()
    private lateinit var adapter: PostsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<View?>(R.id.fragment_bg)
            ?.setBackgroundColor(requireContext().getColor(R.color.home_bg))

        val group = view.findViewById<MaterialButtonToggleGroup>(R.id.segmented)
        val btnLost = view.findViewById<MaterialButton>(R.id.btn_lost)
        val btnFound = view.findViewById<MaterialButton>(R.id.btn_found)
        val recyclerView = view.findViewById<RecyclerView>(R.id.postsRecyclerView)

        // RecyclerView
        adapter = PostsAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.posts.clear()
            adapter.posts.addAll(posts)
            adapter.notifyDataSetChanged()
        }

        fun applyTextStyles(checkedId: Int) {
            val selected = R.style.SegmentedText_Selected
            val unselected = R.style.SegmentedText_Unselected

            when (checkedId) {
                R.id.btn_lost -> {
                    btnLost.setTextAppearance(selected)
                    btnFound.setTextAppearance(unselected)
                }
                R.id.btn_found -> {
                    btnFound.setTextAppearance(selected)
                    btnLost.setTextAppearance(unselected)
                }
            }
        }

        // מצב התחלתי
        val startChecked =
            group.checkedButtonId.takeIf { it != View.NO_ID } ?: R.id.btn_lost

        applyTextStyles(startChecked)

        if (startChecked == R.id.btn_lost) {
            viewModel.loadLostPosts()
        } else {
            viewModel.loadFoundPosts()
        }

        // שינוי כפתור
        group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            applyTextStyles(checkedId)

            when (checkedId) {
                R.id.btn_lost -> viewModel.loadLostPosts()
                R.id.btn_found -> viewModel.loadFoundPosts()
            }
        }
    }
}
