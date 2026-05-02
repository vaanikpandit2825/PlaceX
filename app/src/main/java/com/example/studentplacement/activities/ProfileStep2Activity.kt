package com.example.studentplacement.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.studentplacement.R
import com.example.studentplacement.utils.ApiClient
import com.example.studentplacement.utils.SessionManager
import com.example.studentplacement.utils.Validator

class ProfileStep2Activity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_step2)
        session = SessionManager(this)

        val etCollege = findViewById<EditText>(R.id.etCollege)
        val etTenth   = findViewById<EditText>(R.id.etTenth)
        val etTwelfth = findViewById<EditText>(R.id.etTwelfth)
        val etCgpa    = findViewById<EditText>(R.id.etCgpa)
        val btnSave   = findViewById<Button>(R.id.btnNext2)
        val btnBack   = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val college = etCollege.text.toString().trim()
            val tenth   = etTenth.text.toString().trim()
            val twelfth = etTwelfth.text.toString().trim()
            val cgpa    = etCgpa.text.toString().trim()

            when {
                college.isEmpty()                  -> etCollege.error = "Required"
                tenth.isEmpty()                    -> etTenth.error   = "Required"
                !Validator.isValidPercent(tenth)    -> etTenth.error   = "Enter 0–100"
                twelfth.isEmpty()                  -> etTwelfth.error = "Required"
                !Validator.isValidPercent(twelfth)  -> etTwelfth.error = "Enter 0–100"
                cgpa.isEmpty()                     -> etCgpa.error   = "Required"
                !Validator.isValidCgpa(cgpa)        -> etCgpa.error   = "Enter 0–10"
                else -> {
                    val request = object : StringRequest(Method.POST,
                        ApiClient.BASE_URL + "save_step2.php",
                        { response ->
                            if (response.trim() == "Success") {
                                Toast.makeText(this, "Profile complete!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, DashboardActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                finish()
                            } else {
                                Toast.makeText(this, response, Toast.LENGTH_LONG).show()
                            }
                        },
                        { error ->
                            Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_LONG).show()
                        }) {
                        override fun getParams() = mapOf(
                            "student_id" to session.getStudentId().toString(),
                            "college" to college, "tenth" to tenth,
                            "twelfth" to twelfth, "cgpa" to cgpa)
                    }
                    Volley.newRequestQueue(this).add(request)
                }
            }
        }
    }
}
