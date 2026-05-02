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
import org.json.JSONArray
import org.json.JSONObject

class ApplicationHistoryActivity : AppCompatActivity() {

    private val statusFlow = listOf(
        "Applied",
        "Shortlisted",
        "Interview Scheduled",
        "Interview Done",
        "Selected",
        "Offer Accepted"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_history)

        val appId    = intent.getIntExtra("application_id", -1)
        val llSteps  = findViewById<LinearLayout>(R.id.llHistorySteps)
        val progress = findViewById<ProgressBar>(R.id.progressHistory)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        progress.visibility = View.VISIBLE

        val req = object : StringRequest(
            Request.Method.POST,
            ApiClient.BASE_URL + "get_application_history.php",

            { response ->
                progress.visibility = View.GONE

                try {
                    val arr = JSONArray(response)
                    llSteps.removeAllViews()


                    val historyList = mutableListOf<JSONObject>()
                    for (i in 0 until arr.length()) {
                        historyList.add(arr.getJSONObject(i))
                    }

                    for (step in statusFlow) {

                        val match = historyList.lastOrNull {
                            it.getString("status") == step
                        }

                        val isDone = match != null

                        val timestamp = match
                            ?.optString("changed_at", "")
                            ?.take(16) ?: ""

                        // 🔥 NEW: Interview details
                        val interviewDetails = if (step == "Interview Scheduled" && isDone) {
                            val date = match.optString("interview_date", "")
                            val time = match.optString("interview_time", "")
                            val mode = match.optString("interview_mode", "")

                            if (date.isNotEmpty() || time.isNotEmpty()) {
                                "\n     $date  $time ($mode)"
                            } else ""
                        } else ""

                        val tv = TextView(this)

                        tv.text = if (isDone)
                            "$step\n    $timestamp$interviewDetails"
                        else
                            "$step"

                        tv.setTextColor(
                            if (isDone) Color.parseColor("#4CAF50")
                            else Color.parseColor("#80FFFFFF")
                        )

                        tv.textSize = if (isDone) 15f else 13f
                        tv.setPadding(0, 12, 0, 12)

                        llSteps.addView(tv)


                        if (step != statusFlow.last()) {
                            val div = View(this)
                            div.layoutParams = LinearLayout.LayoutParams(2, 32).apply {
                                setMargins(24, 0, 0, 0)
                            }
                            div.setBackgroundColor(
                                if (isDone) Color.parseColor("#4CAF50")
                                else Color.parseColor("#333F50")
                            )
                            llSteps.addView(div)
                        }
                    }


                    val rejected = historyList.firstOrNull {
                        it.getString("status") == "Rejected"
                    }

                    if (rejected != null) {
                        val ts = rejected.optString("changed_at", "").take(16)

                        val tv = TextView(this)
                        tv.text = "Rejected\n    $ts"
                        tv.setTextColor(Color.parseColor("#F44336"))
                        tv.textSize = 15f
                        tv.setPadding(0, 12, 0, 12)

                        llSteps.addView(tv)
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
                "application_id" to appId.toString()
            )
        }

        Volley.newRequestQueue(this).add(req)
    }
}