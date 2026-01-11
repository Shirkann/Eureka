package com.example.eureka.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eureka.R
import com.example.eureka.models.FirebaseAuthModel
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val authModel = FirebaseAuthModel.shared

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editTextEmail = view.findViewById<TextInputEditText>(R.id.emailInput)
        val editTextPassword = view.findViewById<TextInputEditText>(R.id.passwordInput)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val registerButton = view.findViewById<TextView>(R.id.RegisterButton)


        val fullText = getString(R.string.registertext)
        val clickableWord = getString(R.string.registerbuttontext)

        val start = fullText.indexOf(clickableWord)
        val end = start + clickableWord.length

        val spannable = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                findNavController().navigate(R.id.action_login_to_register)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = requireContext().getColor(R.color.blue)
            }
        }

        if (start >= 0) {
            spannable.setSpan(
                clickableSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        registerButton.text = spannable
        registerButton.movementMethod = LinkMovementMethod.getInstance()
        registerButton.highlightColor = Color.TRANSPARENT

        // Login button
        loginButton.setOnClickListener {
            val email = editTextEmail.text?.toString()?.trim().orEmpty()
            val password = editTextPassword.text?.toString()?.trim().orEmpty()

            if (email.isBlank() || password.isBlank()) {
                toast("נא למלא אימייל וסיסמה")
                return@setOnClickListener
            }

            authModel.signIn(email, password) { success ->
                if (success) {
                    toast("התחברת בהצלחה ✅")
                    findNavController()
                        .navigate(R.id.action_login_to_home)
                } else {
                    toast("שם משתמש או סיסמה לא נכונים")
                }
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
