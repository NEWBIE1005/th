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
import com.example.project2.adapters.SubjectAdapter
import com.example.project2.dal.AppDB
import com.example.project2.databinding.ActivitySubjectListBinding
import com.example.project2.entities.Subject
import kotlinx.coroutines.launch

class SubjectListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySubjectListBinding
    private lateinit var adapter: SubjectAdapter
    private lateinit var db: AppDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubjectListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDB.getInstance(this)

        setupRecyclerView()

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, SubjectFormActivity::class.java))
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadSubjects(s?.toString()?.trim() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val query = binding.edtSearch.text.toString().trim()
        loadSubjects(query)
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter(
            onItemClick = { subject ->
                val intent = Intent(this, SubjectFormActivity::class.java).apply {
                    putExtra("SUBJECT_ID", subject.id)
                }
                startActivity(intent)
            },
            onItemLongClick = { subject ->
                confirmDeleteSubject(subject)
            }
        )
        binding.rvSubjects.layoutManager = LinearLayoutManager(this)
        binding.rvSubjects.adapter = adapter
    }

    private fun loadSubjects(query: String = "") {
        lifecycleScope.launch {
            val subjects = if (query.isEmpty()) {
                db.dao().getAllSubjects()
            } else {
                db.dao().searchSubjects(query)
            }
            adapter.updateData(subjects)
        }
    }

    private fun confirmDeleteSubject(subject: Subject) {
        lifecycleScope.launch {
            val count = db.dao().countSchedulesBySubject(subject.id)
            if (count > 0) {
                Toast.makeText(this@SubjectListActivity, "Môn học đang có lịch học, không thể xoá", Toast.LENGTH_SHORT).show()
                return@launch
            }

            AlertDialog.Builder(this@SubjectListActivity)
                .setTitle("Xóa môn học")
                .setMessage("Bạn có chắc chắn muốn xóa môn học ${subject.subjectName}?")
                .setPositiveButton("Xóa") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            db.dao().deleteSubject(subject)
                            Toast.makeText(this@SubjectListActivity, "Đã xóa môn học", Toast.LENGTH_SHORT).show()
                            val currentQuery = binding.edtSearch.text.toString().trim()
                            loadSubjects(currentQuery)
                        } catch (e: Exception) {
                            Toast.makeText(this@SubjectListActivity, "Lỗi khi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }
}
