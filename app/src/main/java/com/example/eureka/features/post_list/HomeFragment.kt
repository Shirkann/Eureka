package com.example.eureka.features.post_list

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.eureka.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class HomeFragment : Fragment(R.layout.fragment_home)
{

    private val viewModel: PostsListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<View?>(R.id.fragment_bg)
            ?.setBackgroundColor(requireContext().getColor(R.color.home_bg))

        val group = view.findViewById<MaterialButtonToggleGroup>(R.id.segmented)
        val btnLost = view.findViewById<MaterialButton>(R.id.btn_lost)
        val btnFound = view.findViewById<MaterialButton>(R.id.btn_found)
        val content = view.findViewById<ViewGroup>(R.id.home_content)

        fun showText(text: String) {
            content.removeAllViews()
            content.addView(
                TextView(requireContext()).apply
            {
                this.text = text
                textSize = 18f
            })
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

        val startChecked = group.checkedButtonId.takeIf { it != View.NO_ID } ?: R.id.btn_lost
        applyTextStyles(startChecked)
        showText(if (startChecked == R.id.btn_lost) "רשימת אבדות" else "רשימת מציאות")

        group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            applyTextStyles(checkedId)

            when (checkedId) {
                R.id.btn_lost -> showText("רשימת אבדות")
                R.id.btn_found -> showText("רשימת מציאות")
            }
        }
    }
}