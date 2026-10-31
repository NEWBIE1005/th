package com.example.project2.dal

import androidx.room.*
import com.example.project2.entities.*

@Dao
abstract class DAO {
    // --- Account ---
    @Insert
    abstract suspend fun insertAccount(account: Account): Long

    @Query("SELECT * FROM Account WHERE username = :username LIMIT 1")
    abstract suspend fun getAccountByUsername(username: String): Account?

    @Query("DELETE FROM Account WHERE id = :id")
    abstract suspend fun deleteAccountById(id: Long)

    // --- Student ---
    @Insert
    abstract suspend fun insertStudent(student: Student): Long

    @Update
    abstract suspend fun updateStudent(student: Student)

    @Query("SELECT * FROM Student WHERE id = :id LIMIT 1")
    abstract suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM Student WHERE accountId = :accountId LIMIT 1")
    abstract suspend fun getStudentByAccountId(accountId: Long): Student?

    @Query("SELECT * FROM Student WHERE studentCode = :code LIMIT 1")
    abstract suspend fun getStudentByCode(code: String): Student?

    @Query("SELECT * FROM Student WHERE email = :email LIMIT 1")
    abstract suspend fun getStudentByEmail(email: String): Student?

    @Query("SELECT * FROM Student")
    abstract suspend fun getAllStudents(): List<Student>

    @Query("SELECT * FROM Student WHERE studentCode LIKE '%' || :q || '%' OR fullname LIKE '%' || :q || '%'")
    abstract suspend fun searchStudents(q: String): List<Student>

    @Delete
    abstract suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM StudySchedule WHERE studentId = :studentId")
    abstract suspend fun deleteSchedulesByStudentId(studentId: Long)

    @Transaction
    open suspend fun deleteStudentFull(student: Student) {
        deleteSchedulesByStudentId(student.id)
        deleteStudent(student)
        deleteAccountById(student.accountId)
    }

    // --- Subject ---
    @Insert
    abstract suspend fun insertSubject(subject: Subject): Long

    @Update
    abstract suspend fun updateSubject(subject: Subject)

    @Delete
    abstract suspend fun deleteSubject(subject: Subject)

    @Query("SELECT * FROM Subject WHERE id = :id LIMIT 1")
    abstract suspend fun getSubjectById(id: Long): Subject?

    @Query("SELECT * FROM Subject WHERE subjectCode = :code LIMIT 1")
    abstract suspend fun getSubjectByCode(code: String): Subject?

    @Query("SELECT * FROM Subject")
    abstract suspend fun getAllSubjects(): List<Subject>

    @Query("SELECT * FROM Subject WHERE subjectCode LIKE '%' || :q || '%' OR subjectName LIKE '%' || :q || '%'")
    abstract suspend fun searchSubjects(q: String): List<Subject>

    @Query("SELECT COUNT(*) FROM StudySchedule WHERE subjectId = :subjectId")
    abstract suspend fun countSchedulesBySubject(subjectId: Long): Int

    // --- StudySchedule ---
    @Insert
    abstract suspend fun insertSchedule(schedule: StudySchedule): Long

    @Update
    abstract suspend fun updateSchedule(schedule: StudySchedule)

    @Query("DELETE FROM StudySchedule WHERE id = :id")
    abstract suspend fun deleteScheduleById(id: Long)

    @Query("SELECT * FROM StudySchedule WHERE id = :id LIMIT 1")
    abstract suspend fun getScheduleById(id: Long): StudySchedule?

    @Query("UPDATE StudySchedule SET status = :status WHERE id = :id")
    abstract suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT s.*, sub.subjectCode AS subjectCode, sub.subjectName AS subjectName FROM StudySchedule s INNER JOIN Subject sub ON s.subjectId = sub.id WHERE s.id = :id LIMIT 1")
    abstract suspend fun getScheduleViewById(id: Long): ScheduleView?

    @Query("SELECT s.*, sub.subjectCode AS subjectCode, sub.subjectName AS subjectName FROM StudySchedule s INNER JOIN Subject sub ON s.subjectId = sub.id WHERE s.studentId = :studentId ORDER BY s.studyDate ASC, s.startTime ASC")
    abstract suspend fun getScheduleViewsByStudent(studentId: Long): List<ScheduleView>

    @Query("SELECT s.*, sub.subjectCode AS subjectCode, sub.subjectName AS subjectName FROM StudySchedule s INNER JOIN Subject sub ON s.subjectId = sub.id WHERE s.studentId = :studentId AND (:status = '' OR s.status = :status) AND (sub.subjectName LIKE '%' || :q || '%' OR sub.subjectCode LIKE '%' || :q || '%' OR s.location LIKE '%' || :q || '%' OR s.studyDate LIKE '%' || :q || '%') ORDER BY s.studyDate ASC, s.startTime ASC")
    abstract suspend fun searchScheduleViews(studentId: Long, q: String, status: String): List<ScheduleView>

    @Query("SELECT s.*, sub.subjectCode AS subjectCode, sub.subjectName AS subjectName FROM StudySchedule s INNER JOIN Subject sub ON s.subjectId = sub.id WHERE s.studentId = :studentId AND s.studyDate BETWEEN :fromDate AND :toDate AND s.status != 'CANCELLED' ORDER BY s.studyDate ASC, s.startTime ASC")
    abstract suspend fun getScheduleViewsInRange(studentId: Long, fromDate: String, toDate: String): List<ScheduleView>

    @Query("SELECT * FROM StudySchedule WHERE studentId = :studentId AND studyDate = :date AND status != 'CANCELLED' AND id != :excludeId AND startTime < :end AND endTime > :start")
    abstract suspend fun getOverlapping(studentId: Long, date: String, start: String, end: String, excludeId: Long): List<StudySchedule>
}
