package com.example.studentplacement.activities

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.studentplacement.R
import com.example.studentplacement.utils.ApiClient
import com.example.studentplacement.utils.SessionManager
import org.json.JSONArray
import java.io.File
import java.io.FileWriter

class AdminExportActivity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_export)
        session = SessionManager(this)

        val tvPreview  = findViewById<TextView>(R.id.tvExportPreview)
        val btnExport  = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDownloadCsv)
        val progress   = findViewById<ProgressBar>(R.id.progressExport)
        var cachedData = JSONArray()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        progress.visibility = View.VISIBLE
        val req = object : StringRequest(Request.Method.POST, ApiClient.BASE_URL + "admin_export.php",
            { response ->
                progress.visibility = View.GONE
                try {
                    val arr = JSONArray(response)
                    cachedData = arr
                    val sb = StringBuilder()
                    sb.appendLine("Name | Branch | CGPA | Placed | Applications")
                    sb.appendLine("─".repeat(55))
                    for (i in 0 until minOf(arr.length(), 15)) {
                        val r = arr.getJSONObject(i)
                        sb.appendLine("${r.getString("name")} | ${r.getString("branch")} | ${r.getString("cgpa")} | ${r.getString("placed")} | ${r.getString("total_applications")}")
                    }
                    if (arr.length() > 15) sb.appendLine("... and ${arr.length() - 15} more students")
                    tvPreview.text = sb.toString()
                    btnExport.isEnabled = true
                } catch (e: Exception) {
                    tvPreview.text = "Error loading data: ${e.message}"
                }
            }, { progress.visibility = View.GONE }) {
            override fun getParams() = mapOf("admin_id" to session.getAdminId().toString())
        }
        Volley.newRequestQueue(this).add(req)

        btnExport.setOnClickListener {
            try {
                val sb = StringBuilder()
                sb.appendLine("Name,Email,Branch,College,CGPA,10th%,12th%,Placed,Total Applications,Companies Applied")
                for (i in 0 until cachedData.length()) {
                    val r = cachedData.getJSONObject(i)
                    sb.appendLine("\"${r.optString("name")}\",\"${r.optString("email")}\",\"${r.optString("branch")}\",\"${r.optString("college")}\",${r.optString("cgpa")},${r.optString("tenth_percent")},${r.optString("twelfth_percent")},${r.optString("placed")},${r.optString("total_applications")},\"${r.optString("companies_applied")}\"")
                }
                val file = File(getExternalFilesDir(null), "PlaceX_Report.csv")
                FileWriter(file).use { it.write(sb.toString()) }
                Toast.makeText(this, "Report saved to:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
