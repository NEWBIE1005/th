package com.example.project2.activities.admin

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project2.dal.AppDB
import com.example.project2.databinding.ActivityStudentFormBinding
import com.example.project2.entities.Account
import com.example.project2.entities.Student
import kotlinx.coroutines.launch

class StudentFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentFormBinding
    private lateinit var db: AppDB
    private var studentId: Long = -1L
    private var currentStudent: Student? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDB.getInstance(this)
        studentId = intent.getLongExtra("STUDENT_ID", -1L)

        if (studentId != -1L) {
            binding.tvTitle.text = "Sửa sinh viên"
            binding.edtStudentCode.isEnabled = false
            loadStudentData()
        } else {
            binding.tvTitle.text = "Thêm sinh viên"
        }

        binding.btnSave.setOnClickListener {
            saveStudent()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadStudentData() {
        lifecycleScope.launch {
            val student = db.dao().getStudentById(studentId)
            if (student != null) {
                currentStudent = student
                binding.edtStudentCode.setText(student.studentCode)
                binding.edtFullname.setText(student.fullname)
                binding.edtEmail.setText(student.email)
                binding.edtPhone.setText(student.phone)
            } else {
                Toast.makeText(this@StudentFormActivity, "Không tìm thấy sinh viên", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun saveStudent() {
        val studentCode = binding.edtStudentCode.text.toString().trim()
        val fullname = binding.edtFullname.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val phone = binding.edtPhone.text.toString().trim()

        var isValid = true

        if (studentCode.isEmpty()) {
            binding.edtStudentCode.error = "Không được bỏ trống"
            isValid = false
        }

        if (fullname.isEmpty()) {
            binding.edtFullname.error = "Không được bỏ trống"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.edtEmail.error = "Không được bỏ trống"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Email không hợp lệ"
            isValid = false
        }

        if (phone.isEmpty()) {
            binding.edtPhone.error = "Không được bỏ trống"
            isValid = false
        } else if (!phone.matches(Regex("^\\d{10,11}$"))) {
            binding.edtPhone.error = "Số điện thoại phải gồm 10-11 chữ số"
            isValid = false
        }

        if (!isValid) return

        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                if (studentId == -1L) {
                    // Add new student
                    val existingStudentByCode = db.dao().getStudentByCode(studentCode)
                    val existingAccountByUsername = db.dao().getAccountByUsername(studentCode)

                    if (existingStudentByCode != null || existingAccountByUsername != null) {
                        binding.edtStudentCode.error = "Mã sinh viên đã tồn tại"
                        binding.btnSave.isEnabled = true
                        return@launch
                    }

                    val existingStudentByEmail = db.dao().getStudentByEmail(email)
                    if (existingStudentByEmail != null) {
                        binding.edtEmail.error = "Email đã tồn tại"
                        binding.btnSave.isEnabled = true
                        return@launch
                    }

                    val accountId = db.dao().insertAccount(
                        Account(
                            username = studentCode,
                            password = "123",
                            role = "STUDENT"
                        )
                    )

                    db.dao().insertStudent(
                        Student(
                            studentCode = studentCode,
                            fullname = fullname,
                            email = email,
                            phone = phone,
                            accountId = accountId
                        )
                    )

                    Toast.makeText(this@StudentFormActivity, "Lưu thành công", Toast.LENGTH_SHORT).show()
                    finish()

                } else {
                    // Update existing student
                    val existingStudentByEmail = db.dao().getStudentByEmail(email)
                    if (existingStudentByEmail != null && existingStudentByEmail.id != studentId) {
                        binding.edtEmail.error = "Email đã tồn tại"
                        binding.btnSave.isEnabled = true
                        return@launch
                    }

                    val accountId = currentStudent?.accountId ?: 0L
                    db.dao().updateStudent(
                        Student(
                            id = studentId,
                            studentCode = studentCode,
                            fullname = fullname,
                            email = email,
                            phone = phone,
                            accountId = accountId
                        )
                    )

                    Toast.makeText(this@StudentFormActivity, "Lưu thành công", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@StudentFormActivity, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
            }
        }
    }
}
