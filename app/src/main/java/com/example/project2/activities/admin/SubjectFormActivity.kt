package com.example.project2.activities.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project2.dal.AppDB
import com.example.project2.databinding.ActivitySubjectFormBinding
import com.example.project2.entities.Subject
import kotlinx.coroutines.launch

class SubjectFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubjectFormBinding
    private lateinit var db: AppDB
    private var subjectId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDB.getInstance(this)
        subjectId = intent.getLongExtra("SUBJECT_ID", -1L)

        if (subjectId != -1L) {
            binding.tvTitle.text = "Sửa môn học"
            binding.edtSubjectCode.isEnabled = false
            loadSubjectData()
        } else {
            binding.tvTitle.text = "Thêm môn học"
        }

        binding.btnSave.setOnClickListener {
            saveSubject()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadSubjectData() {
        lifecycleScope.launch {
            val subject = db.dao().getSubjectById(subjectId)
            if (subject != null) {
                binding.edtSubjectCode.setText(subject.subjectCode)
                binding.edtSubjectName.setText(subject.subjectName)
                binding.edtDescription.setText(subject.description)
                binding.edtCredits.setText(subject.credits.toString())
            } else {
                Toast.makeText(this@SubjectFormActivity, "Không tìm thấy môn học", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun saveSubject() {
        val subjectCode = binding.edtSubjectCode.text.toString().trim()
        val subjectName = binding.edtSubjectName.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        val creditsStr = binding.edtCredits.text.toString().trim()

        var isValid = true

        if (subjectCode.isEmpty()) {
            binding.edtSubjectCode.error = "Không được bỏ trống"
            isValid = false
        }

        if (subjectName.isEmpty()) {
            binding.edtSubjectName.error = "Không được bỏ trống"
            isValid = false
        }

        val credits = creditsStr.toIntOrNull()
        if (creditsStr.isEmpty() || credits == null || credits < 1 || credits > 10) {
            binding.edtCredits.error = "Số tín chỉ phải là số nguyên từ 1 đến 10"
            isValid = false
        }

        if (!isValid) return

        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                if (subjectId == -1L) {
                    val existing = db.dao().getSubjectByCode(subjectCode)
                    if (existing != null) {
                        binding.edtSubjectCode.error = "Mã môn học đã tồn tại"
                        binding.btnSave.isEnabled = true
                        return@launch
                    }

                    db.dao().insertSubject(
                        Subject(
                            subjectCode = subjectCode,
                            subjectName = subjectName,
                            description = description,
                            credits = credits ?: 0
                        )
                    )

                    Toast.makeText(this@SubjectFormActivity, "Lưu thành công", Toast.LENGTH_SHORT).show()
                    finish()

                } else {
                    db.dao().updateSubject(
                        Subject(
                            id = subjectId,
                            subjectCode = subjectCode,
                            subjectName = subjectName,
                            description = description,
                            credits = credits ?: 0
                        )
                    )

                    Toast.makeText(this@SubjectFormActivity, "Lưu thành công", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SubjectFormActivity, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
            }
        }
    }
}
