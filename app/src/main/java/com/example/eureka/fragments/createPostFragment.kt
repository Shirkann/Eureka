package com.example.eureka.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.models.ItemCategory
import com.example.eureka.models.Post
import com.example.eureka.models.PostType
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale
import java.util.UUID

class createPostFragment : Fragment(R.layout.fragment_createpost) {

    private var selectedPostType: PostType = PostType.LOST
    private var selectedItemCategory: ItemCategory? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            getLocation()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .findViewById<View?>(R.id.fragment_bg)
            ?.setBackgroundColor(requireContext().getColor(R.color.white))

        val group = view.findViewById<MaterialButtonToggleGroup>(R.id.segmentedCreate)
        val btnLostCreate = view.findViewById<MaterialButton>(R.id.btn_lostCreate)
        val btnFoundCreate = view.findViewById<MaterialButton>(R.id.btn_foundCreate)
        val createButton = view.findViewById<Button>(R.id.createButton)
        val descriptionInput = view.findViewById<TextInputEditText>(R.id.descriptionInput)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }


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
        val itemCategoryMap = mapOf(
            ItemCategory.PHONE to "טלפון",
            ItemCategory.KEYS to "מפתחות",
            ItemCategory.WALLET to "ארנק",
            ItemCategory.BAG to "תיק",
            ItemCategory.OTHER to "אחר"
        )
        val itemTypeNames = itemCategoryMap.values.toList()


        val itemTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            itemTypeNames
        )

        itemTypeInput.setAdapter(itemTypeAdapter)

        itemTypeInput.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position) as String
            selectedItemCategory = itemCategoryMap.entries.find { it.value == selectedName }?.key
        }

        itemTypeInput.setOnClickListener {
            itemTypeInput.showDropDown()
        }

        val startChecked = group.checkedButtonId.takeIf { it != View.NO_ID } ?: R.id.btn_lostCreate
        applyTextStyles(startChecked)
        selectedPostType = if (startChecked == R.id.btn_lostCreate) PostType.LOST else PostType.FOUND


        group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            applyTextStyles(checkedId)

            selectedPostType = when (checkedId) {
                R.id.btn_lostCreate -> PostType.LOST
                R.id.btn_foundCreate -> PostType.FOUND
                else -> PostType.LOST // Should not happen
            }
        }

        createButton.setOnClickListener {
            val description = descriptionInput.text.toString()
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null && selectedItemCategory != null) {
                val newPost = Post(
                    id = UUID.randomUUID().toString(),
                    ownerId = user.uid,
                    createdAt = Date().time,
                    type = selectedPostType,
                    latitude = selectedLatitude,
                    longitude = selectedLongitude,
                    text = description,
                    category = selectedItemCategory!!,
                    imageRemoteUrl = null,
                    imageLocalPath = null
                )

                FirebaseFirestore.getInstance().collection("posts")
                    .add(newPost)
                    .addOnSuccessListener {
                        findNavController().navigate(R.id.action_createPost_to_home)
                    }
                    .addOnFailureListener {
                        // Handle the error
                    }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocation() {
        val progressBar = view?.findViewById<ProgressBar>(R.id.locationProgressBar)
        progressBar?.visibility = View.VISIBLE

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                selectedLatitude = it.latitude
                selectedLongitude = it.longitude
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                val address = addresses?.get(0)?.getAddressLine(0)
                view?.findViewById<TextInputEditText>(R.id.locationInput)?.setText(address)
                view?.findViewById<TextInputLayout>(R.id.locationInputLayout)?.hint = ""
                progressBar?.visibility = View.GONE
            }
        }
    }
}