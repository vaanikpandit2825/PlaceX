package com.example.studentplacement.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.studentplacement.R
import com.example.studentplacement.utils.ApiClient
import com.example.studentplacement.utils.Validator
import com.google.android.material.tabs.TabLayout

class RegisterActivity : AppCompatActivity() {
    private var role = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val tabs          = findViewById<TabLayout>(R.id.tabRegRole)
        val studentFields = findViewById<View>(R.id.layoutStudentFields)
        val recruiterFlds = findViewById<View>(R.id.layoutRecruiterFields)

        tabs.addTab(tabs.newTab().setText("🎓 Student"))
        tabs.addTab(tabs.newTab().setText("🏢 Recruiter"))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                role = if (tab.position == 1) "recruiter" else "student"
                studentFields.visibility = if (role == "student")   View.VISIBLE else View.GONE
                recruiterFlds.visibility = if (role == "recruiter") View.VISIBLE else View.GONE
            }
            override fun onTabUnselected(t: TabLayout.Tab) {}
            override fun onTabReselected(t: TabLayout.Tab) {}
        })

        // Student fields
        val etName    = findViewById<EditText>(R.id.etName)
        val etEmail   = findViewById<EditText>(R.id.etEmail)
        val etBranch  = findViewById<EditText>(R.id.etBranch)
        val etCgpa    = findViewById<EditText>(R.id.etCgpa)
        val etPass    = findViewById<EditText>(R.id.etPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmPassword)

        // Recruiter fields
        val etRName   = findViewById<EditText>(R.id.etRecruiterName)
        val etREmail  = findViewById<EditText>(R.id.etRecruiterEmail)
        val etRPass   = findViewById<EditText>(R.id.etRecruiterPassword)
        val etCoName  = findViewById<EditText>(R.id.etCompanyName)
        val etIndustry= findViewById<EditText>(R.id.etIndustry)
        val etCoLoc   = findViewById<EditText>(R.id.etCompanyLocation)

        val btnReg   = findViewById<Button>(R.id.btnRegister)
        val tvLogin  = findViewById<TextView>(R.id.tvLogin)
        val progress = findViewById<ProgressBar>(R.id.progressRegister)

        btnReg.setOnClickListener {
            val params = if (role == "student") {
                val name = etName.text.toString().trim(); val email = etEmail.text.toString().trim()
                val branch = etBranch.text.toString().trim(); val cgpa = etCgpa.text.toString().trim()
                val pass = etPass.text.toString().trim(); val confirm = etConfirm.text.toString().trim()
                when {
                    name.isEmpty()               -> { etName.error   = "Required"; return@setOnClickListener }
                    email.isEmpty()              -> { etEmail.error  = "Required"; return@setOnClickListener }
                    !Validator.isValidEmail(email)-> { etEmail.error  = "Invalid email"; return@setOnClickListener }
                    branch.isEmpty()             -> { etBranch.error = "Required"; return@setOnClickListener }
                    cgpa.isEmpty()               -> { etCgpa.error   = "Required"; return@setOnClickListener }
                    !Validator.isValidCgpa(cgpa) -> { etCgpa.error   = "CGPA must be 0–10"; return@setOnClickListener }
                    pass.length < 6              -> { etPass.error   = "Min 6 chars"; return@setOnClickListener }
                    pass != confirm              -> { etConfirm.error = "Passwords don't match"; return@setOnClickListener }
                }
                mapOf("role" to "student","name" to name,"email" to email,"branch" to branch,"cgpa" to cgpa,"password" to pass)
            } else {
                val rName = etRName.text.toString().trim(); val rEmail = etREmail.text.toString().trim()
                val rPass = etRPass.text.toString().trim(); val coName = etCoName.text.toString().trim()
                val industry = etIndustry.text.toString().trim(); val coLoc = etCoLoc.text.toString().trim()
                when {
                    rName.isEmpty()  -> { etRName.error  = "Required"; return@setOnClickListener }
                    rEmail.isEmpty() -> { etREmail.error = "Required"; return@setOnClickListener }
                    rPass.length < 6 -> { etRPass.error = "Min 6 chars"; return@setOnClickListener }
                    coName.isEmpty() -> { etCoName.error = "Required"; return@setOnClickListener }
                }
                mapOf("role" to "recruiter","name" to rName,"email" to rEmail,"password" to rPass,
                    "company_name" to coName,"industry" to industry,"location" to coLoc)
            }

            progress.visibility = View.VISIBLE; btnReg.isEnabled = false
            val request = object : StringRequest(Method.POST, ApiClient.BASE_URL + "register.php",
                { response ->
                    progress.visibility = View.GONE; btnReg.isEnabled = true
                    val r = response.trim()
                    if (r == "Success") {
                        val msg = if (role == "recruiter") "Registered! Waiting for admin approval." else "Registered! Please login."
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, LoginActivity::class.java)); finish()
                    } else {
                        Toast.makeText(this, "$r", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    progress.visibility = View.GONE; btnReg.isEnabled = true
                    Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_LONG).show()
                }) {
                override fun getParams() = params
            }
            Volley.newRequestQueue(this).add(request)
        }
        tvLogin.setOnClickListener { finish() }
    }
}
