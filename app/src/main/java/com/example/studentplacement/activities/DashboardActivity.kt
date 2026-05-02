package com.example.studentplacement.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.studentplacement.R
import com.example.studentplacement.adapter.ApplicationAdapter
import com.example.studentplacement.model.ApplicationDetail
import com.example.studentplacement.utils.ApiClient
import com.example.studentplacement.utils.SessionManager
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class DashboardActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var adapter: ApplicationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        session = SessionManager(this)

        val tvWelcome      = findViewById<TextView>(R.id.tvWelcome)
        val tvBranch       = findViewById<TextView>(R.id.tvBranch)
        val tvCgpa         = findViewById<TextView>(R.id.tvCgpa)
        val tvCollege      = findViewById<TextView>(R.id.tvCollege)
        val tvApplied      = findViewById<TextView>(R.id.tvStatApplied)
        val tvSelected     = findViewById<TextView>(R.id.tvStatSelected)
        val tvIntView      = findViewById<TextView>(R.id.tvStatInterview)
        val tvMyCgpa       = findViewById<TextView>(R.id.tvStatCgpa)
        val tvNoApps       = findViewById<TextView>(R.id.tvNoApps)
        val tvPlacedBanner = findViewById<TextView>(R.id.tvPlacedBanner)
        val tvSkillTip     = findViewById<TextView>(R.id.tvSkillTip)
        val tvNotifBadge   = findViewById<TextView>(R.id.tvNotifBadge)
        val rv             = findViewById<RecyclerView>(R.id.rvApplications)

        tvWelcome.text = "Hello, ${session.getStudentName().split(" ").first()} 👋"

        if (session.isPlaced()) {
            tvPlacedBanner.visibility = View.VISIBLE
        }

        rv.layoutManager = LinearLayoutManager(this)
        adapter = ApplicationAdapter(emptyList()) { appId ->
            startActivity(Intent(this, ApplicationHistoryActivity::class.java)
                .putExtra("application_id", appId))
        }
        rv.adapter = adapter

        // Load student info
        loadStudentData(tvBranch, tvCgpa, tvCollege, tvMyCgpa)

        // Load applications
        loadApplications(tvApplied, tvSelected, tvIntView, tvNoApps, tvSkillTip)

        // Load notifications
        loadNotifications(tvNotifBadge)

        // Navigation
        findViewById<MaterialButton>(R.id.btnBrowseJobs).setOnClickListener {
            startActivity(Intent(this, JobListActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, ProfileStep1Activity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnResume).setOnClickListener {
            startActivity(Intent(this, ResumeActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnAnalytics).setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // Logout
        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }


    override fun onResume() {
        super.onResume()

        val tvApplied  = findViewById<TextView>(R.id.tvStatApplied)
        val tvSelected = findViewById<TextView>(R.id.tvStatSelected)
        val tvIntView  = findViewById<TextView>(R.id.tvStatInterview)
        val tvNoApps   = findViewById<TextView>(R.id.tvNoApps)
        val tvSkillTip = findViewById<TextView>(R.id.tvSkillTip)

        loadApplications(tvApplied, tvSelected, tvIntView, tvNoApps, tvSkillTip)
    }

    private fun loadStudentData(
        tvBranch: TextView,
        tvCgpa: TextView,
        tvCollege: TextView,
        tvMyCgpa: TextView
    ) {
        val req = object : StringRequest(
            Method.POST,
            ApiClient.BASE_URL + "get_student.php",

            { response ->
                if (response.trim().startsWith("{")) {
                    val obj = JSONObject(response)
                    tvBranch.text  = obj.optString("branch")
                    tvCgpa.text    = "CGPA: ${obj.optString("cgpa")}"
                    tvCollege.text = obj.optString("college").ifEmpty { "College not set" }
                    tvMyCgpa.text  = obj.optString("cgpa")
                }
            },
            {}
        ) {
            override fun getParams() = mapOf(
                "student_id" to session.getStudentId().toString()
            )
        }

        Volley.newRequestQueue(this).add(req)
    }

    private fun loadApplications(
        tvApplied: TextView,
        tvSelected: TextView,
        tvIntView: TextView,
        tvNoApps: TextView,
        tvSkillTip: TextView
    ) {
        val req = object : StringRequest(
            Method.POST,
            ApiClient.BASE_URL + "get_applications.php",

            { response ->
                try {
                    val obj = JSONObject(response)
                    val arr = obj.getJSONArray("applications")
                    val suggestSkills = obj.optBoolean("suggest_skills", false)

                    if (suggestSkills) {
                        tvSkillTip.visibility = View.VISIBLE
                        tvSkillTip.text = "Improve skills in Java, Python, SQL"
                    } else {
                        tvSkillTip.visibility = View.GONE
                    }

                    val apps = mutableListOf<ApplicationDetail>()

                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        apps.add(
                            ApplicationDetail(
                                applicationId = o.getInt("application_id"),
                                status        = o.getString("status"),
                                appliedAt     = o.getString("applied_at"),
                                updatedAt     = o.optString("updated_at"),
                                jobTitle      = o.getString("title"),
                                packageLpa    = o.getDouble("package_lpa"),
                                location      = o.getString("location"),
                                jobType       = o.getString("job_type"),
                                companyName   = o.getString("company_name"),
                                industry      = o.getString("industry"),
                                companyId     = o.optInt("company_id"),
                                jobId         = o.optInt("job_id")
                            )
                        )
                    }

                    adapter.updateList(apps)

                    tvApplied.text  = apps.size.toString()
                    tvSelected.text = apps.count {
                        it.status == "Selected" || it.status == "Offer Accepted"
                    }.toString()
                    tvIntView.text  = apps.count { it.status.contains("Interview") }.toString()

                    tvNoApps.visibility = if (apps.isEmpty()) View.VISIBLE else View.GONE

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }

        ) {
            override fun getParams() = mapOf(
                "student_id" to session.getStudentId().toString()
            )
        }

        Volley.newRequestQueue(this).add(req)
    }

    private fun loadNotifications(tvNotifBadge: TextView) {
        val req = object : StringRequest(
            Method.POST,
            ApiClient.BASE_URL + "get_notifications.php",

            { response ->
                try {
                    val obj = JSONObject(response)
                    val unread = obj.optInt("unread_count", 0)

                    if (unread > 0) {
                        tvNotifBadge.visibility = View.VISIBLE
                        tvNotifBadge.text = if (unread > 9) "9+" else unread.toString()
                    } else {
                        tvNotifBadge.visibility = View.GONE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            {}
        ) {
            override fun getParams() = mapOf(
                "student_id" to session.getStudentId().toString()
            )
        }

        Volley.newRequestQueue(this).add(req)
    }
}