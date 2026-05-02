package com.example.studentplacement.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.studentplacement.R
import com.example.studentplacement.utils.ApiClient
import com.example.studentplacement.utils.SessionManager
import org.json.JSONObject

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        session = SessionManager(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val req = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "analytics.php",
            { response ->
                try {
                    val obj = JSONObject(response)

                    // Platform stats
                    findViewById<TextView>(R.id.tvTotalStudents).text  = obj.getString("total_students")
                    findViewById<TextView>(R.id.tvTotalJobs).text      = obj.getString("total_jobs")
                    findViewById<TextView>(R.id.tvTotalApps).text      = obj.getString("total_apps")
                    findViewById<TextView>(R.id.tvTotalCompanies).text = obj.getString("total_companies")
                    findViewById<TextView>(R.id.tvPlaced).text         = obj.getString("placed")
                    findViewById<TextView>(R.id.tvAvgPackage).text     = "₹ ${obj.getString("avg_package")} LPA"
                    findViewById<TextView>(R.id.tvMaxPackage).text     = "₹ ${obj.getString("max_package")} LPA"
                    findViewById<TextView>(R.id.tvAboveAvg).text       = obj.getString("above_avg_cgpa")

                    // My stats
                    findViewById<TextView>(R.id.tvMyApplied).text   = obj.getString("my_applied")
                    findViewById<TextView>(R.id.tvMyPlaced).text    = obj.getString("my_placed")
                    findViewById<TextView>(R.id.tvMyInterview).text = obj.getString("my_interview")
                    findViewById<TextView>(R.id.tvMyRejected).text  = obj.getString("my_rejected")

                    // Application status breakdown (GROUP BY)
                    val llStatus = findViewById<LinearLayout>(R.id.llStatusBreakdown)
                    llStatus.removeAllViews()
                    val statusArr = obj.getJSONArray("status_breakdown")
                    for (i in 0 until statusArr.length()) {
                        val s = statusArr.getJSONObject(i)
                        llStatus.addView(makeRow("• ${s.getString("status")}: ${s.getString("cnt")} application(s)"))
                    }
                    if (statusArr.length() == 0)
                        llStatus.addView(makeRow("No applications yet"))

                    // Company stats (JOIN + GROUP BY)
                    val llCompany = findViewById<LinearLayout>(R.id.llCompanyStats)
                    llCompany.removeAllViews()
                    val compArr = obj.getJSONArray("company_stats")
                    for (i in 0 until compArr.length()) {
                        val c = compArr.getJSONObject(i)
                        llCompany.addView(makeRow(
                            "${i + 1}. ${c.getString("company_name")} — ${c.getString("job_count")} job(s) | Avg ₹${c.getString("avg_pkg")} LPA"
                        ))
                    }

                    // Branch breakdown (GROUP BY)
                    val llBranch = findViewById<LinearLayout>(R.id.llBranchBreakdown)
                    llBranch.removeAllViews()
                    val branchArr = obj.getJSONArray("branch_breakdown")
                    for (i in 0 until branchArr.length()) {
                        val b = branchArr.getJSONObject(i)
                        llBranch.addView(makeRow("• ${b.getString("branch")}: ${b.getString("cnt")} student(s)"))
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Parse error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this,
                    "Network error — is XAMPP running?\n${error.message}",
                    Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams() = mapOf("student_id" to session.getStudentId().toString())
        }

        Volley.newRequestQueue(this).add(req)
    }

    private fun makeRow(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(resources.getColor(android.R.color.white, theme))
        textSize = 13f
        setPadding(0, 6, 0, 6)
    }
}
