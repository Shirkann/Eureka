package com.example.eureka.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.eureka.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val group = view.findViewById<MaterialButtonToggleGroup>(R.id.segmented)
        val btnLost = view.findViewById<MaterialButton>(R.id.btn_lost)
        val btnFound = view.findViewById<MaterialButton>(R.id.btn_found)
        val content = view.findViewById<ViewGroup>(R.id.home_content)

        fun showText(text: String) {
            content.removeAllViews()
            val tv = TextView(requireContext()).apply {
                this.text = text
                textSize = 18f
            }
            content.addView(tv)
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

        // מצב התחלתי (במקרה שה-XML מסמן ברירת מחדל)
        val startChecked = group.checkedButtonId.takeIf { it != View.NO_ID } ?: R.id.btn_lost
        applyTextStyles(startChecked)

        // תוכן התחלתי
        showText(if (startChecked == R.id.btn_lost) "רשימת אבדות" else "רשימת מציאות")

        // מאזין לשינוי בחירה
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
