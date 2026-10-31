package com.example.project2.activities.student

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project2.adapters.ScheduleAdapter
import com.example.project2.dal.AppDB
import com.example.project2.databinding.ActivityScheduleListBinding
import com.example.project2.entities.ScheduleView
import com.example.project2.utils.NotificationHelper
import com.example.project2.utils.ReminderScheduler
import com.example.project2.utils.SessionManager
import com.example.project2.utils.TimeUtils
import kotlinx.coroutines.launch

class ScheduleListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleListBinding
    private lateinit var adapter: ScheduleAdapter
    private lateinit var db: AppDB
    private lateinit var sessionManager: SessionManager

    private val statusOptions = arrayOf("Tất cả", "PENDING", "CONFIRMED", "CANCELLED")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDB.getInstance(this)
        sessionManager = SessionManager(this)

        setupSpinner()
        setupRecyclerView()

        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, ScheduleFormActivity::class.java))
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadSchedules()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSpinner() {
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            statusOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spnStatus.adapter = spinnerAdapter

        binding.spnStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadSchedules()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(
            onItemClick = { item ->
                if (item.schedule.status == "CANCELLED") {
                    Toast.makeText(this, "Lịch học đã hủy không thể sửa", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this, ScheduleFormActivity::class.java).apply {
                        putExtra("SCHEDULE_ID", item.schedule.id)
                    }
                    startActivity(intent)
                }
            },
            onItemLongClick = { item ->
                showActionDialog(item)
            }
        )
        binding.rvSchedules.layoutManager = LinearLayoutManager(this)
        binding.rvSchedules.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadSchedules()
    }

    private fun loadSchedules() {
        val query = binding.edtSearch.text.toString().trim()
        val selectedStatusPos = binding.spnStatus.selectedItemPosition
        val statusFilter = if (selectedStatusPos > 0) statusOptions[selectedStatusPos] else ""

        lifecycleScope.launch {
            val student = db.dao().getStudentByAccountId(sessionManager.getAccountId())
            if (student != null) {
                val list = db.dao().searchScheduleViews(student.id, query, statusFilter)
                adapter.updateData(list)
            }
        }
    }

    private fun showActionDialog(item: ScheduleView) {
        val actions = mutableListOf<String>()
        val schedule = item.schedule

        when (schedule.status) {
            "PENDING" -> {
                actions.add("Xác nhận")
                actions.add("Hủy lịch")
                actions.add("Xóa")
            }
            "CONFIRMED" -> {
                actions.add("Hủy lịch")
                actions.add("Xóa")
            }
            "CANCELLED" -> {
                actions.add("Xóa")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Tùy chọn lịch học")
            .setItems(actions.toTypedArray()) { _, which ->
                when (actions[which]) {
                    "Xác nhận" -> confirmSchedule(item)
                    "Hủy lịch" -> cancelSchedule(item)
                    "Xóa" -> deleteSchedule(item)
                }
            }
            .show()
    }

    private fun confirmSchedule(item: ScheduleView) {
        lifecycleScope.launch {
            db.dao().updateStatus(item.schedule.id, "CONFIRMED")
            val dateDisp = TimeUtils.displayDate(item.schedule.studyDate)
            NotificationHelper.show(
                this@ScheduleListActivity,
                "Lịch học đã xác nhận",
                "${item.subjectName} lúc ${item.schedule.startTime} ngày $dateDisp"
            )
            Toast.makeText(this@ScheduleListActivity, "Đã xác nhận lịch học", Toast.LENGTH_SHORT).show()
            loadSchedules()
        }
    }

    private fun cancelSchedule(item: ScheduleView) {
        lifecycleScope.launch {
            db.dao().updateStatus(item.schedule.id, "CANCELLED")
            ReminderScheduler.cancel(this@ScheduleListActivity, item.schedule.id)
            val dateDisp = TimeUtils.displayDate(item.schedule.studyDate)
            NotificationHelper.show(
                this@ScheduleListActivity,
                "Lịch học đã huỷ",
                "${item.subjectName} lúc ${item.schedule.startTime} ngày $dateDisp"
            )
            Toast.makeText(this@ScheduleListActivity, "Đã hủy lịch học", Toast.LENGTH_SHORT).show()
            loadSchedules()
        }
    }

    private fun deleteSchedule(item: ScheduleView) {
        lifecycleScope.launch {
            db.dao().deleteScheduleById(item.schedule.id)
            ReminderScheduler.cancel(this@ScheduleListActivity, item.schedule.id)
            val dateDisp = TimeUtils.displayDate(item.schedule.studyDate)
            NotificationHelper.show(
                this@ScheduleListActivity,
                "Lịch học đã xoá",
                "${item.subjectName} lúc ${item.schedule.startTime} ngày $dateDisp"
            )
            Toast.makeText(this@ScheduleListActivity, "Đã xóa lịch học", Toast.LENGTH_SHORT).show()
            loadSchedules()
        }
    }
}
