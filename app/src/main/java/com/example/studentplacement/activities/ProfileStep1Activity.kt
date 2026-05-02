package com.example.studentplacement.activities

import android.app.DatePickerDialog
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
import java.util.Calendar

class ProfileStep1Activity : AppCompatActivity() {
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_step1)
        session = SessionManager(this)

        val etName    = findViewById<EditText>(R.id.etName)
        val etPhone   = findViewById<EditText>(R.id.etPhone)
        val etDob     = findViewById<EditText>(R.id.etDob)
        val etGender  = findViewById<EditText>(R.id.etGender)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val btnNext   = findViewById<Button>(R.id.btnNext)
        val btnBack   = findViewById<ImageButton>(R.id.btnBack)

        etDob.setOnClickListener { showDatePicker(etDob) }
        btnBack.setOnClickListener { finish() }

        btnNext.setOnClickListener {
            val name    = etName.text.toString().trim()
            val phone   = etPhone.text.toString().trim()
            val dob     = etDob.text.toString().trim()
            val gender  = etGender.text.toString().trim()
            val address = etAddress.text.toString().trim()

            when {
                name.isEmpty()                  -> etName.error   = "Required"
                phone.isEmpty()                 -> etPhone.error  = "Required"
                !Validator.isValidPhone(phone)   -> etPhone.error  = "Enter valid 10-digit phone"
                dob.isEmpty()                   -> etDob.error    = "Required"
                gender.isEmpty()                -> etGender.error = "Required"
                address.isEmpty()               -> etAddress.error = "Required"
                else -> {
                    val request = object : StringRequest(Method.POST,
                        ApiClient.BASE_URL + "save_step1.php",
                        { response ->
                            if (response.trim() == "Success") {
                                Toast.makeText(this, " Step 1 saved", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, ProfileStep2Activity::class.java))
                            } else {
                                Toast.makeText(this, response, Toast.LENGTH_LONG).show()
                            }
                        },
                        { error ->
                            Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_LONG).show()
                        }) {
                        override fun getParams() = mapOf(
                            "student_id" to session.getStudentId().toString(),
                            "name" to name, "phone" to phone, "dob" to dob,
                            "gender" to gender, "address" to address)
                    }
                    Volley.newRequestQueue(this).add(request)
                }
            }
        }
    }

    private fun showDatePicker(et: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            et.setText(String.format("%02d-%02d-%04d", d, m + 1, y))
        }, cal.get(Calendar.YEAR) - 20, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
}
