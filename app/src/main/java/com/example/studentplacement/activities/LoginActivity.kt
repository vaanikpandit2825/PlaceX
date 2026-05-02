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
import com.example.studentplacement.utils.SessionManager
import com.example.studentplacement.utils.Validator
import com.google.android.material.tabs.TabLayout
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private var selectedRole = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        session = SessionManager(this)

        val tabs     = findViewById<TabLayout>(R.id.tabRole)
        val etEmail  = findViewById<EditText>(R.id.etEmail)
        val etPass   = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvReg    = findViewById<TextView>(R.id.tvRegister)
        val tvDemo   = findViewById<TextView>(R.id.tvDemoLogin)
        val progress = findViewById<ProgressBar>(R.id.progressLogin)

        tabs.addTab(tabs.newTab().setText(" Student"))
        tabs.addTab(tabs.newTab().setText(" Recruiter"))
        tabs.addTab(tabs.newTab().setText(" Admin"))

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedRole = when (tab.position) { 1 -> "recruiter"; 2 -> "admin"; else -> "student" }
                tvReg.visibility = if (selectedRole == "student" || selectedRole == "recruiter") View.VISIBLE else View.GONE
                tvDemo.visibility = if (selectedRole == "student") View.VISIBLE else View.GONE
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPass.text.toString().trim()
            when {
                email.isEmpty() -> { etEmail.error = "Enter email"; return@setOnClickListener }
                pass.isEmpty()  -> { etPass.error  = "Enter password"; return@setOnClickListener }
            }
            progress.visibility = View.VISIBLE; btnLogin.isEnabled = false

            val req = object : StringRequest(Method.POST, ApiClient.BASE_URL + "login.php",
                { response ->
                    progress.visibility = View.GONE; btnLogin.isEnabled = true
                    val r = response.trim()
                    if (r.startsWith("{")) {
                        val obj = JSONObject(r)
                        if (obj.getString("status") == "Success") {
                            val role = obj.getString("role")
                            when (role) {
                                "admin" -> {
                                    session.saveAdminSession(obj.getInt("admin_id"), obj.getString("name"))
                                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                                }
                                "recruiter" -> {
                                    session.saveRecruiterSession(obj.getInt("recruiter_id"),
                                        obj.getString("name"), obj.getInt("company_id"),
                                        obj.getString("company_name"))
                                    startActivity(Intent(this, RecruiterDashboardActivity::class.java))
                                }
                                else -> {
                                    session.saveStudentSession(obj.getInt("student_id"), email,
                                        obj.getString("name"), obj.getInt("is_placed") == 1)
                                    startActivity(Intent(this, DashboardActivity::class.java))
                                }
                            }
                            finish()
                        }
                    } else {
                        Toast.makeText(this, " $r", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    progress.visibility = View.GONE; btnLogin.isEnabled = true
                    Toast.makeText(this, "Network error — is XAMPP running?\n${error.message}", Toast.LENGTH_LONG).show()
                }) {
                override fun getParams() = mapOf("email" to email, "password" to pass, "role" to selectedRole)
            }
            Volley.newRequestQueue(this).add(req)
        }

        tvReg.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        tvDemo.setOnClickListener { etEmail.setText("arjun@demo.com"); etPass.setText("demo123") }
    }
}
