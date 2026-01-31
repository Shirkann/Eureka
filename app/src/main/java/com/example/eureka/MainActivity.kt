package com.example.eureka

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    object Globals {
        var appContext: Context? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Globals.appContext = applicationContext
        enableEdgeToEdge()
        setContentView(R.layout.skeleton)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.f1_wrapper) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        // Check if user is logged in
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            navGraph.setStartDestination(R.id.postListFragment)
        } else {
            navGraph.setStartDestination(R.id.loginFragment)
        }
        navController.graph = navGraph


        val appBar = findViewById<AppBarLayout>(R.id.appbar)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = topInset)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { v, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            v.updatePadding(bottom = bottomInset)
            insets
        }

        bottomNav.itemIconTintList = null

        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideBars = destination.id == R.id.loginFragment ||
                    destination.id == R.id.registerFragment

            appBar.visibility = if (hideBars) View.GONE else View.VISIBLE
            bottomNav.visibility = if (hideBars) View.GONE else View.VISIBLE
        }
    }
}