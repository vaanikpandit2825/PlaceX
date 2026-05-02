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

class NotificationsActivity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        session = SessionManager(this)

        val llNotifs = findViewById<LinearLayout>(R.id.llNotifications)
        val progress = findViewById<ProgressBar>(R.id.progressNotif)
        val tvEmpty  = findViewById<TextView>(R.id.tvNoNotifs)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Mark all as read
        val markReq = object : StringRequest(Request.Method.POST, ApiClient.BASE_URL + "mark_notification_read.php",
            {}, {}) {
            override fun getParams() = mapOf("student_id" to session.getStudentId().toString())
        }
        Volley.newRequestQueue(this).add(markReq)

        progress.visibility = View.VISIBLE
        val req = object : StringRequest(Request.Method.POST, ApiClient.BASE_URL + "get_notifications.php",
            { response ->
                progress.visibility = View.GONE
                try {
                    val obj    = JSONObject(response)
                    val notifs = obj.getJSONArray("notifications")
                    llNotifs.removeAllViews()
                    if (notifs.length() == 0) { tvEmpty.visibility = View.VISIBLE;  }
                    tvEmpty.visibility = View.GONE
                    for (i in 0 until notifs.length()) {
                        val n = notifs.getJSONObject(i)
                        val card = layoutInflater.inflate(R.layout.item_notification, llNotifs, false)
                        card.findViewById<TextView>(R.id.tvNotifMessage).text = n.getString("message")
                        card.findViewById<TextView>(R.id.tvNotifTime).text    = n.getString("created_at").take(16)
                        if (n.getInt("is_read") == 0) {
                            card.setBackgroundColor(Color.parseColor("#1E3A5F"))
                        }
                        llNotifs.addView(card)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }, { progress.visibility = View.GONE }) {
            override fun getParams() = mapOf("student_id" to session.getStudentId().toString())
        }
        Volley.newRequestQueue(this).add(req)
    }
}
