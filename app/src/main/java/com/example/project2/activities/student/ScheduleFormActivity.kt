package com.example.project2.activities.student

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project2.dal.AppDB
import com.example.project2.databinding.ActivityScheduleFormBinding
import com.example.project2.entities.StudySchedule
import com.example.project2.entities.Subject
import com.example.project2.utils.NotificationHelper
import com.example.project2.utils.ReminderScheduler
import com.example.project2.utils.SessionManager
import com.example.project2.utils.TimeUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ScheduleFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleFormBinding
    private lateinit var db: AppDB
    private lateinit var sessionManager: SessionManager

    private var scheduleId: Long = -1L
    private var currentSchedule: StudySchedule? = null

    private var subjectsList: List<Subject> = emptyList()
    private var selectedDate: String = ""
    private var selectedStartTime: String = ""
    private var selectedEndTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDB.getInstance(this)
        sessionManager = SessionManager(this)
        scheduleId = intent.getLongExtra("SCHEDULE_ID", -1L)

        setupPickers()
        loadSubjectsAndSchedule()

        binding.btnSave.setOnClickListener {
            saveSchedule()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupPickers() {
        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            if (selectedDate.isNotEmpty()) {
                val parts = selectedDate.split("-")
                if (parts.size == 3) {
                    cal.set(Calendar.YEAR, parts[0].toInt())
                    cal.set(Calendar.MONTH, parts[1].toInt() - 1)
                    cal.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                }
            }

            val dpd = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    binding.btnPickDate.text = "Ngày: ${TimeUtils.displayDate(selectedDate)}"
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }

        binding.btnPickStart.setOnClickListener {
            val cal = Calendar.getInstance()
            if (selectedStartTime.isNotEmpty()) {
                val parts = selectedStartTime.split(":")
                if (parts.size == 2) {
                    cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    cal.set(Calendar.MINUTE, parts[1].toInt())
                }
            }

            val tpd = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedStartTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                    binding.btnPickStart.text = "Bắt đầu: $selectedStartTime"
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            )
            tpd.show()
        }

        binding.btnPickEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            if (selectedEndTime.isNotEmpty()) {
                val parts = selectedEndTime.split(":")
                if (parts.size == 2) {
                    cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    cal.set(Calendar.MINUTE, parts[1].toInt())
                }
            }

            val tpd = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    selectedEndTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                    binding.btnPickEnd.text = "Kết thúc: $selectedEndTime"
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            )
            tpd.show()
        }
    }

    private fun loadSubjectsAndSchedule() {
        lifecycleScope.launch {
            subjectsList = db.dao().getAllSubjects()
            val subjectNames = subjectsList.map { "${it.subjectCode} - ${it.subjectName}" }
            val adapter = ArrayAdapter(
                this@ScheduleFormActivity,
                android.R.layout.simple_spinner_item,
                subjectNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spnSubject.adapter = adapter

            if (scheduleId != -1L) {
                binding.tvTitle.text = "Sửa lịch học"
                binding.btnSave.text = "Lưu"

                val schedule = db.dao().getScheduleById(scheduleId)
                if (schedule != null) {
                    currentSchedule = schedule
                    selectedDate = schedule.studyDate
                    selectedStartTime = schedule.startTime
                    selectedEndTime = schedule.endTime

                    binding.btnPickDate.text = "Ngày: ${TimeUtils.displayDate(selectedDate)}"
                    binding.btnPickStart.text = "Bắt đầu: $selectedStartTime"
                    binding.btnPickEnd.text = "Kết thúc: $selectedEndTime"
                    binding.edtLocation.setText(schedule.location)

                    val index = subjectsList.indexOfFirst { it.id == schedule.subjectId }
                    if (index >= 0) {
                        binding.spnSubject.setSelection(index)
                    }
                } else {
                    Toast.makeText(this@ScheduleFormActivity, "Không tìm thấy lịch học", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                binding.tvTitle.text = "Đặt lịch học"
                binding.btnSave.text = "Đặt lịch"
            }
        }
    }

    private fun saveSchedule() {
        if (subjectsList.isEmpty()) {
            Toast.makeText(this, "Chưa chọn môn học", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedSubjectPosition = binding.spnSubject.selectedItemPosition
        if (selectedSubjectPosition < 0 || selectedSubjectPosition >= subjectsList.size) {
            Toast.makeText(this, "Vui lòng chọn môn học", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedSubject = subjectsList[selectedSubjectPosition]

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày học", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedStartTime.isEmpty() || selectedEndTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn giờ bắt đầu và kết thúc", Toast.LENGTH_SHORT).show()
            return
        }

        if (TimeUtils.toMinutes(selectedEndTime) <= TimeUtils.toMinutes(selectedStartTime)) {
            Toast.makeText(this, "Giờ kết thúc phải lớn hơn giờ bắt đầu", Toast.LENGTH_SHORT).show()
            return
        }

        val location = binding.edtLocation.text.toString().trim()
        if (location.isEmpty()) {
            binding.edtLocation.error = "Không được bỏ trống"
            return
        }

        if (TimeUtils.isPast(selectedDate, selectedStartTime)) {
            Toast.makeText(this, "Thời gian đặt lịch không được ở thời điểm đã qua", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val student = db.dao().getStudentByAccountId(sessionManager.getAccountId())
                if (student == null) {
                    Toast.makeText(this@ScheduleFormActivity, "Không tìm thấy sinh viên", Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = true
                    return@launch
                }

                val overlapping = db.dao().getOverlapping(
                    studentId = student.id,
                    date = selectedDate,
                    start = selectedStartTime,
                    end = selectedEndTime,
                    excludeId = scheduleId
                )

                if (overlapping.isNotEmpty()) {
                    val overlapView = db.dao().getScheduleViewById(overlapping[0].id)
                    val overlapName = overlapView?.subjectName ?: "môn học khác"
                    val oStart = overlapping[0].startTime
                    val oEnd = overlapping[0].endTime

                    Toast.makeText(
                        this@ScheduleFormActivity,
                        "Trùng lịch với $overlapName $oStart–$oEnd",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.btnSave.isEnabled = true
                    return@launch
                }

                if (scheduleId == -1L) {
                    // New booking -> status PENDING
                    val newSchedule = StudySchedule(
                        studentId = student.id,
                        subjectId = selectedSubject.id,
                        studyDate = selectedDate,
                        startTime = selectedStartTime,
                        endTime = selectedEndTime,
                        location = location,
                        status = "PENDING"
                    )

                    val newId = db.dao().insertSchedule(newSchedule)

                    ReminderScheduler.schedule(
                        this@ScheduleFormActivity,
                        newId,
                        selectedDate,
                        selectedStartTime
                    )

                    val dateDisp = TimeUtils.displayDate(selectedDate)
                    NotificationHelper.show(
                        this@ScheduleFormActivity,
                        "Đặt lịch thành công",
                        "${selectedSubject.subjectName} vào $dateDisp lúc $selectedStartTime"
                    )

                    Toast.makeText(this@ScheduleFormActivity, "Đặt lịch thành công", Toast.LENGTH_SHORT).show()
                    finish()

                } else {
                    // Edit schedule -> keep current status
                    val currentStatus = currentSchedule?.status ?: "PENDING"
                    val updatedSchedule = StudySchedule(
                        id = scheduleId,
                        studentId = student.id,
                        subjectId = selectedSubject.id,
                        studyDate = selectedDate,
                        startTime = selectedStartTime,
                        endTime = selectedEndTime,
                        location = location,
                        status = currentStatus
                    )

                    db.dao().updateSchedule(updatedSchedule)

                    ReminderScheduler.schedule(
                        this@ScheduleFormActivity,
                        scheduleId,
                        selectedDate,
                        selectedStartTime
                    )

                    val dateDisp = TimeUtils.displayDate(selectedDate)
                    NotificationHelper.show(
                        this@ScheduleFormActivity,
                        "Lịch học đã thay đổi",
                        "${selectedSubject.subjectName} vào $dateDisp lúc $selectedStartTime"
                    )

                    Toast.makeText(this@ScheduleFormActivity, "Cập nhật lịch học thành công", Toast.LENGTH_SHORT).show()
                    finish()
                }

            } catch (e: Exception) {
                Toast.makeText(this@ScheduleFormActivity, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
            }
        }
    }
}
