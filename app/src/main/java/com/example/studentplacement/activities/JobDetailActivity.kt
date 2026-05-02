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

class JobDetailActivity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail)
        session = SessionManager(this)

        val jobId      = intent.getIntExtra("job_id", -1)
        val jobTitle   = intent.getStringExtra("job_title")   ?: ""
        val company    = intent.getStringExtra("company_name") ?: ""
        val industry   = intent.getStringExtra("industry")    ?: ""
        val pkg        = intent.getDoubleExtra("package_lpa", 0.0)
        val location   = intent.getStringExtra("location")    ?: ""
        val jobType    = intent.getStringExtra("job_type")    ?: ""
        val deadline   = intent.getStringExtra("deadline")    ?: ""
        val desc       = intent.getStringExtra("description") ?: ""
        val minCgpa    = intent.getDoubleExtra("min_cgpa", 6.0)
        val minTenth   = intent.getDoubleExtra("min_tenth", 60.0)
        val minTwelfth = intent.getDoubleExtra("min_twelfth", 60.0)
        val companyId  = intent.getIntExtra("company_id", -1)
        val reqSkills  = intent.getStringExtra("required_skills") ?: ""

        if (jobId == -1) { finish(); return }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvJobTitle).text    = jobTitle
        findViewById<TextView>(R.id.tvCompanyName).text = company
        findViewById<TextView>(R.id.tvIndustry).text    = industry
        findViewById<TextView>(R.id.tvPackage).text     = "₹ $pkg LPA"
        findViewById<TextView>(R.id.tvLocation).text    = "📍 $location"
        findViewById<TextView>(R.id.tvJobType).text     = jobType
        findViewById<TextView>(R.id.tvDeadline).text    = "Apply by: $deadline"
        findViewById<TextView>(R.id.tvDescription).text = desc
        findViewById<TextView>(R.id.tvReqCgpa).text     = "Min CGPA: $minCgpa"
        findViewById<TextView>(R.id.tvReqTenth).text    = "Min 10th: $minTenth%"
        findViewById<TextView>(R.id.tvReqTwelfth).text  = "Min 12th: $minTwelfth%"

        // Show required skills if present
        val tvSkills = findViewById<TextView>(R.id.tvReqSkills)
        if (reqSkills.isNotEmpty()) {
            tvSkills.text = "Required Skills: $reqSkills"
            tvSkills.visibility = View.VISIBLE
        }

        val btnApply   = findViewById<MaterialButton>(R.id.btnApply)
        val tvApplied  = findViewById<TextView>(R.id.tvAlreadyApplied)
        val tvEligNote = findViewById<TextView>(R.id.tvEligibilityNote)

        // Block if placed
        if (session.isPlaced()) {
            btnApply.isEnabled = false
            tvEligNote.text = "🎉 You are already placed. Applications locked."
            tvEligNote.visibility = View.VISIBLE
        }

        btnApply.setOnClickListener {
            btnApply.isEnabled = false

            val req = object : StringRequest(
                Request.Method.POST,
                ApiClient.BASE_URL + "apply_job.php",


                { response ->
                    btnApply.isEnabled = true

                    try {
                        val json = org.json.JSONObject(response)
                        val status = json.getString("status")
                        val message = json.getString("message")

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                        when (status) {
                            "success" -> {
                                btnApply.visibility = View.GONE
                                tvApplied.visibility = View.VISIBLE
                            }

                            "error" -> {
                                if (message == "Already applied") {
                                    btnApply.visibility = View.GONE
                                    tvApplied.visibility = View.VISIBLE
                                } else if (message == "Not eligible") {
                                    tvEligNote.text = "You do not meet eligibility"
                                    tvEligNote.visibility = View.VISIBLE
                                    btnApply.isEnabled = false
                                }
                            }
                        }

                    } catch (e: Exception) {
                        // 🔥 Shows RAW response if JSON fails
                        Toast.makeText(this, "Parse error:\n$response", Toast.LENGTH_LONG).show()
                    }
                },

                //  ERROR RESPONSE
                { error ->
                    btnApply.isEnabled = true

                    val msg = when {
                        error.networkResponse != null -> {
                            val data = String(error.networkResponse.data)
                            "Server error:\n$data"
                        }
                        error.message != null -> {
                            "Network error: ${error.message}"
                        }
                        else -> "Unknown error"
                    }

                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }

            ) {
                override fun getParams(): Map<String, String> {
                    return hashMapOf(
                        "student_id" to session.getStudentId().toString(),
                        "job_id" to jobId.toString()
                    )
                }
            }

            Volley.newRequestQueue(this).add(req)
        }
    }
}
