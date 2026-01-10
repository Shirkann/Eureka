package com.example.eureka.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.models.FireBaseModel
import com.example.eureka.models.FirebaseManager
import com.example.eureka.models.ItemCategory
import com.example.eureka.models.Post
import com.example.eureka.models.PostType
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Date
import java.util.Locale
import java.util.UUID

@Suppress("DEPRECATION")
class CreatePostFragment : Fragment(R.layout.fragment_createpost) {

    // View References
    private lateinit var group: MaterialButtonToggleGroup
    private lateinit var btnLostCreate: MaterialButton
    private lateinit var btnFoundCreate: MaterialButton
    private lateinit var createButton: Button
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var itemTypeInput: MaterialAutoCompleteTextView
    private lateinit var locationInput: TextInputEditText
    private lateinit var locationInputLayout: TextInputLayout

    // State Variables
    private var selectedPostType: PostType = PostType.LOST
    private var selectedItemCategory: ItemCategory? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    // Model
    private val fireBaseModel = FireBaseModel()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupUI()
        checkLocationPermission()
    }

    private fun initViews(view: View) {
        group = view.findViewById(R.id.segmentedCreate)
        btnLostCreate = view.findViewById(R.id.btn_lostCreate)
        btnFoundCreate = view.findViewById(R.id.btn_foundCreate)
        createButton = view.findViewById(R.id.createButton)
        descriptionInput = view.findViewById(R.id.descriptionInput)
        itemTypeInput = view.findViewById(R.id.itemTypeInput)
        locationInput = view.findViewById(R.id.locationInput)
        locationInputLayout = view.findViewById(R.id.locationInputLayout)

        requireActivity().findViewById<View?>(R.id.fragment_bg)
            ?.setBackgroundColor(requireContext().getColor(R.color.white))
    }

    private fun setupUI() {
        setupSegmentedControl()
        setupCategoryDropdown()
        setupCreateButton()
    }

    private fun setupSegmentedControl() {
        // Set initial state
        val startCheckedId = group.checkedButtonId.takeIf { it != View.NO_ID } ?: R.id.btn_lostCreate
        applyTextStyles(startCheckedId)
        selectedPostType = if (startCheckedId == R.id.btn_lostCreate) PostType.LOST else PostType.FOUND

        // Add listener for changes
        group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            applyTextStyles(checkedId)
            selectedPostType = when (checkedId) {
                R.id.btn_lostCreate -> PostType.LOST
                R.id.btn_foundCreate -> PostType.FOUND
                else -> PostType.LOST // Should not happen
            }
        }
    }

    private fun setupCategoryDropdown() {
        val itemCategoryMap = mapOf(
            ItemCategory.PHONE to "טלפון",
            ItemCategory.KEYS to "מפתחות",
            ItemCategory.WALLET to "ארנק",
            ItemCategory.BAG to "תיק",
            ItemCategory.OTHER to "אחר"
        )
        val itemTypeNames = itemCategoryMap.values.toList()
        val itemTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, itemTypeNames)

        itemTypeInput.setAdapter(itemTypeAdapter)
        itemTypeInput.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position) as String
            selectedItemCategory = itemCategoryMap.entries.find { it.value == selectedName }?.key
        }
        itemTypeInput.setOnClickListener { itemTypeInput.showDropDown() }
    }

    private fun setupCreateButton() {
        createButton.setOnClickListener {
            createPost()
        }
    }

    private fun createPost() {
        val user = FirebaseManager.auth.currentUser
        val description = descriptionInput.text.toString()

        if (user == null) {
            Toast.makeText(requireContext(), "You must be logged in to create a post", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedItemCategory == null) {
            Toast.makeText(requireContext(), "Please select an item category", Toast.LENGTH_SHORT).show()
            return
        }

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

        fireBaseModel.addPost(newPost) { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.action_createPost_to_home)
            } else {
                Toast.makeText(requireContext(), "Failed to create post", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun applyTextStyles(checkedId: Int) {
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

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocation() {
        locationInput.setText("מאחזר מיקום...")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    selectedLatitude = location.latitude
                    selectedLongitude = location.longitude
                    getTextFromLocation(location.latitude, location.longitude)
                } else {
                    locationInput.setText("Unable to get location")
                }
            }
            .addOnFailureListener {
                locationInput.setText("Failed to get location")
                Toast.makeText(requireContext(), "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getTextFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()?.getAddressLine(0)
            locationInput.setText(address ?: "Address not found")
            locationInputLayout.hint = ""
        } catch (e: Exception) {
            locationInput.setText("Unable to get address")
        }
    }
}
