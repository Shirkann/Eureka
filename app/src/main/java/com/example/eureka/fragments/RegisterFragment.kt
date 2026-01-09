package com.example.eureka.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment(R.layout.fragment_register) {

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

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        val db = FirebaseFirestore.getInstance()
                        val userMap = hashMapOf(
                            "fullname" to fullname
                        )
                        if (user != null) {
                            db.collection("users").document(user.uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    findNavController().navigate(R.id.action_register_to_home)
                                }
                        }

                    }
                }

        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }
}