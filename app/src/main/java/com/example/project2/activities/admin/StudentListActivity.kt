package com.example.project2.activities.admin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project2.adapters.StudentAdapter
import com.example.project2.dal.AppDB
import com.example.project2.databinding.ActivityStudentListBinding
import com.example.project2.entities.Student
import kotlinx.coroutines.launch

class StudentListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentListBinding
    private lateinit var adapter: StudentAdapter
    private lateinit var db: AppDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDB.getInstance(this)

        setupRecyclerView()

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, StudentFormActivity::class.java))
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadStudents(s?.toString()?.trim() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val query = binding.edtSearch.text.toString().trim()
        loadStudents(query)
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(
            onItemClick = { student ->
                val intent = Intent(this, StudentFormActivity::class.java).apply {
                    putExtra("STUDENT_ID", student.id)
                }
                startActivity(intent)
            },
            onItemLongClick = { student ->
                confirmDeleteStudent(student)
            }
        )
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter
    }

    private fun loadStudents(query: String = "") {
        lifecycleScope.launch {
            val students = if (query.isEmpty()) {
                db.dao().getAllStudents()
            } else {
                db.dao().searchStudents(query)
            }
            adapter.updateData(students)
        }
    }

    private fun confirmDeleteStudent(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Xóa sinh viên")
            .setMessage("Bạn có chắc chắn muốn xóa sinh viên ${student.fullname}?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch {
                    try {
                        db.dao().deleteStudentFull(student)
                        Toast.makeText(this@StudentListActivity, "Đã xóa sinh viên", Toast.LENGTH_SHORT).show()
                        val currentQuery = binding.edtSearch.text.toString().trim()
                        loadStudents(currentQuery)
                    } catch (e: Exception) {
                        Toast.makeText(this@StudentListActivity, "Lỗi khi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
