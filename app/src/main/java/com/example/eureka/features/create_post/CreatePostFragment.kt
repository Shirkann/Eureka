package com.example.eureka.features.create_post

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.models.StorageModel
import com.example.eureka.models.ItemCategory
import com.example.eureka.models.Post.Post
import com.example.eureka.models.Post.PostFirebaseModel
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
import com.example.eureka.models.Post.PostsRepository
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
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
    private lateinit var takePictureButton: Button
    private lateinit var chooseFromGalleryButton: Button
    private lateinit var postImage: ImageView

    // State
    private var selectedPostType: PostType = PostType.LOST
    private var selectedItemCategory: ItemCategory? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private var locationName: String? = null
    private var isLoadingLocation = false
    private var addressSuggestions: MutableMap<String, Pair<Double, Double>> = mutableMapOf()
    private var editingPostId: String? = null
    private var imageUri: Uri? = null

    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let { postImage.setImageURI(it) }
        }
    }

    private val chooseFromGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            postImage.setImageURI(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editingPostId = arguments?.getString("postId")

        initViews(view)
        setupUI()

        if (editingPostId != null) {
            loadPostToEdit(editingPostId!!)
        } else {
            checkLocationPermission()
        }
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
        takePictureButton = view.findViewById(R.id.btn_take_picture)
        chooseFromGalleryButton = view.findViewById(R.id.btn_choose_from_gallery)
        postImage = view.findViewById(R.id.post_image)

        requireActivity()
            .findViewById<View?>(R.id.fragment_bg)
            ?.setBackgroundColor(requireContext().getColor(R.color.white))
    }

    private fun setupUI() {
        setupSegmentedControl()
        setupCategoryDropdown()
        setupLocationInput()
        setupCreateButton()
        setupImageButtons()

        if (editingPostId != null) {
            createButton.text = "עדכן פוסט"
        }
    }

    private fun setupImageButtons() {
        takePictureButton.setOnClickListener {
            imageUri = createImageUri()
            takePictureLauncher.launch(imageUri)
        }

        chooseFromGalleryButton.setOnClickListener {
            chooseFromGalleryLauncher.launch("image/*")
        }
    }

    private fun createImageUri(): Uri {
        val imageFile = File(requireContext().externalCacheDir, "${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", imageFile)
    }

    private fun loadPostToEdit(postId: String) {
        PostFirebaseModel().getPostById(postId) { post ->
            if (post != null) {
                selectedPostType = post.type ?: PostType.LOST
                if (selectedPostType == PostType.LOST) {
                    group.check(R.id.btn_lostCreate)
                } else {
                    group.check(R.id.btn_foundCreate)
                }

                descriptionInput.setText(post.text)
                selectedItemCategory = post.category
                itemTypeInput.setText(getItemCategoryName(post.category), false)

                locationInput.setText(post.locationName)
                locationName = post.locationName
                selectedLatitude = post.latitude
                selectedLongitude = post.longitude

                post.imageRemoteUrl?.let {
                    Picasso.get().load(it).into(postImage)
                }
            }
        }
    }

    private fun getItemCategoryName(category: ItemCategory): String {
        return when (category) {
            ItemCategory.PHONE -> "טלפון"
            ItemCategory.KEYS -> "מפתחות"
            ItemCategory.WALLET -> "ארנק"
            ItemCategory.BAG -> "תיק"
            ItemCategory.OTHER -> "אחר"
        }
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
                if (!isLoadingLocation) {
                    locationName = s?.toString()

                    if ((s?.length ?: 0) >= 3) {
                        searchAddresses(s.toString())
                    }
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        locationInput.setOnItemClickListener { _, _, position, _ ->
            val selectedText = (locationInput.adapter as? ArrayAdapter<*>)?.getItem(position) as? String
            selectedText?.let {
                locationInput.setText(it)
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
                val addresses = geocoder.getFromLocationName(query, 5)

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
            if (editingPostId != null) {
                updatePost()
            } else {
                createPost()
            }
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
            Toast.makeText(requireContext(), "Please select an item category", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            StorageModel().uploadImage(imageUri!!) { imageUrl ->
                if (imageUrl != null) {
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
                        imageRemoteUrl = imageUrl,
                        imageLocalPath = null,
                        lastUpdated = Date().time
                    )
                    savePost(newPost)
                } else {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
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
                lastUpdated = Date().time
            )
            savePost(newPost)
        }
    }

    private fun savePost(post: Post) {
        PostsRepository.shared.addPost(post) { success ->
            if (success) {
                findNavController().navigate(R.id.action_createPost_to_home)
            } else {
                Toast.makeText(requireContext(), "Failed to create post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePost() {
        val user = Firebase.auth.currentUser
        val description = descriptionInput.text?.toString().orEmpty()

        if (user == null || editingPostId == null) return

        if (selectedItemCategory == null) {
            Toast.makeText(requireContext(), "Please select an item category", Toast.LENGTH_SHORT).show()
            return
        }

        PostFirebaseModel().getPostById(editingPostId!!) { originalPost ->
            if (originalPost != null) {
                if (imageUri != null) {
                    StorageModel().uploadImage(imageUri!!) { imageUrl ->
                        if (imageUrl != null) {
                            val updatedPost = originalPost.copy(
                                type = selectedPostType,
                                latitude = selectedLatitude,
                                longitude = selectedLongitude,
                                locationName = locationName,
                                text = description,
                                category = selectedItemCategory!!,
                                imageRemoteUrl = imageUrl,
                                lastUpdated = Date().time
                            )
                            saveUpdatedPost(updatedPost)
                        } else {
                            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val updatedPost = originalPost.copy(
                        type = selectedPostType,
                        latitude = selectedLatitude,
                        longitude = selectedLongitude,
                        locationName = locationName,
                        text = description,
                        category = selectedItemCategory!!,
                        lastUpdated = Date().time
                    )
                    saveUpdatedPost(updatedPost)
                }
            }
        }
    }

    private fun saveUpdatedPost(post: Post) {
        PostsRepository.shared.updatePost(post) { success ->
            if (success) {
                Toast.makeText(requireContext(), "פוסט עודכן בהצלחה", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "עדכון נכשל", Toast.LENGTH_SHORT).show()
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
        val ctx = context ?: return

        isLoadingLocation = true
        locationInput.setText("מאתר מיקום...")

        getCurrentLocation(
            context = ctx,
            onSuccess = { lat, lng ->
                if (!isAdded || view == null) return@getCurrentLocation

                val safeContext = context ?: return@getCurrentLocation

                selectedLatitude = lat
                selectedLongitude = lng

                locationName = getAddressFromLocation(safeContext, lat, lng)

                locationInput.setText(locationName ?: "כתובת לא נמצאה")
                locationInputLayout.hint = ""
                isLoadingLocation = false
            },
            onError = { errorMessage ->
                if (!isAdded || view == null) return@getCurrentLocation

                val safeContext = context ?: return@getCurrentLocation

                locationInput.setText("לא ניתן לאתר מיקום")
                Toast.makeText(safeContext, errorMessage, Toast.LENGTH_SHORT).show()
                isLoadingLocation = false
            }
        )
    }

}
