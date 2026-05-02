package com.example.studentplacement.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.studentplacement.R
import com.example.studentplacement.utils.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo    = findViewById<ImageView>(R.id.splashLogo)
        val appName = findViewById<TextView>(R.id.splashAppName)
        val tagline = findViewById<TextView>(R.id.splashTagline)

        val fadeIn  = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)

        logo.startAnimation(fadeIn)
        appName.startAnimation(slideUp)
        tagline.startAnimation(slideUp)

        Handler(Looper.getMainLooper()).postDelayed({
            val session = SessionManager(this)
            val target  = if (session.isLoggedIn()) DashboardActivity::class.java
                          else LoginActivity::class.java
            startActivity(Intent(this, target))
            finish()
        }, 2000)
    }
}
