package com.example.eureka.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.utils.AuthValidator
import com.example.eureka.utils.AuthValidator.hashPassword
import com.example.eureka.models.Model
import com.example.eureka.models.User
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextEmail = view.findViewById<TextInputEditText>(R.id.emailInput)
        val editTextPassword = view.findViewById<TextInputEditText>(R.id.passwordInput)
        val editTextFullName = view.findViewById<TextInputEditText>(R.id.fullnameInput)
        val registerButton = view.findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = editTextEmail.text?.toString()?.trim().orEmpty()
            val password = editTextPassword.text?.toString()?.trim().orEmpty()
            val fullName = editTextFullName.text?.toString()?.trim().orEmpty()

            when {
                !AuthValidator.isValidEmail(email) -> {
                    toast("אימייל לא תקין")
                    editTextEmail.requestFocus()
                }

                !AuthValidator.isValidPassword(password) -> {
                    toast("הסיסמה חייבת להיות לפחות 6 תווים")
                    editTextPassword.requestFocus()
                }

                fullName.isBlank() -> {
                    toast("נא להזין שם מלא")
                    editTextFullName.requestFocus()
                }

                else -> {
                    val user = User(
                        id = UUID.randomUUID().toString(),
                        fullName = fullName,
                        email = email,
                        avatar = null,
                    )
                    Model.shared.addUser(user) {
                        toast("נרשמת בהצלחה ✅")
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
