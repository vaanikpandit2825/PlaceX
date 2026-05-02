package com.example.studentplacement.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("PlaceXSession", Context.MODE_PRIVATE)

    companion object {
        const val KEY_ROLE          = "role"
        const val KEY_STUDENT_ID    = "student_id"
        const val KEY_STUDENT_EMAIL = "student_email"
        const val KEY_STUDENT_NAME  = "student_name"
        const val KEY_IS_PLACED     = "is_placed"
        const val KEY_ADMIN_ID      = "admin_id"
        const val KEY_RECRUITER_ID  = "recruiter_id"
        const val KEY_COMPANY_ID    = "company_id"
        const val KEY_COMPANY_NAME  = "company_name"
        const val KEY_IS_LOGGED_IN  = "is_logged_in"
    }

    fun saveStudentSession(id: Int, email: String, name: String, isPlaced: Boolean) {
        prefs.edit().apply {
            putString(KEY_ROLE, "student"); putInt(KEY_STUDENT_ID, id)
            putString(KEY_STUDENT_EMAIL, email); putString(KEY_STUDENT_NAME, name)
            putBoolean(KEY_IS_PLACED, isPlaced); putBoolean(KEY_IS_LOGGED_IN, true); apply()
        }
    }
    fun saveAdminSession(id: Int, name: String) {
        prefs.edit().apply {
            putString(KEY_ROLE, "admin"); putInt(KEY_ADMIN_ID, id)
            putString(KEY_STUDENT_NAME, name); putBoolean(KEY_IS_LOGGED_IN, true); apply()
        }
    }
    fun saveRecruiterSession(id: Int, name: String, companyId: Int, companyName: String) {
        prefs.edit().apply {
            putString(KEY_ROLE, "recruiter"); putInt(KEY_RECRUITER_ID, id)
            putString(KEY_STUDENT_NAME, name); putInt(KEY_COMPANY_ID, companyId)
            putString(KEY_COMPANY_NAME, companyName); putBoolean(KEY_IS_LOGGED_IN, true); apply()
        }
    }
    // Also keep old saveSession for backward compat
    fun saveSession(id: Int, email: String, name: String) = saveStudentSession(id, email, name, false)

    fun getRole()         = prefs.getString(KEY_ROLE, "student") ?: "student"
    fun getStudentId()    = prefs.getInt(KEY_STUDENT_ID, -1)
    fun getStudentEmail() = prefs.getString(KEY_STUDENT_EMAIL, "") ?: ""
    fun getStudentName()  = prefs.getString(KEY_STUDENT_NAME, "") ?: ""
    fun isPlaced()        = prefs.getBoolean(KEY_IS_PLACED, false)
    fun getAdminId()      = prefs.getInt(KEY_ADMIN_ID, -1)
    fun getRecruiterId()  = prefs.getInt(KEY_RECRUITER_ID, -1)
    fun getCompanyId()    = prefs.getInt(KEY_COMPANY_ID, -1)
    fun getCompanyName()  = prefs.getString(KEY_COMPANY_NAME, "") ?: ""
    fun isLoggedIn()      = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun updatePlacedStatus(placed: Boolean) { prefs.edit().putBoolean(KEY_IS_PLACED, placed).apply() }
    fun clearSession()    { prefs.edit().clear().apply() }
}
