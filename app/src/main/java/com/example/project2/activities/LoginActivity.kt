package com.example.project2.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project2.activities.admin.AdminActivity
import com.example.project2.activities.student.StudentHomeActivity
import com.example.project2.dal.AppDB
import com.example.project2.databinding.ActivityLoginBinding
import com.example.project2.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDB.getInstance(this)
        val sessionManager = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsername.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnLogin.isEnabled = false

            lifecycleScope.launch {
                try {
                    val account = db.dao().getAccountByUsername(username)
                    if (account != null && account.password == password) {
                        sessionManager.saveSession(account)
                        val intent = if (account.role == "ADMIN") {
                            Intent(this@LoginActivity, AdminActivity::class.java)
                        } else {
                            Intent(this@LoginActivity, StudentHomeActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                        binding.btnLogin.isEnabled = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }
}
