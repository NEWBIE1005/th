package com.example.project2.activities.student

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.project2.activities.LoginActivity
import com.example.project2.dal.AppDB
import com.example.project2.databinding.ActivityStudentHomeBinding
import com.example.project2.utils.SessionManager
import com.example.project2.utils.TimeUtils
import kotlinx.coroutines.launch

class StudentHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentHomeBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        binding.tvToday.text = "Hôm nay: ${TimeUtils.displayDate(TimeUtils.today())}"

        binding.btnBook.setOnClickListener {
            startActivity(Intent(this, ScheduleFormActivity::class.java))
        }

        binding.btnMySchedules.setOnClickListener {
            startActivity(Intent(this, ScheduleListActivity::class.java))
        }

        binding.btnCalendar.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadStudentInfoAndNextSchedule()
    }

    private fun loadStudentInfoAndNextSchedule() {
        val sessionManager = SessionManager(this)
        val accountId = sessionManager.getAccountId()
        val db = AppDB.getInstance(this)

        lifecycleScope.launch {
            val student = db.dao().getStudentByAccountId(accountId)
            if (student != null) {
                binding.tvWelcome.text = "Xin chào, ${student.fullname}"
                val schedules = db.dao().getScheduleViewsByStudent(student.id)
                val todayStr = TimeUtils.today()
                val nowTimeStr = TimeUtils.nowTime()

                val nextSchedule = schedules.firstOrNull { view ->
                    view.schedule.status != "CANCELLED" &&
                            (view.schedule.studyDate > todayStr ||
                                    (view.schedule.studyDate == todayStr && view.schedule.endTime >= nowTimeStr))
                }

                if (nextSchedule != null) {
                    val dateDisp = TimeUtils.displayDate(nextSchedule.schedule.studyDate)
                    binding.tvNext.text = "${nextSchedule.subjectName}\n$dateDisp (${nextSchedule.schedule.startTime} - ${nextSchedule.schedule.endTime})\nPhòng: ${nextSchedule.schedule.location}"
                } else {
                    binding.tvNext.text = "Không có buổi học sắp tới"
                }
            } else {
                binding.tvWelcome.text = "Xin chào, ${sessionManager.getUsername()}"
                binding.tvNext.text = "Không có buổi học sắp tới"
            }
        }
    }
}
