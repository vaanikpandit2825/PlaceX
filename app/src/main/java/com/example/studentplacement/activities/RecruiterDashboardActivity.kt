package com.example.studentplacement.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

class RecruiterDashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_dashboard)

        session = SessionManager(this)

        findViewById<TextView>(R.id.tvRecruiterWelcome).text =
            "Hello, ${session.getStudentName()} 🏢"

        findViewById<TextView>(R.id.tvRecruiterCompany).text =
            session.getCompanyName()

        findViewById<MaterialButton>(R.id.btnPostJob).setOnClickListener {
            startActivity(Intent(this, PostJobActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }

    private fun loadDashboard() {

        val tvStatus      = findViewById<TextView>(R.id.tvApprovalStatus)
        val tvTotalApps   = findViewById<TextView>(R.id.tvRecTotalApps)
        val tvShortlisted = findViewById<TextView>(R.id.tvRecShortlisted)
        val tvSelected    = findViewById<TextView>(R.id.tvRecSelected)
        val llJobs        = findViewById<LinearLayout>(R.id.llRecruiterJobs)
        val cardActions   = findViewById<LinearLayout>(R.id.cardRecruiterActions)

        val request = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "recruiter_dashboard.php",

            { response ->
                Log.d("API_RESPONSE", response)

                try {
                    val obj = JSONObject(response)

                    val approved = obj.getBoolean("approved")

                    if (!approved) {
                        tvStatus.text = "Pending Approval"
                        tvStatus.setTextColor(Color.parseColor("#FF9800"))
                        cardActions.visibility = LinearLayout.GONE

                    }

                    tvStatus.text = "Company Approved"
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"))

                    tvTotalApps.text   = obj.getString("total_apps")
                    tvShortlisted.text = obj.getString("shortlisted")
                    tvSelected.text    = obj.getString("selected")

                    llJobs.removeAllViews()

                    val jobs = obj.getJSONArray("jobs")

                    if (jobs.length() == 0) {
                        llJobs.addView(makeText("No jobs posted yet."))
                    }

                    for (i in 0 until jobs.length()) {
                        val j = jobs.getJSONObject(i)
                        val jobId = j.getInt("job_id")

                        val card = layoutInflater.inflate(
                            R.layout.item_recruiter_job,
                            llJobs,
                            false
                        )

                        card.findViewById<TextView>(R.id.tvRecJobTitle).text =
                            j.getString("title")

                        card.findViewById<TextView>(R.id.tvRecJobPkg).text =
                            "₹${j.getString("package_lpa")} LPA • ${j.getString("job_type")}"

                        card.findViewById<TextView>(R.id.tvRecJobApplicants).text =
                            "${j.getString("applicant_count")} applicants"

                        card.findViewById<TextView>(R.id.tvRecJobDeadline).text =
                            "Deadline: ${j.getString("deadline")}"

                        card.findViewById<TextView>(R.id.tvDriveStatus).text =
                            "Drive: ${j.getString("drive_status")}"

                        val btnStartDrive = card.findViewById<MaterialButton>(R.id.btnStartDrive)
                        val btnSlots = card.findViewById<MaterialButton>(R.id.btnScheduleSlots)

                        btnStartDrive.setOnClickListener {
                            startDrive(jobId)
                        }

                        btnSlots.setOnClickListener {
                            showSlotDialog(jobId)
                        }

                        card.findViewById<MaterialButton>(R.id.btnViewShortlist)
                            .setOnClickListener {
                                startActivity(
                                    Intent(this, ShortlistActivity::class.java)
                                        .putExtra("job_id", jobId)
                                        .putExtra("job_title", j.getString("title"))
                                )
                            }

                        llJobs.addView(card)
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },

            {
                Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
            }

        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "recruiter_id" to session.getRecruiterId().toString(),
                    "company_id" to session.getCompanyId().toString()
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun startDrive(jobId: Int) {

        val req = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "start_drive.php",

            {
                Toast.makeText(this, "Drive Started", Toast.LENGTH_SHORT).show()
                loadDashboard()
            },

            {
                Toast.makeText(this, "Error starting drive", Toast.LENGTH_SHORT).show()
            }

        ) {
            override fun getParams() = mapOf(
                "job_id" to jobId.toString()
            )
        }

        Volley.newRequestQueue(this).add(req)
    }

    private fun showSlotDialog(jobId: Int) {

        val view = layoutInflater.inflate(R.layout.dialog_slots, null)

        val etDate = view.findViewById<EditText>(R.id.etDate)
        val etStart = view.findViewById<EditText>(R.id.etStartTime)
        val etEnd = view.findViewById<EditText>(R.id.etEndTime)
        val etDuration = view.findViewById<EditText>(R.id.etDuration)

        AlertDialog.Builder(this)
            .setTitle("Schedule Interviews")
            .setView(view)
            .setPositiveButton("Generate") { _, _ ->

                val duration = etDuration.text.toString().ifEmpty { "15" }

                generateSlots(
                    jobId,
                    etDate.text.toString(),
                    etStart.text.toString(),
                    etEnd.text.toString(),
                    duration
                )
            }
            .show()
    }

    private fun generateSlots(
        jobId: Int,
        date: String,
        start: String,
        end: String,
        duration: String
    ) {

        val req = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "generate_slots.php",

            {
                Toast.makeText(this, "Slots Generated", Toast.LENGTH_SHORT).show()
            },

            {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }

        ) {
            override fun getParams() = mapOf(
                "job_id" to jobId.toString(),
                "date" to date,
                "start" to start,
                "end" to end,
                "duration" to duration
            )
        }

        Volley.newRequestQueue(this).add(req)
    }

    private fun makeText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(0, 8, 0, 8)
        }
    }
}