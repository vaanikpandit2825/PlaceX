package com.example.studentplacement.utils

object Validator {
    fun isValidEmail(email: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPhone(phone: String) =
        phone.length == 10 && phone.all { it.isDigit() }

    fun isValidCgpa(cgpa: String): Boolean {
        val v = cgpa.toDoubleOrNull() ?: return false
        return v in 0.0..10.0
    }

    fun isValidPercent(p: String): Boolean {
        val v = p.toDoubleOrNull() ?: return false
        return v in 0.0..100.0
    }
}
