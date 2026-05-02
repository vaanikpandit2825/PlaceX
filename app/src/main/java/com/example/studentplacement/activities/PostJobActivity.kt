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

class PostJobActivity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_job)
        session = SessionManager(this)

        val etTitle    = findViewById<EditText>(R.id.etJobTitle)
        val etDesc     = findViewById<EditText>(R.id.etJobDesc)
        val etPkg      = findViewById<EditText>(R.id.etPackage)
        val etMinCgpa  = findViewById<EditText>(R.id.etMinCgpa)
        val etMinTenth = findViewById<EditText>(R.id.etMinTenth)
        val etMin12    = findViewById<EditText>(R.id.etMinTwelfth)
        val etSkills   = findViewById<EditText>(R.id.etReqSkills)
        val etBranch   = findViewById<EditText>(R.id.etAllowedBranch)
        val etLocation = findViewById<EditText>(R.id.etJobLocation)
        val etDeadline = findViewById<EditText>(R.id.etDeadline)
        val spinType   = findViewById<Spinner>(R.id.spinnerJobType)
        val btnPost    = findViewById<MaterialButton>(R.id.btnPostJob)
        val progress   = findViewById<ProgressBar>(R.id.progressPost)

        ArrayAdapter.createFromResource(this, R.array.job_types, android.R.layout.simple_spinner_item)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinType.adapter = it }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        btnPost.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc  = etDesc.text.toString().trim()
            if (title.isEmpty()) { etTitle.error = "Required"; return@setOnClickListener }
            if (desc.isEmpty())  { etDesc.error  = "Required"; return@setOnClickListener }

            progress.visibility = View.VISIBLE; btnPost.isEnabled = false
            val req = object : StringRequest(Request.Method.POST, ApiClient.BASE_URL + "post_job.php",
                { response ->
                    progress.visibility = View.GONE; btnPost.isEnabled = true
                    if (response.trim() == "Success") {
                        Toast.makeText(this, "Job posted successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, response, Toast.LENGTH_LONG).show()
                    }
                },
                { error ->
                    progress.visibility = View.GONE; btnPost.isEnabled = true
                    Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_LONG).show()
                }) {
                override fun getParams() = mapOf(
                    "company_id"      to session.getCompanyId().toString(),
                    "recruiter_id"    to session.getRecruiterId().toString(),
                    "title"           to title,
                    "description"     to desc,
                    "package_lpa"     to (etPkg.text.toString().ifEmpty { "0" }),
                    "min_cgpa"        to (etMinCgpa.text.toString().ifEmpty { "6.0" }),
                    "min_10th"   to (etMinTenth.text.toString().ifEmpty { "60.0" }),
                    "min_12th"   to (etMin12.text.toString().ifEmpty { "60.0" }),
                    "required_skills" to etSkills.text.toString().trim(),
                    "allowed_branch"  to etBranch.text.toString().trim(),
                    "location"        to etLocation.text.toString().trim(),
                    "job_type"        to spinType.selectedItem.toString(),
                    "deadline"        to etDeadline.text.toString().trim()
                )
            }
            Volley.newRequestQueue(this).add(req)
        }
    }
}
