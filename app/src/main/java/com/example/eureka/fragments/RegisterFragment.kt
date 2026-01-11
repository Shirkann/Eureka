package com.example.eureka.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
<<<<<<< Updated upstream
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
=======
import com.example.eureka.models.FirebaseAuthModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
>>>>>>> Stashed changes

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val authModel = FirebaseAuthModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registerButton = view.findViewById<Button>(R.id.registerButton)
        val backButton = view.findViewById<Button>(R.id.backButton)
        val emailInput = view.findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.passwordInput)
        val fullnameInput = view.findViewById<TextInputEditText>(R.id.fullnameInput)

        registerButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val fullname = fullnameInput.text.toString().trim()

<<<<<<< Updated upstream
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

=======
            if (email.isEmpty() || password.isEmpty() || fullname.isEmpty()) {
                Toast.makeText(requireContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authModel.createUser(email, password) { success: Boolean ->
                if (success) {
                    val user = Firebase.auth.currentUser

                    user?.let {
                        val userMap = hashMapOf(
                            "fullname" to fullname,
                            "email" to email
                        )

                        Firebase.firestore
                            .collection("users")
                            .document(it.uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "נרשמת בהצלחה 🎉", Toast.LENGTH_SHORT).show()
                                findNavController()
                                    .navigate(R.id.action_register_to_home)
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "שגיאה בשמירת משתמש", Toast.LENGTH_SHORT).show()
                            }
>>>>>>> Stashed changes
                    }
                } else {
                    Toast.makeText(requireContext(), "הרשמה נכשלה ❌", Toast.LENGTH_SHORT).show()
                }
<<<<<<< Updated upstream

=======
            }
>>>>>>> Stashed changes
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }
}