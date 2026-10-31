package com.example.project2.activities.student

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project2.R
import com.example.project2.adapters.ScheduleAdapter
import com.example.project2.dal.AppDB
import com.example.project2.databinding.ActivityCalendarBinding
import com.example.project2.utils.SessionManager
import com.example.project2.utils.TimeUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var adapter: ScheduleAdapter
    private lateinit var db: AppDB
    private lateinit var sessionManager: SessionManager

    private var selectedDate: String = TimeUtils.today()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDB.getInstance(this)
        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupListeners()

        loadCalendarSchedules()
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(
            onItemClick = null,
            onItemLongClick = null
        )
        binding.rvSchedules.layoutManager = LinearLayoutManager(this)
        binding.rvSchedules.adapter = adapter
    }

    private fun setupListeners() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            loadCalendarSchedules()
        }

        binding.rgMode.setOnCheckedChangeListener { _, _ ->
            loadCalendarSchedules()
        }
    }

    private fun loadCalendarSchedules() {
        var fromDate = selectedDate
        var toDate = selectedDate

        when (binding.rgMode.checkedRadioButtonId) {
            R.id.rbWeek -> {
                val range = TimeUtils.weekRange(selectedDate)
                fromDate = range.first
                toDate = range.second
                binding.tvRange.text = "Tuần ${TimeUtils.displayDate(fromDate)} – ${TimeUtils.displayDate(toDate)}"
            }
            R.id.rbMonth -> {
                val range = TimeUtils.monthRange(selectedDate)
                fromDate = range.first
                toDate = range.second
                val parts = selectedDate.split("-")
                val monthYear = if (parts.size >= 2) "${parts[1]}/${parts[0]}" else selectedDate
                binding.tvRange.text = "Tháng $monthYear"
            }
            else -> { // rbDay
                fromDate = selectedDate
                toDate = selectedDate
                binding.tvRange.text = "Ngày ${TimeUtils.displayDate(selectedDate)}"
            }
        }

        lifecycleScope.launch {
            val student = db.dao().getStudentByAccountId(sessionManager.getAccountId())
            if (student != null) {
                val list = db.dao().getScheduleViewsInRange(student.id, fromDate, toDate)
                if (list.isEmpty()) {
                    binding.rvSchedules.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvSchedules.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                    adapter.updateData(list)
                }
            } else {
                binding.rvSchedules.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
            }
        }
    }
}
