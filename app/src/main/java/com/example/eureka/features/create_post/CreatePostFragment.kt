package com.example.eureka.features.create_post

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.models.FireBaseModel
import com.example.eureka.models.ItemCategory
import com.example.eureka.models.Post.Post
import com.example.eureka.models.Post.PostType
import com.example.eureka.utils.LocationUtils.getAddressFromLocation
import com.example.eureka.utils.LocationUtils.getCurrentLocation
import com.example.eureka.utils.LocationUtils.hasLocationPermission
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import android.location.Geocoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreatePostFragment : Fragment(R.layout.fragment_createpost) {

    // View References
    private lateinit var group: MaterialButtonToggleGroup
    private lateinit var btnLostCreate: MaterialButton
    private lateinit var btnFoundCreate: MaterialButton
    private lateinit var createButton: Button
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var itemTypeInput: MaterialAutoCompleteTextView
    private lateinit var locationInput: MaterialAutoCompleteTextView
    private lateinit var locationInputLayout: TextInputLayout

    // State
    private var selectedPostType: PostType = PostType.LOST
    private var selectedItemCategory: ItemCategory? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var locationName: String? = null
    private var isLoadingLocation = false  // Flag to prevent TextWatcher from interfering during GPS load
    private var addressSuggestions: MutableMap<String, Pair<Double, Double>> = mutableMapOf()  // Store address -> (lat, lng)


    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
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

        requireActivity()
            .findViewById<View?>(R.id.fragment_bg)
            ?.setBackgroundColor(requireContext().getColor(R.color.white))
    }

    private fun setupUI() {
        setupSegmentedControl()
        setupCategoryDropdown()
        setupLocationInput()
        setupCreateButton()
    }

    private fun setupSegmentedControl() {
        val startCheckedId =
            group.checkedButtonId.takeIf { it != View.NO_ID } ?: R.id.btn_lostCreate

        applyTextStyles(startCheckedId)
        selectedPostType =
            if (startCheckedId == R.id.btn_lostCreate) PostType.LOST else PostType.FOUND

        group.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            applyTextStyles(checkedId)
            selectedPostType = when (checkedId) {
                R.id.btn_lostCreate -> PostType.LOST
                R.id.btn_foundCreate -> PostType.FOUND
                else -> PostType.LOST
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

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            itemCategoryMap.values.toList()
        )

        itemTypeInput.setAdapter(adapter)

        itemTypeInput.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position) as String
            selectedItemCategory =
                itemCategoryMap.entries.firstOrNull { it.value == selectedName }?.key
        }

        itemTypeInput.setOnClickListener {
            itemTypeInput.showDropDown()
        }
    }

    private fun setupLocationInput() {
        locationInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Only update locationName if user is manually editing (not while loading GPS)
                if (!isLoadingLocation) {
                    locationName = s?.toString()

                    // Search for address suggestions when user types
                    if ((s?.length ?: 0) >= 3) {
                        searchAddresses(s.toString())
                    }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Handle selection from autocomplete suggestions
        locationInput.setOnItemClickListener { _, _, position, _ ->
            val selectedText = (locationInput.adapter as? ArrayAdapter<*>)?.getItem(position) as? String
            selectedText?.let {
                locationInput.setText(it)
                // Get coordinates for the selected address
                val coords = addressSuggestions[it]
                if (coords != null) {
                    selectedLatitude = coords.first
                    selectedLongitude = coords.second
                    locationName = it
                }
            }
        }
    }

    private fun searchAddresses(query: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocationName(query, 5)  // Get up to 5 suggestions

                addressSuggestions.clear()
                val suggestions = mutableListOf<String>()

                addresses?.forEach { address ->
                    val fullAddress = address.getAddressLine(0)
                    addressSuggestions[fullAddress] = Pair(address.latitude, address.longitude)
                    suggestions.add(fullAddress)
                }

                if (suggestions.isNotEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            suggestions
                        )
                        locationInput.setAdapter(adapter)
                        locationInput.showDropDown()
                    }
                }
            } catch (_: Exception) {
                // Silently fail, user can still edit manually
            }
        }
    }

    private fun setupCreateButton() {
        createButton.setOnClickListener {
            createPost()
        }
    }

    private fun createPost() {
        val user = Firebase.auth.currentUser
        val description = descriptionInput.text?.toString().orEmpty()

        if (user == null) {
            Toast.makeText(requireContext(), "You must be logged in", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedItemCategory == null) {
            Toast.makeText(
                requireContext(),
                "Please select an item category",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val newPost = Post(
            id = UUID.randomUUID().toString(),
            ownerId = user.uid,
            createdAt = Date().time,
            type = selectedPostType,
            latitude = selectedLatitude,
            longitude = selectedLongitude,
            locationName = locationName,
            text = description,
            category = selectedItemCategory!!,
            imageRemoteUrl = null,
            imageLocalPath = null,
            lastUpdated = null
        )


        FireBaseModel().addPost(newPost) { success ->
            if (success) {
                findNavController()
                    .navigate(R.id.action_createPost_to_home)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to create post",
                    Toast.LENGTH_SHORT
                ).show()
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
        if (hasLocationPermission(requireContext())) {
            @Suppress("MissingPermission")
            loadLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun loadLocation() {
        isLoadingLocation = true
        locationInput.setText("מאתר מיקום...")

        getCurrentLocation(
            context = requireContext(),
            onSuccess = { lat, lng ->
                selectedLatitude = lat
                selectedLongitude = lng

                locationName =
                    getAddressFromLocation(requireContext(), lat, lng)

                locationInput.setText(locationName ?: "כתובת לא נמצאה")
                locationInputLayout.hint = ""
                isLoadingLocation = false
            },
            onError = {
                locationInput.setText("לא ניתן לאתר מיקום")
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                isLoadingLocation = false
            }
        )
    }

}
