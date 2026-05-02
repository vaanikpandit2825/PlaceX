package com.example.studentplacement.activities

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
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class ResumeActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume)
        session = SessionManager(this)

        val etSkills = findViewById<EditText>(R.id.etSkills)
        val etExp    = findViewById<EditText>(R.id.etExperience)
        val etProj   = findViewById<EditText>(R.id.etProjects)
        val etCerts  = findViewById<EditText>(R.id.etCertifications)
        val btnSave  = findViewById<MaterialButton>(R.id.btnSaveResume)
        val btnEdit  = findViewById<MaterialButton>(R.id.btnEditResume)
        val btnBack  = findViewById<ImageButton>(R.id.btnBack)
        val tvName   = findViewById<TextView>(R.id.tvStudentName)
        val tvEmail  = findViewById<TextView>(R.id.tvStudentEmail)
        val tvBranch = findViewById<TextView>(R.id.tvStudentBranch)
        val tvCgpa   = findViewById<TextView>(R.id.tvStudentCgpa)

        tvName.text  = session.getStudentName()
        tvEmail.text = session.getStudentEmail()
        btnBack.setOnClickListener { finish() }

        fun setEditMode(edit: Boolean) {
            isEditMode = edit
            listOf(etSkills, etExp, etProj, etCerts).forEach { it.isEnabled = edit }
            btnSave.visibility = if (edit) View.VISIBLE else View.GONE
            btnEdit.visibility = if (edit) View.GONE    else View.VISIBLE
        }
        setEditMode(false)

        // Load student info + resume from MySQL
        val reqStudent = object : StringRequest(Request.Method.POST,
            ApiClient.BASE_URL + "get_student.php",
            { response ->
                if (response.trim().startsWith("{")) {
                    val obj = JSONObject(response)
                    tvBranch.text = obj.optString("branch")
                    tvCgpa.text   = "CGPA: ${obj.optString("cgpa")}"
                }
            }, {}) {
            override fun getParams() = mapOf("student_id" to session.getStudentId().toString())
        }
        Volley.newRequestQueue(this).add(reqStudent)

        val reqResume = object : StringRequest(Request.Method.POST,
            ApiClient.BASE_URL + "get_resume.php",
            { response ->
                if (response.trim().startsWith("{")) {
                    val obj = JSONObject(response)
                    etSkills.setText(obj.optString("skills"))
                    etExp.setText(obj.optString("experience"))
                    etProj.setText(obj.optString("projects"))
                    etCerts.setText(obj.optString("certifications"))
                }
            }, {}) {
            override fun getParams() = mapOf("student_id" to session.getStudentId().toString())
        }
        Volley.newRequestQueue(this).add(reqResume)

        btnEdit.setOnClickListener { setEditMode(true) }

        btnSave.setOnClickListener {
            val saveReq = object : StringRequest(Request.Method.POST,
                ApiClient.BASE_URL + "save_resume.php",
                { response ->
                    if (response.trim() == "Success") {
                        Toast.makeText(this, "Resume saved!", Toast.LENGTH_SHORT).show()
                        setEditMode(false)
                    } else {
                        Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
                }) {
                override fun getParams() = mapOf(
                    "student_id"     to session.getStudentId().toString(),
                    "skills"         to etSkills.text.toString().trim(),
                    "experience"     to etExp.text.toString().trim(),
                    "projects"       to etProj.text.toString().trim(),
                    "certifications" to etCerts.text.toString().trim()
                )
            }
            Volley.newRequestQueue(this).add(saveReq)
        }
    }
}
