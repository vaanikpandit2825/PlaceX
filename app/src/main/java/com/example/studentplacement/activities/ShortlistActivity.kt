package com.example.studentplacement.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.studentplacement.R
import com.example.studentplacement.utils.ApiClient
import com.example.studentplacement.utils.SessionManager
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class ShortlistActivity : AppCompatActivity() {

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shortlist)

        session = SessionManager(this)

        val jobId    = intent.getIntExtra("job_id", -1)
        val jobTitle = intent.getStringExtra("job_title") ?: ""

        val tvTitle  = findViewById<TextView>(R.id.tvShortlistJobTitle)
        val tvSub    = findViewById<TextView>(R.id.tvShortlistSubtitle)
        val llList   = findViewById<LinearLayout>(R.id.llShortlistCandidates)
        val progress = findViewById<ProgressBar>(R.id.progressShortlist)

        tvTitle.text = "Top Candidates"
        tvSub.text   = "for $jobTitle"

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        progress.visibility = View.VISIBLE

        val req = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "shortlist_students.php",

            { response ->
                progress.visibility = View.GONE

                try {
                    val arr = JSONArray(response)
                    llList.removeAllViews()

                    if (arr.length() == 0) {
                        val tv = TextView(this)
                        tv.text = "No applicants yet for this job."
                        tv.setTextColor(Color.WHITE)
                        llList.addView(tv)
                    }

                    for (i in 0 until arr.length()) {

                        val c = arr.getJSONObject(i)
                        val card = layoutInflater.inflate(R.layout.item_shortlist_candidate, llList, false)

                        val appId = c.getInt("application_id")
                        val status = c.getString("status")

                        val tvStatus = card.findViewById<TextView>(R.id.tvCandidateStatus)

                        val btnShortlist = card.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnShortlistCandidate)
                        val btnInterview = card.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnInterviewCandidate)
                        val btnSelect    = card.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSelectCandidate)
                        val btnReject    = card.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRejectCandidate)

                        card.findViewById<TextView>(R.id.tvCandidateName).text = c.getString("name")
                        card.findViewById<TextView>(R.id.tvCandidateCgpa).text = "CGPA: ${c.getString("cgpa")}"

                        tvStatus.text = status

                        tvStatus.setTextColor(
                            when(status) {
                                "Shortlisted" -> Color.parseColor("#2196F3")
                                "Selected" -> Color.parseColor("#4CAF50")
                                "Interview Scheduled" -> Color.parseColor("#FF9800")
                                "Rejected" -> Color.parseColor("#F44336")
                                else -> Color.WHITE
                            }
                        )

                        btnShortlist.isEnabled = status == "Applied"

                        btnShortlist.setOnClickListener {
                            updateStatus(appId, "Shortlisted", tvStatus)
                        }

                        btnInterview.setOnClickListener {
                            showInterviewDialog(appId, tvStatus)
                        }

                        btnSelect.setOnClickListener {
                            updateStatus(appId, "Selected", tvStatus)
                        }

                        btnReject.setOnClickListener {
                            updateStatus(appId, "Rejected", tvStatus)
                        }

                        if (status == "Rejected") {
                            btnShortlist.isEnabled = false
                            btnInterview.isEnabled = false
                            btnSelect.isEnabled = false
                            btnReject.isEnabled = false
                        }

                        llList.addView(card)
                    }

                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },

            {
                progress.visibility = View.GONE
                Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
            }

        ) {
            override fun getParams() = mapOf(
                "job_id" to jobId.toString(),
                "filter" to "shortlisted"
            )
        }

        Volley.newRequestQueue(this).add(req)
    }

    private fun updateStatus(appId: Int, status: String, tvStatus: TextView) {

        val req = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "shortlist_students.php",

            {
                tvStatus.text = status
                Toast.makeText(this, "Updated to $status", Toast.LENGTH_SHORT).show()
            },

            {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }

        ) {
            override fun getParams() = mapOf(
                "action" to "update_status",
                "application_id" to appId.toString(),
                "status" to status
            )
        }

        Volley.newRequestQueue(this).add(req)
    }

    private fun showInterviewDialog(appId: Int, tvStatus: TextView) {

        val dialogView = layoutInflater.inflate(R.layout.dialog_interview, null)

        val etDate = dialogView.findViewById<EditText>(R.id.etDate)
        val etTime = dialogView.findViewById<EditText>(R.id.etTime)
        val etMode = dialogView.findViewById<EditText>(R.id.etMode)

        etDate.setOnClickListener {
            val cal = Calendar.getInstance()

            val picker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                    etDate.setText(date)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )

            picker.datePicker.minDate = System.currentTimeMillis()
            picker.show()
        }

        etTime.setOnClickListener {

            if (etDate.text.toString().isEmpty()) {
                Toast.makeText(this, "Select date first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cal = Calendar.getInstance()
            val currentHour = cal.get(Calendar.HOUR_OF_DAY)
            val currentMinute = cal.get(Calendar.MINUTE)

            val picker = TimePickerDialog(
                this,
                { _, hour, minute ->

                    val selectedDate = etDate.text.toString()
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    if (selectedDate == today) {
                        if (hour < currentHour || (hour == currentHour && minute < currentMinute)) {
                            Toast.makeText(this, "Cannot select past time", Toast.LENGTH_SHORT).show()
                            return@TimePickerDialog
                        }
                    }

                    val time = String.format("%02d:%02d", hour, minute)
                    etTime.setText(time)

                },
                currentHour,
                currentMinute,
                true
            )

            picker.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Schedule Interview")
            .setView(dialogView)
            .setPositiveButton("Schedule") { _, _ ->

                val date = etDate.text.toString()
                val time = etTime.text.toString()
                val mode = etMode.text.toString()

                updateInterview(appId, date, time, mode, tvStatus)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateInterview(
        appId: Int,
        date: String,
        time: String,
        mode: String,
        tvStatus: TextView
    ) {

        val req = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "shortlist_students.php",

            {
                tvStatus.text = "Interview Scheduled"
                Toast.makeText(this, "Interview Scheduled", Toast.LENGTH_SHORT).show()
            },

            {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }

        ) {
            override fun getParams() = mapOf(
                "action" to "update_status",
                "application_id" to appId.toString(),
                "status" to "Interview Scheduled",
                "interview_date" to date,
                "interview_time" to time,
                "mode" to mode
            )
        }

        Volley.newRequestQueue(this).add(req)
    }
}