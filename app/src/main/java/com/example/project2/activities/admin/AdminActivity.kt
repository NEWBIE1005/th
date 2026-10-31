package com.example.project2.activities.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.project2.activities.LoginActivity
import com.example.project2.databinding.ActivityAdminBinding
import com.example.project2.utils.SessionManager

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)
        binding.tvWelcome.text = "Xin chào, ${sessionManager.getUsername()}"

        binding.btnManageStudent.setOnClickListener {
            startActivity(Intent(this, StudentListActivity::class.java))
        }

        binding.btnManageSubject.setOnClickListener {
            startActivity(Intent(this, SubjectListActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
