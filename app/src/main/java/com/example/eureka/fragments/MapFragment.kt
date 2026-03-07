package com.example.eureka.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.databinding.FragmentMapBinding
import com.example.eureka.models.FireBaseModel
import com.example.eureka.models.Post.Post
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var binding: FragmentMapBinding? = null
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private val posts = mutableListOf<Post>()
    private val postsByLocation = mutableMapOf<String, MutableList<Post>>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                enableMyLocation()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMapBinding.bind(view)

        // ensure the map is not null
        val binding = FragmentMapBinding.bind(view)
        this.binding = binding
        mapView = binding.mapView

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        checkLocationPermission()
        loadPosts()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupMap()
        addMarkers()
    }

    private fun setupMap() {
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true

            val defaultLocation = LatLng(31.0461, 34.8516)
            moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 7f))

            setOnMarkerClickListener { marker ->
                val key = marker.tag as? String
                if (key != null) {
                    val list = postsByLocation[key] ?: emptyList()

                    if (list.isEmpty()) {
                        val id = posts.find { "${it.latitude},${it.longitude}" == key }?.id
                        if (id != null) {
                            findNavController().navigate(R.id.action_map_to_post_detail, bundleOf("postId" to id))
                        }
                    } else if (list.size == 1) {
                        findNavController().navigate(R.id.action_map_to_post_detail, bundleOf("postId" to list[0].id))
                    } else {
                        showDialog(list)
                    }
                }
                true
            }
        }
    }

    private fun showDialog(list: List<Post>) {
        val items = list.mapIndexed { i, post ->
            val type = post.type?.name ?: "Unknown"
            val category = post.category.name
            "${ i + 1}. $type - $category"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("${list.size} posts here")
            .setItems(items) { _, which ->
                findNavController().navigate(R.id.action_map_to_post_detail, bundleOf("postId" to list[which].id))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addMarkers() {
        googleMap?.clear()
        postsByLocation.clear()

        posts.forEach { post ->
            if (post.latitude != null && post.longitude != null) {
                val key = "${post.latitude},${post.longitude}"

                if (!postsByLocation.containsKey(key)) {
                    postsByLocation[key] = mutableListOf()
                }
                postsByLocation[key]?.add(post)
            }
        }

        postsByLocation.forEach { (key, list) ->
            val parts = key.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()

                if (lat != null && lng != null) {
                    val pos = LatLng(lat, lng)

                    val title = if (list.size > 1) "${list.size} posts" else list[0].locationName ?: "Location"
                    val snippet = list.map { it.type?.name ?: "Unknown" }.distinct().joinToString(", ")

                    val marker = googleMap?.addMarker(
                        MarkerOptions().position(pos).title(title).snippet(snippet)
                    )
                    marker?.tag = key
                }
            }
        }
    }

    private fun loadPosts() {
        posts.clear()
        FireBaseModel().getPostsByType(0L, com.example.eureka.models.Post.PostType.LOST, 100) { lost ->
            posts.addAll(lost)
            FireBaseModel().getPostsByType(0L, com.example.eureka.models.Post.PostType.FOUND, 100) { found ->
                posts.addAll(found)
                addMarkers()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}