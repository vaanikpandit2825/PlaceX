package com.example.studentplacement.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.studentplacement.R
import com.example.studentplacement.adapter.JobAdapter
import com.example.studentplacement.model.Job
import com.example.studentplacement.utils.ApiClient
import com.example.studentplacement.utils.SessionManager
import com.google.android.material.chip.Chip
import org.json.JSONObject

class JobListActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var adapter: JobAdapter
    private var allJobs = listOf<Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_list)

        session = SessionManager(this)

        val rv       = findViewById<RecyclerView>(R.id.rvJobs)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val tvCount  = findViewById<TextView>(R.id.tvJobCount)
        val tvEmpty  = findViewById<TextView>(R.id.tvNoJobs)
        val chipAll  = findViewById<Chip>(R.id.chipAll)
        val chipFT   = findViewById<Chip>(R.id.chipFullTime)
        val chipInt  = findViewById<Chip>(R.id.chipIntern)

        // (Optional) only if you added this in XML
        val tvPlaced = try { findViewById<TextView>(R.id.tvPlacedMsg) } catch (e: Exception) { null }

        // If user is already placed → block UI
        if (session.isPlaced()) {
            tvPlaced?.visibility = View.VISIBLE
            tvEmpty.visibility   = View.VISIBLE
            tvEmpty.text         = "You are placed! No more applications needed."
            tvCount.text         = "0 jobs available"
        } else {

            findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

            rv.layoutManager = LinearLayoutManager(this)

            adapter = JobAdapter(emptyList()) { job ->
                startActivity(Intent(this, JobDetailActivity::class.java).apply {
                    putExtra("job_id", job.id)
                    putExtra("company_id", job.companyId)
                    putExtra("job_title", job.title)
                    putExtra("company_name", job.companyName)
                    putExtra("industry", job.industry)
                    putExtra("package_lpa", job.packageLpa)
                    putExtra("location", job.location)
                    putExtra("job_type", job.jobType)
                    putExtra("deadline", job.deadline)
                    putExtra("description", job.description)
                    putExtra("min_cgpa", job.minCgpa)
                    putExtra("min_tenth", job.minTenth)
                    putExtra("min_twelfth", job.minTwelfth)
                    putExtra("required_skills", job.requiredSkills)
                })
            }

            rv.adapter = adapter

            fun filter(q: String, type: String) {
                val f = allJobs.filter { j ->
                    (q.isEmpty() || j.title.contains(q, true) ||
                            j.companyName.contains(q, true) ||
                            j.location.contains(q, true)) &&
                            (type == "All" || j.jobType == type)
                }

                adapter.updateList(f)
                tvCount.text = "${f.size} jobs found"
                tvEmpty.visibility = if (f.isEmpty()) View.VISIBLE else View.GONE
            }

            val req = object : StringRequest(
                Request.Method.POST,
                ApiClient.BASE_URL + "get_jobs.php",
                { response ->
                    try {
                        val obj = JSONObject(response)

                        if (obj.optBoolean("placed", false)) {
                            tvEmpty.text = "You are placed! No more applications needed."
                            tvEmpty.visibility = View.VISIBLE
                              // THIS is correct here
                        }

                        val arr = obj.getJSONArray("jobs")
                        val jobs = mutableListOf<Job>()

                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            jobs.add(
                                Job(
                                    id = o.getInt("job_id"),
                                    title = o.getString("title"),
                                    description = o.getString("description"),
                                    packageLpa = o.getDouble("package_lpa"),
                                    minCgpa = o.getDouble("min_cgpa"),
                                    minTenth = o.getDouble("min_tenth"),
                                    minTwelfth = o.getDouble("min_twelfth"),
                                    requiredSkills = o.optString("required_skills", ""),
                                    allowedBranch = o.optString("allowed_branch", ""),
                                    location = o.getString("location"),
                                    jobType = o.getString("job_type"),
                                    deadline = o.getString("deadline"),
                                    companyId = o.getInt("company_id"),
                                    companyName = o.getString("company_name"),
                                    industry = o.getString("industry")
                                )
                            )
                        }

                        allJobs = jobs
                        filter("", "All")

                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                {
                    Toast.makeText(this, "Network error — is XAMPP running?", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams() =
                    mapOf("student_id" to session.getStudentId().toString())
            }

            Volley.newRequestQueue(this).add(req)

            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val t = if (chipAll.isChecked) "All"
                    else if (chipFT.isChecked) "Full Time"
                    else "Internship"

                    filter(s.toString().trim(), t)
                }
            })

            chipAll.setOnCheckedChangeListener { _, _ ->
                filter(etSearch.text.toString().trim(), "All")
            }

            chipFT.setOnCheckedChangeListener { _, _ ->
                filter(etSearch.text.toString().trim(), "Full Time")
            }

            chipInt.setOnCheckedChangeListener { _, _ ->
                filter(etSearch.text.toString().trim(), "Internship")
            }
        }
    }
}