package com.example.project2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.project2.activities.LoginActivity
import com.example.project2.activities.admin.AdminActivity
import com.example.project2.activities.student.StudentHomeActivity
import com.example.project2.utils.NotificationHelper
import com.example.project2.utils.SessionManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createChannel(this)

        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            val intent = if (sessionManager.getRole() == "ADMIN") {
                Intent(this, AdminActivity::class.java)
            } else {
                Intent(this, StudentHomeActivity::class.java)
            }
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
