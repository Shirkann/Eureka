package com.example.eureka.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.models.Model
import com.example.eureka.models.User
import com.example.eureka.utils.AuthValidator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
                    // 1. Create user in Firebase Auth
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = firebaseAuth.currentUser
                                if (firebaseUser == null) {
                                    toast("שגיאה: משתמש לא נמצא אחרי הרשמה")
                                    return@addOnCompleteListener
                                }

                                // 2. Create user object to save in Firestore
                                val userMap = hashMapOf(
                                    "id" to firebaseUser.uid,
                                    "email" to email,
                                    "fullName" to fullName,
                                    "avatar" to null
                                )

                                // 3. Save user to Firestore
                                firestore.collection("users").document(firebaseUser.uid)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        // 4. Save user to local Room DB
                                        val user = User(
                                            id = firebaseUser.uid,
                                            fullName = fullName,
                                            email = email,
                                            avatar = null
                                        )
                                        Model.shared.addUser(user) {
                                            toast("נרשמת בהצלחה ✅")
                                            findNavController().popBackStack()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        firebaseUser.delete()
                                        toast("שמירת פרטי משתמש נכשלה: ${e.message}")
                                    }
                            } else {
                                toast("הרשמה נכשלה: ${task.exception?.message}")
                            }
                        }
                }
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
