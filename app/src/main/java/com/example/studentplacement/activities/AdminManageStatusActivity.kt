package com.example.studentplacement.activities

import android.graphics.Color
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
import org.json.JSONObject

class AdminManageStatusActivity : AppCompatActivity() {
    private lateinit var session: SessionManager
    private val statusOptions = arrayOf("Applied","Shortlisted","Interview Scheduled","Interview Done","Selected","Rejected","Offer Accepted")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_manage_status)
        session = SessionManager(this)

        val llApps   = findViewById<LinearLayout>(R.id.llAllApplications)
        val progress = findViewById<ProgressBar>(R.id.progressManage)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        progress.visibility = View.VISIBLE
        val req = object : StringRequest(Request.Method.POST, ApiClient.BASE_URL + "admin_dashboard.php",
            { response ->
                progress.visibility = View.GONE
                try {
                    val obj  = JSONObject(response)
                    val apps = obj.getJSONArray("all_applications")
                    llApps.removeAllViews()
                    for (i in 0 until apps.length()) {
                        val a = apps.getJSONObject(i)
                        val card = layoutInflater.inflate(R.layout.item_manage_application, llApps, false)
                        card.findViewById<TextView>(R.id.tvManageStudent).text   = "${a.getString("student_name")} (${a.getString("branch")}, CGPA:${a.getString("cgpa")})"
                        card.findViewById<TextView>(R.id.tvManageJob).text       = "${a.getString("job_title")} @ ${a.getString("company_name")}"
                        val tvStatus = card.findViewById<TextView>(R.id.tvManageStatus)
                        tvStatus.text = a.getString("status")
                        tvStatus.setTextColor(when(a.getString("status")) {
                            "Selected" -> Color.parseColor("#4CAF50")
                            "Rejected" -> Color.parseColor("#F44336")
                            else       -> Color.parseColor("#FF9800")
                        })
                        val spinner = card.findViewById<Spinner>(R.id.spinnerStatus)
                        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
                        spinner.adapter = spinnerAdapter
                        spinner.setSelection(statusOptions.indexOf(a.getString("status")).coerceAtLeast(0))

                        card.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnUpdateStatus).setOnClickListener {
                            val newStatus = spinner.selectedItem.toString()
                            val appId     = a.getInt("application_id")
                            updateStatus(appId, newStatus, tvStatus)
                        }
                        llApps.addView(card)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }, { progress.visibility = View.GONE }) {
            override fun getParams() = mapOf("admin_id" to session.getAdminId().toString())
        }
        Volley.newRequestQueue(this).add(req)
    }

    private fun updateStatus(appId: Int, status: String, tvStatus: TextView) {
        val req = object : StringRequest(Request.Method.POST, ApiClient.BASE_URL + "admin_update_status.php",
            { response ->
                if (response.trim() == "Success") {
                    tvStatus.text = status
                    Toast.makeText(this, "Status updated to $status", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                }
            }, {}) {
            override fun getParams() = mapOf("application_id" to appId.toString(), "status" to status)
        }
        Volley.newRequestQueue(this).add(req)
    }
}
