package com.example.eureka.fragments

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.eureka.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class CreatePostFragment : Fragment(R.layout.fragment_createpost) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<View?>(R.id.fragment_bg)
            ?.setBackgroundColor(requireContext().getColor(R.color.white))

        val group = view.findViewById<MaterialButtonToggleGroup>(R.id.segmentedCreate)
        val btnLostCreate = view.findViewById<MaterialButton>(R.id.btn_lostCreate)
        val btnFoundCreate = view.findViewById<MaterialButton>(R.id.btn_foundCreate)

        fun applyTextStyles(checkedId: Int) {
            val selected = R.style.SegmentedText_Selected
            val unselected = R.style.SegmentedText_Unselected

            when (checkedId) {
                R.id.btn_lostCreate -> {
                    btnLostCreate.setTextAppearance(selected)
                    btnFoundCreate.setTextAppearance(unselected)
                }

                R.id.btn_foundCreate -> {
                    btnFoundCreate.setTextAppearance(selected)
                    btnLostCreate.setTextAppearance(unselected)
                }
            }
        }


        // Dropdowns
        val itemTypeInput =
            view.findViewById<MaterialAutoCompleteTextView>(R.id.itemTypeInput)


        // ===== סוג פריט =====
        val itemTypes = listOf(
            "ארנק",
            "טלפון",
            "מפתחות",
            "תיק",
            "אוזניות",
            "אחר"
        )

        val itemTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            itemTypes
        )

        itemTypeInput.setAdapter(itemTypeAdapter)

        itemTypeInput.setOnClickListener {
            itemTypeInput.showDropDown()
        }

        val locations = listOf(
            "אוניברסיטה",
            "בסיס צבאי",
            "אוטובוס / רכבת",
            "קניון",
            "רחוב",
            "אחר"
        )

        val locationAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            locations
        )

        val startChecked = group.checkedButtonId.takeIf { it != View.NO_ID } ?: R.id.btn_lostCreate
        applyTextStyles(startChecked)

        group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            applyTextStyles(checkedId)

        }

    }
}
