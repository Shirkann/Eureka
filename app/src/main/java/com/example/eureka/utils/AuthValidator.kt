package com.example.eureka.utils

import android.util.Patterns

object AuthValidator {

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }
}
