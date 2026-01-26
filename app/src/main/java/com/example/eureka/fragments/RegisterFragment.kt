
package com.example.eureka.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.models.FirebaseManager
import com.google.android.material.textfield.TextInputEditText

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private val authModel = FirebaseAuthModel.shared

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerButton = view.findViewById<Button>(R.id.registerButton)
        val backButton = view.findViewById<Button>(R.id.backButton)
        val emailInput = view.findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.passwordInput)
        val fullnameInput = view.findViewById<TextInputEditText>(R.id.fullnameInput)

        registerButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val fullname = fullnameInput.text.toString()

            FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseManager.auth.currentUser
                        user?.let {
                            val userMap = hashMapOf("fullname" to fullname)
                            FirebaseManager.db.collection("users").document(it.uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    findNavController().navigate(R.id.action_register_to_home)
                                }
                        }
                    }
                }
        }

        backButton.setOnClickListener {
            findNavController()
                .navigate(R.id.action_register_to_login)
        }
    }
}
