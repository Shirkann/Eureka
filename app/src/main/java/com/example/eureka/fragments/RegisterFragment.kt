package com.example.eureka.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.models.FirebaseAuthModel
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
            val email = emailInput.text?.toString()?.trim().orEmpty()
            val password = passwordInput.text?.toString()?.trim().orEmpty()
            val fullname = fullnameInput.text?.toString()?.trim().orEmpty()

            if (email.isBlank() || password.isBlank() || fullname.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    "נא למלא את כל השדות",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            authModel.createUser(email, password, fullname) { success ->
                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "נרשמת בהצלחה 🎉",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController()
                        .navigate(R.id.action_register_to_home)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "שגיאה בהרשמה",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        backButton.setOnClickListener {
            findNavController()
                .navigate(R.id.action_register_to_login)
        }
    }
}
